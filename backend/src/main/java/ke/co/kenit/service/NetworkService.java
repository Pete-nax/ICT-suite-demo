package ke.co.kenit.service;

import ke.co.kenit.model.NetworkDevice;
import ke.co.kenit.repository.NetworkDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NetworkService {

    private final NetworkDeviceRepository deviceRepo;

    // Ping all known devices every 2 minutes — keeps the dashboard live
    @Scheduled(fixedDelay = 120_000)
    @Transactional
    public void pingAllDevices() {
        List<NetworkDevice> devices = deviceRepo.findAll();
        if (devices.isEmpty()) return;

        log.info("Ping sweep: checking {} devices", devices.size());

        for (NetworkDevice device : devices) {
            try {
                long start = System.currentTimeMillis();
                boolean reachable = InetAddress.getByName(device.getIpAddress())
                    .isReachable(3000); // 3s timeout — enough for a slow office network
                long pingMs = System.currentTimeMillis() - start;

                device.setOnline(reachable);
                device.setPingMs(reachable ? pingMs : null);

                if (reachable) {
                    device.setLastSeen(LocalDateTime.now());
                }
                deviceRepo.save(device);

            } catch (IOException e) {
                // Network is down or device IP changed — mark offline and move on
                device.setOnline(false);
                deviceRepo.save(device);
                log.debug("Could not reach {}: {}", device.getIpAddress(), e.getMessage());
            }
        }
    }

    @Transactional
    public NetworkDevice registerOrUpdateDevice(String ip, String mac, String hostname, String vendor) {
        return deviceRepo.findByIpAddress(ip).map(existing -> {
            existing.setMacAddress(mac);
            existing.setHostname(hostname);
            existing.setVendor(vendor);
            existing.setOnline(true);
            existing.setLastSeen(LocalDateTime.now());
            return deviceRepo.save(existing);
        }).orElseGet(() -> {
            var device = new NetworkDevice();
            device.setIpAddress(ip);
            device.setMacAddress(mac);
            device.setHostname(hostname);
            device.setVendor(vendor);
            device.setOnline(true);
            device.setFirstSeen(LocalDateTime.now());
            device.setLastSeen(LocalDateTime.now());
            return deviceRepo.save(device);
        });
    }

    public List<NetworkDevice> getAllDevices() {
        return deviceRepo.findAll();
    }

    public List<NetworkDevice> getOnlineDevices() {
        return deviceRepo.findByIsOnlineTrue();
    }
}
