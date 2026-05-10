package ke.co.kenit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Format: KEN-2026-001 — easy to put on a sticker and stick on the laptop
    @Column(unique = true, nullable = false)
    private String assetTag;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private AssetType type;

    private String brand;
    private String model;
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    private AssetStatus status = AssetStatus.ACTIVE;

    // Who has this asset right now
    private String assignedTo;
    private String department;
    private String location; // e.g., "Server Room", "Floor 2 - Open Office"

    // KES purchase cost — useful for budget reporting
    @Column(precision = 12, scale = 2)
    private BigDecimal purchaseCostKes;

    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;

    private String ipAddress; // filled by the network scanner
    private String macAddress;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum AssetType {
        LAPTOP, DESKTOP, MONITOR, PRINTER, SCANNER,
        ROUTER, SWITCH, SERVER, UPS, PHONE, TABLET, LICENSE, OTHER
    }

    public enum AssetStatus {
        ACTIVE, IN_REPAIR, DECOMMISSIONED, IN_STORAGE, LOST, STOLEN
    }
}
