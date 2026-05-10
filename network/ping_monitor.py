#!/usr/bin/env python3
"""
KenIT Ping Monitor
Continuously monitors critical network hosts and logs status changes.
Run this on the ICT server — it'll catch outages before users call you.

Usage:
    python3 ping_monitor.py --config hosts.json
    python3 ping_monitor.py  # uses default hosts below
"""

import socket
import time
import json
import argparse
import os
from datetime import datetime

# Default critical hosts for a typical Kenyan office setup
# Edit this or pass --config with your own JSON file
DEFAULT_HOSTS = [
    {"name": "Main Router",       "ip": "192.168.1.1",   "critical": True},
    {"name": "DNS Server",        "ip": "8.8.8.8",       "critical": True},
    {"name": "KRA iTax Portal",   "ip": "itax.kra.go.ke","critical": False},
    {"name": "File Server",       "ip": "192.168.1.10",  "critical": True},
    {"name": "Print Server",      "ip": "192.168.1.20",  "critical": False},
]

INTERVAL_SECONDS = 30
LOG_FILE = "ping_monitor.log"


def tcp_ping(host: str, port: int = 80, timeout: int = 3) -> tuple[bool, float]:
    """
    TCP connect ping — doesn't need sudo unlike ICMP.
    Checks if a port is open, which is good enough for monitoring.
    """
    start = time.time()
    try:
        with socket.create_connection((host, port), timeout=timeout):
            elapsed = (time.time() - start) * 1000
            return True, round(elapsed, 1)
    except (socket.timeout, socket.error, OSError):
        return False, 0.0


def icmp_ping(host: str) -> tuple[bool, float]:
    """
    ICMP ping via subprocess. More reliable than TCP for devices
    that don't have any open ports (e.g. printers, switches).
    Needs sudo on Linux.
    """
    import subprocess
    start = time.time()
    try:
        result = subprocess.run(
            ["ping", "-c", "1", "-W", "2", host],
            stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=3
        )
        elapsed = (time.time() - start) * 1000
        return result.returncode == 0, round(elapsed, 1)
    except Exception:
        return False, 0.0


def check_host(host: dict) -> dict:
    """Check a single host — try ICMP first, fall back to TCP."""
    ip = host["ip"]
    is_up, ms = icmp_ping(ip)

    if not is_up:
        # Maybe it just blocks ICMP — try TCP port 80
        is_up, ms = tcp_ping(ip)

    return {
        **host,
        "is_up": is_up,
        "ping_ms": ms,
        "checked_at": datetime.now().strftime("%H:%M:%S")
    }


def log_event(message: str):
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    line = f"[{timestamp}] {message}"
    print(line)
    with open(LOG_FILE, "a") as f:
        f.write(line + "\n")


def monitor_loop(hosts: list[dict]):
    """Main monitoring loop — runs forever until Ctrl+C."""
    # Track previous state so we only log status changes, not every ping
    prev_status = {h["ip"]: None for h in hosts}

    print(f"KenIT Ping Monitor started — checking {len(hosts)} host(s) every {INTERVAL_SECONDS}s")
    print(f"Log file: {os.path.abspath(LOG_FILE)}\n")
    print(f"{'Host':<25} {'IP':<20} {'Status':<10} {'Ping'}")
    print("-" * 65)

    while True:
        results = [check_host(h) for h in hosts]

        # Clear and redraw the status table
        os.system("clear" if os.name == "posix" else "cls")
        print(f"KenIT Network Monitor — {datetime.now().strftime('%d %b %Y %H:%M:%S EAT')}")
        print(f"{'Host':<25} {'IP':<20} {'Status':<12} {'Ping (ms)'}")
        print("-" * 70)

        for r in results:
            status_icon = "✅ UP" if r["is_up"] else "🔴 DOWN"
            ping_display = f"{r['ping_ms']}ms" if r["is_up"] else "—"
            crit_mark = " ⚠️" if not r["is_up"] and r["critical"] else ""
            print(f"{r['name']:<25} {r['ip']:<20} {status_icon:<12} {ping_display}{crit_mark}")

            # Only log when status changes — avoids a huge log file
            current = r["is_up"]
            if prev_status[r["ip"]] is not None and prev_status[r["ip"]] != current:
                event = "RECOVERED" if current else "DOWN"
                log_event(f"[{event}] {r['name']} ({r['ip']}) is now {'UP' if current else 'DOWN'}")
                if not current and r["critical"]:
                    log_event(f"  ⚠️  CRITICAL HOST DOWN — escalate immediately")

            prev_status[r["ip"]] = current

        print(f"\nNext check in {INTERVAL_SECONDS}s — Ctrl+C to stop")
        time.sleep(INTERVAL_SECONDS)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="KenIT Ping Monitor")
    parser.add_argument("--config", help="Path to JSON file with host list")
    args = parser.parse_args()

    if args.config:
        with open(args.config) as f:
            hosts = json.load(f)
    else:
        hosts = DEFAULT_HOSTS

    try:
        monitor_loop(hosts)
    except KeyboardInterrupt:
        print("\nMonitor stopped.")
