package ke.co.kenit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "network_devices")
@Data
@NoArgsConstructor
public class NetworkDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ipAddress;

    private String macAddress;
    private String hostname;
    private String vendor; // resolved from MAC OUI lookup

    private boolean isOnline;

    // Last time we successfully pinged this device
    private LocalDateTime lastSeen;
    private LocalDateTime firstSeen;

    // Response time in milliseconds — helps spot slow devices on the network
    private Long pingMs;

    private String openPorts; // comma-separated: "22,80,443,3389"

    // Linked to an asset record if we know what this device is
    private String assetTag;
}
