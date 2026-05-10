package ke.co.kenit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Auto-generated: TKT-2026-00042 style
    @Column(unique = true, nullable = false)
    private String ticketNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;

    @Enumerated(EnumType.STRING)
    private Category category;

    // Which department raised this — Finance, HR, Registry, etc.
    private String department;

    private String raisedBy;

    // The technician handling it
    private String assignedTo;

    private String resolutionNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    public enum Status { OPEN, IN_PROGRESS, ON_HOLD, RESOLVED, CLOSED }

    public enum Category {
        HARDWARE, SOFTWARE, NETWORK, EMAIL, PRINTER,
        ACCOUNT_ACCESS, DATA_RECOVERY, OTHER
    }
}
