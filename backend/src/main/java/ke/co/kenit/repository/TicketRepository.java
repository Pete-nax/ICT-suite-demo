package ke.co.kenit.repository;

import ke.co.kenit.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByStatusOrderByCreatedAtDesc(Ticket.Status status);

    List<Ticket> findByDepartmentOrderByCreatedAtDesc(String department);

    List<Ticket> findByAssignedToOrderByCreatedAtDesc(String assignedTo);

    long countByStatus(Ticket.Status status);

    long countByPriority(Ticket.Priority priority);

    // Resolved today — useful for the daily standup report
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'RESOLVED' AND t.resolvedAt >= :startOfDay")
    long countResolvedSince(LocalDateTime startOfDay);
}
