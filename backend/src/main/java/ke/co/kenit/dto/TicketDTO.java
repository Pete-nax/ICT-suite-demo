package ke.co.kenit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ke.co.kenit.model.Ticket;
import lombok.Data;

import java.time.LocalDateTime;

public class TicketDTO {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Title can't be empty")
        private String title;

        private String description;

        @NotNull
        private Ticket.Priority priority;

        @NotNull
        private Ticket.Category category;

        @NotBlank
        private String department;

        @NotBlank
        private String raisedBy;
    }

    @Data
    public static class AssignRequest {
        @NotBlank(message = "Need a technician name to assign this")
        private String assignedTo;
    }

    @Data
    public static class ResolveRequest {
        @NotBlank(message = "Resolution notes required — what did you actually do?")
        private String resolutionNotes;
    }

    @Data
    public static class Response {
        private Long id;
        private String ticketNumber;
        private String title;
        private String description;
        private Ticket.Priority priority;
        private Ticket.Status status;
        private Ticket.Category category;
        private String department;
        private String raisedBy;
        private String assignedTo;
        private String resolutionNotes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;
    }
}
