#!/usr/bin/env python3
"""
KenIT Network Scanner
Discovers all devices on the local network and registers them
with the Spring Boot API.

Usage:
    sudo python3 scanner.py --subnet 192.168.1.0/24
    sudo python3 scanner.py --subnet 10.0.0.0/24 --api http://localhost:8080

Needs sudo because ARP needs raw socket access.
"""

import argparse
import socket
import requests
import ipaddress
import subprocess
import json
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

# MAC vendor lookup — covers the common brands you'll see in a Kenyan office
MAC_VENDORS = {
    "00:1A:A0": "Dell",
    "3C:D9:2B": "HP",
    "00:50:56": "VMware",
    "00:0C:29": "VMware",
    "B8:27:EB": "Raspberry Pi",
    "DC:A6:32": "Raspberry Pi",
    "00:1E:C9": "Intel",
    "FC:F8:AE": "Apple",
    "3C:22:FB": "Apple",
    "00:15:5D": "Microsoft Hyper-V",
    "00:1B:21": "Intel NIC",
    "08:00:27": "VirtualBox",
}


def resolve_vendor(mac: str) -> str:
    """Look up vendor from MAC OUI prefix."""
    if not mac:
        return "Unknown"
    prefix = mac.upper()[:8]
    return MAC_VENDORS.get(prefix, "Unknown")


def ping_host(ip: str, timeout: int = 1) -> bool:
    """Quick ICMP ping. Works on Linux — needs sudo for raw sockets."""
    try:
        result = subprocess.run(
            ["ping", "-c", "1", "-W", str(timeout), str(ip)],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            timeout=timeout + 1
        )
        return result.returncode == 0
    except (subprocess.TimeoutExpired, FileNotFoundError):
        return False


def get_hostname(ip: str) -> str:
    """Reverse DNS lookup. Fails silently — not all devices have hostnames."""
    try:
        return socket.gethostbyaddr(str(ip))[0]
    except (socket.herror, socket.gaierror):
        return ""


def arp_scan(subnet: str) -> list[dict]:
    """
    Use arp-scan if available (most accurate), fall back to ping sweep.
    arp-scan is way faster and gets MAC addresses reliably.
    Install: sudo apt install arp-scan
    """
    devices = []

    try:
        result = subprocess.run(
            ["arp-scan", "--localnet", "--interface", get_local_interface()],
            capture_output=True, text=True, timeout=30
        )
        if result.returncode == 0:
            for line in result.stdout.splitlines():
                parts = line.split("\t")
                if len(parts) >= 2 and is_valid_ip(parts[0]):
                    ip = parts[0].strip()
                    mac = parts[1].strip() if len(parts) > 1 else ""
                    devices.append({
                        "ip": ip,
                        "mac": mac,
                        "hostname": get_hostname(ip),
                        "vendor": resolve_vendor(mac)
                    })
            print(f"arp-scan found {len(devices)} devices")
            return devices
    except (subprocess.TimeoutExpired, FileNotFoundError):
        print("arp-scan not available, falling back to ping sweep...")

    # Ping sweep fallback
    return ping_sweep(subnet)


def ping_sweep(subnet: str) -> list[dict]:
    """Parallel ping sweep across the subnet."""
    network = ipaddress.ip_network(subnet, strict=False)
    hosts = list(network.hosts())

    print(f"Pinging {len(hosts)} hosts in {subnet}...")
    found = []

    with ThreadPoolExecutor(max_workers=50) as executor:
        futures = {executor.submit(ping_host, str(ip)): str(ip) for ip in hosts}
        for future in as_completed(futures):
            ip = futures[future]
            if future.result():
                hostname = get_hostname(ip)
                found.append({
                    "ip": ip,
                    "mac": get_mac_from_arp_cache(ip),
                    "hostname": hostname,
                    "vendor": "Unknown"
                })

    return found


def get_mac_from_arp_cache(ip: str) -> str:
    """Read MAC from the OS ARP cache after a ping."""
    try:
        result = subprocess.run(["arp", "-n", ip], capture_output=True, text=True)
        for line in result.stdout.splitlines():
            if ip in line and "ether" in line:
                parts = line.split()
                return parts[2] if len(parts) > 2 else ""
    except Exception:
        pass
    return ""


def get_local_interface() -> str:
    """Get the default network interface name."""
    try:
        result = subprocess.run(["ip", "route", "show", "default"],
                                capture_output=True, text=True)
        parts = result.stdout.split()
        idx = parts.index("dev") + 1
        return parts[idx]
    except (ValueError, IndexError):
        return "eth0"


def is_valid_ip(s: str) -> bool:
    try:
        ipaddress.ip_address(s)
        return True
    except ValueError:
        return False


def register_with_api(devices: list[dict], api_url: str):
    """Push discovered devices to the Spring Boot API."""
    print(f"\nRegistering {len(devices)} devices with {api_url}...")
    success = 0

    for device in devices:
        try:
            resp = requests.post(
                f"{api_url}/api/network/devices",
                params={
                    "ip": device["ip"],
                    "mac": device.get("mac", ""),
                    "hostname": device.get("hostname", ""),
                    "vendor": device.get("vendor", "")
                },
                timeout=5
            )
            if resp.status_code in (200, 201):
                success += 1
        except requests.exceptions.ConnectionError:
            print(f"  API not reachable at {api_url} — make sure Spring Boot is running")
            break
        except Exception as e:
            print(f"  Failed to register {device['ip']}: {e}")

    print(f"Registered {success}/{len(devices)} devices successfully")


def print_table(devices: list[dict]):
    """Print a clean table to stdout — useful for CLI-only use."""
    print(f"\n{'IP Address':<18} {'MAC Address':<20} {'Hostname':<30} {'Vendor'}")
    print("-" * 80)
    for d in sorted(devices, key=lambda x: ipaddress.ip_address(x["ip"])):
        print(f"{d['ip']:<18} {d.get('mac','N/A'):<20} {d.get('hostname','N/A'):<30} {d.get('vendor','Unknown')}")
    print(f"\nTotal: {len(devices)} device(s) found at {datetime.now().strftime('%d %b %Y %H:%M:%S EAT')}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="KenIT LAN Scanner")
    parser.add_argument("--subnet", default="192.168.1.0/24",
                        help="Subnet to scan, e.g. 192.168.1.0/24")
    parser.add_argument("--api", default="http://localhost:8080",
                        help="KenIT API base URL")
    parser.add_argument("--no-register", action="store_true",
                        help="Just print results, don't push to API")
    args = parser.parse_args()

    print(f"KenIT Network Scanner — {datetime.now().strftime('%d %b %Y %H:%M EAT')}")
    print(f"Target subnet: {args.subnet}\n")

    devices = arp_scan(args.subnet)

    print_table(devices)

    if not args.no_register:
        register_with_api(devices, args.api)
