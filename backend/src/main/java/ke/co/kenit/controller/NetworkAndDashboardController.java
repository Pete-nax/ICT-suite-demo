package ke.co.kenit.controller;

import ke.co.kenit.dto.DashboardStats;
import ke.co.kenit.model.NetworkDevice;
import ke.co.kenit.service.DashboardService;
import ke.co.kenit.service.NetworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NetworkAndDashboardController {

    private final NetworkService networkService;
    private final DashboardService dashboardService;

    @GetMapping("/api/network/devices")
    public List<NetworkDevice> getAllDevices() {
        return networkService.getAllDevices();
    }

    @GetMapping("/api/network/devices/online")
    public List<NetworkDevice> getOnlineDevices() {
        return networkService.getOnlineDevices();
    }

    // Called by the Python scanner after it finishes a sweep
    @PostMapping("/api/network/devices")
    public NetworkDevice registerDevice(
            @RequestParam String ip,
            @RequestParam(required = false) String mac,
            @RequestParam(required = false) String hostname,
            @RequestParam(required = false) String vendor) {
        return networkService.registerOrUpdateDevice(ip, mac, hostname, vendor);
    }

    @GetMapping("/api/dashboard/stats")
    public DashboardStats getDashboardStats() {
        return dashboardService.getSummary();
    }
}
