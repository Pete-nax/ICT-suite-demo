package ke.co.kenit.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStats {
    // Ticket counts
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTodayTickets;
    private long criticalTickets;

    // Asset counts
    private long totalAssets;
    private long activeAssets;
    private long assetsInRepair;
    private BigDecimal totalAssetValueKes;

    // Network
    private long totalDevices;
    private long onlineDevices;
    private long offlineDevices;

    // Quick win for the interview demo — shows last scan timestamp
    private String lastNetworkScan;
}
