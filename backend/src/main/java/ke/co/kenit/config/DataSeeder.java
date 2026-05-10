package ke.co.kenit.config;

import ke.co.kenit.dto.AssetDTO;
import ke.co.kenit.dto.TicketDTO;
import ke.co.kenit.model.Asset;
import ke.co.kenit.model.Ticket;
import ke.co.kenit.repository.TicketRepository;
import ke.co.kenit.service.AssetService;
import ke.co.kenit.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final TicketService ticketService;
    private final AssetService assetService;
    private final TicketRepository ticketRepo;

    // Only seeds on first boot — if rows already exist (e.g. after a redeploy), skip.
    // Without this guard, prod restarts crash on duplicate ticket numbers.
    @Bean
    public ApplicationRunner seedDemoData() {
        return args -> {
            if (ticketRepo.count() > 0) {
                log.info("DB already has data — skipping seed");
                return;
            }

            log.info("Fresh DB — seeding Kenyan office demo data...");

            createTicket("Printer on Floor 3 not responding", Ticket.Priority.HIGH,
                Ticket.Category.PRINTER, "Finance", "Wanjiku M.");
            createTicket("Cannot access KRA iTax portal", Ticket.Priority.CRITICAL,
                Ticket.Category.NETWORK, "Finance", "Otieno K.");
            createTicket("Laptop screen flickering", Ticket.Priority.MEDIUM,
                Ticket.Category.HARDWARE, "HR", "Akinyi N.");
            createTicket("Outlook keeps crashing on Windows 11", Ticket.Priority.MEDIUM,
                Ticket.Category.SOFTWARE, "Registry", "Muthoni W.");
            createTicket("Need new user account for new hire", Ticket.Priority.LOW,
                Ticket.Category.ACCOUNT_ACCESS, "HR", "Kamau J.");

            createAsset("Dell Latitude 5540", Asset.AssetType.LAPTOP, "Dell", "Latitude 5540",
                "ICT", "Server Room", new BigDecimal("145000"));
            createAsset("HP LaserJet Pro M404dn", Asset.AssetType.PRINTER, "HP", "LaserJet M404dn",
                "Finance", "Floor 2", new BigDecimal("38000"));
            createAsset("Cisco RV340 Router", Asset.AssetType.ROUTER, "Cisco", "RV340",
                "ICT", "Server Room", new BigDecimal("25000"));
            createAsset("APC Smart-UPS 1500", Asset.AssetType.UPS, "APC", "Smart-UPS 1500",
                "ICT", "Server Room", new BigDecimal("42000"));
            createAsset("HP ProBook 450 G9", Asset.AssetType.LAPTOP, "HP", "ProBook 450 G9",
                "HR", "Floor 1 - HR Desk", new BigDecimal("98000"));

            log.info("Demo data seeded — dashboard is live");
        };
    }

    private void createTicket(String title, Ticket.Priority priority,
                               Ticket.Category category, String dept, String raisedBy) {
        var req = new TicketDTO.CreateRequest();
        req.setTitle(title);
        req.setPriority(priority);
        req.setCategory(category);
        req.setDepartment(dept);
        req.setRaisedBy(raisedBy);
        ticketService.createTicket(req);
    }

    private void createAsset(String name, Asset.AssetType type, String brand,
                              String model, String dept, String location, BigDecimal cost) {
        var req = new AssetDTO.CreateRequest();
        req.setName(name);
        req.setType(type);
        req.setBrand(brand);
        req.setModel(model);
        req.setDepartment(dept);
        req.setLocation(location);
        req.setPurchaseCostKes(cost);
        req.setPurchaseDate(LocalDate.now().minusMonths(6));
        assetService.createAsset(req);
    }
}
