package ke.co.kenit.service;

import ke.co.kenit.dto.DashboardStats;
import ke.co.kenit.model.Asset;
import ke.co.kenit.model.Ticket;
import ke.co.kenit.repository.AssetRepository;
import ke.co.kenit.repository.NetworkDeviceRepository;
import ke.co.kenit.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepo;
    private final AssetRepository assetRepo;
    private final NetworkDeviceRepository deviceRepo;
    private final TicketService ticketService;

    public DashboardStats getSummary() {
        return DashboardStats.builder()
            .openTickets(ticketRepo.countByStatus(Ticket.Status.OPEN))
            .inProgressTickets(ticketRepo.countByStatus(Ticket.Status.IN_PROGRESS))
            .resolvedTodayTickets(ticketService.countResolvedToday())
            .criticalTickets(ticketRepo.countByPriority(Ticket.Priority.CRITICAL))
            .totalAssets(assetRepo.count())
            .activeAssets(assetRepo.countByStatus(Asset.AssetStatus.ACTIVE))
            .assetsInRepair(assetRepo.countByStatus(Asset.AssetStatus.IN_REPAIR))
            .totalAssetValueKes(assetRepo.sumActiveAssetValue())
            .totalDevices(deviceRepo.count())
            .onlineDevices(deviceRepo.countByIsOnline(true))
            .offlineDevices(deviceRepo.countByIsOnline(false))
            .lastNetworkScan(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")))
            .build();
    }
}
