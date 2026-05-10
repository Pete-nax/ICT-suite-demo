package ke.co.kenit.service;

import ke.co.kenit.dto.TicketDTO;
import ke.co.kenit.model.Ticket;
import ke.co.kenit.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepo;

    // Simple counter for ticket numbers — fine for single-node, replace with DB sequence in prod
    private final AtomicLong ticketCounter = new AtomicLong(
        System.currentTimeMillis() % 100000
    );

    @Transactional
    public TicketDTO.Response createTicket(TicketDTO.CreateRequest req) {
        var ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setTitle(req.getTitle());
        ticket.setDescription(req.getDescription());
        ticket.setPriority(req.getPriority());
        ticket.setCategory(req.getCategory());
        ticket.setDepartment(req.getDepartment());
        ticket.setRaisedBy(req.getRaisedBy());
        ticket.setStatus(Ticket.Status.OPEN);

        return toResponse(ticketRepo.save(ticket));
    }

    @Transactional
    public TicketDTO.Response assignTicket(Long id, TicketDTO.AssignRequest req) {
        var ticket = findOrThrow(id);
        ticket.setAssignedTo(req.getAssignedTo());
        ticket.setStatus(Ticket.Status.IN_PROGRESS);
        return toResponse(ticketRepo.save(ticket));
    }

    @Transactional
    public TicketDTO.Response resolveTicket(Long id, TicketDTO.ResolveRequest req) {
        var ticket = findOrThrow(id);
        ticket.setResolutionNotes(req.getResolutionNotes());
        ticket.setStatus(Ticket.Status.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        return toResponse(ticketRepo.save(ticket));
    }

    public List<TicketDTO.Response> getAllTickets() {
        return ticketRepo.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<TicketDTO.Response> getByStatus(Ticket.Status status) {
        return ticketRepo.findByStatusOrderByCreatedAtDesc(status).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public long countResolvedToday() {
        var startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        return ticketRepo.countResolvedSince(startOfDay);
    }

    private Ticket findOrThrow(Long id) {
        return ticketRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Ticket " + id + " not found"));
    }

    private String generateTicketNumber() {
        int year = LocalDate.now().getYear();
        long seq = ticketCounter.incrementAndGet();
        return String.format("TKT-%d-%05d", year, seq);
    }

    private TicketDTO.Response toResponse(Ticket t) {
        var res = new TicketDTO.Response();
        res.setId(t.getId());
        res.setTicketNumber(t.getTicketNumber());
        res.setTitle(t.getTitle());
        res.setDescription(t.getDescription());
        res.setPriority(t.getPriority());
        res.setStatus(t.getStatus());
        res.setCategory(t.getCategory());
        res.setDepartment(t.getDepartment());
        res.setRaisedBy(t.getRaisedBy());
        res.setAssignedTo(t.getAssignedTo());
        res.setResolutionNotes(t.getResolutionNotes());
        res.setCreatedAt(t.getCreatedAt());
        res.setUpdatedAt(t.getUpdatedAt());
        res.setResolvedAt(t.getResolvedAt());
        return res;
    }
}
