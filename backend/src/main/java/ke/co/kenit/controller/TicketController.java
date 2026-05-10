package ke.co.kenit.controller;

import jakarta.validation.Valid;
import ke.co.kenit.dto.TicketDTO;
import ke.co.kenit.model.Ticket;
import ke.co.kenit.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public List<TicketDTO.Response> getAllTickets(
            @RequestParam(required = false) Ticket.Status status) {
        if (status != null) {
            return ticketService.getByStatus(status);
        }
        return ticketService.getAllTickets();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDTO.Response createTicket(@Valid @RequestBody TicketDTO.CreateRequest req) {
        return ticketService.createTicket(req);
    }

    @PatchMapping("/{id}/assign")
    public TicketDTO.Response assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketDTO.AssignRequest req) {
        return ticketService.assignTicket(id, req);
    }

    @PatchMapping("/{id}/resolve")
    public TicketDTO.Response resolveTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketDTO.ResolveRequest req) {
        return ticketService.resolveTicket(id, req);
    }
}
