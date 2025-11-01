package com.maintenance.controller;

import com.maintenance.dto.TicketDTO;
import com.maintenance.dto.TicketStatusUpdateDTO;
import com.maintenance.dto.TicketDTO;
import com.maintenance.dto.TicketStatusUpdateDTO;
import com.maintenance.entity.Ticket;
import com.maintenance.entity.User;
import com.maintenance.entity.enums.Priority;
import com.maintenance.entity.enums.TicketStatus;
import com.maintenance.security.SecurityService;
import com.maintenance.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tickets", description = "Ticket management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {
    
    private final TicketService ticketService;
    private final SecurityService securityService;
    
    @GetMapping
    @Operation(summary = "Get all tickets", description = "Retrieve all tickets with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Page<Ticket>> getAllTickets(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching all tickets with pagination");
        Page<Ticket> tickets = ticketService.findAllTickets(pageable);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/my")
    @Operation(summary = "Get my tickets", description = "Retrieve tickets for the authenticated user")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<Page<Ticket>> getMyTickets(
            @PageableDefault(size = 20) Pageable pageable) {
        
        User currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        log.info("Fetching tickets for user: {}", currentUser.getUsername());
        Page<Ticket> tickets = ticketService.findTicketsByReporter(currentUser, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/assigned")
    @Operation(summary = "Get assigned tickets", description = "Retrieve tickets assigned to the authenticated user")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<Page<Ticket>> getAssignedTickets(
            @PageableDefault(size = 20) Pageable pageable) {
        
        User currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        log.info("Fetching assigned tickets for user: {}", currentUser.getUsername());
        Page<Ticket> tickets = ticketService.findTicketsByAssignee(currentUser, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID", description = "Retrieve a specific ticket by its ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'TENANT')")
    public ResponseEntity<Ticket> getTicketById(
            @Parameter(description = "Ticket ID") @PathVariable Long id) {
        
        log.info("Fetching ticket with ID: {}", id);
        Optional<Ticket> ticket = ticketService.findById(id);
        
        return ticket.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create new ticket", description = "Create a new maintenance ticket")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'TENANT')")
    public ResponseEntity<Ticket> createTicket(
            @Valid @RequestBody TicketDTO ticketDTO) {
        
        User currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        log.info("Creating new ticket by user: {}", currentUser.getUsername());
        
        // Convert DTO to entity
        Ticket ticket = convertToEntity(ticketDTO);
        ticket.setReporter(currentUser);
        
        Ticket createdTicket = ticketService.createTicket(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update ticket", description = "Update an existing ticket")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Ticket> updateTicket(
            @Parameter(description = "Ticket ID") @PathVariable Long id,
            @Valid @RequestBody TicketDTO ticketDTO) {
        
        User currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        log.info("Updating ticket with ID: {} by user: {}", id, currentUser.getUsername());
        
        Ticket existingTicket = ticketService.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        // Update ticket fields
        updateTicketFields(existingTicket, ticketDTO);
        
        Ticket updatedTicket = ticketService.updateTicket(id, existingTicket);
        return ResponseEntity.ok(updatedTicket);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ticket", description = "Delete a ticket (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(
            @Parameter(description = "Ticket ID") @PathVariable Long id) {
        
        log.info("Deleting ticket with ID: {}", id);
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update ticket status", description = "Update the status of a ticket")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Ticket> updateTicketStatus(
            @Parameter(description = "Ticket ID") @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateDTO statusUpdate) {
        
        User currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        log.info("Updating status of ticket {} to {} by user: {}", 
                id, statusUpdate.getStatus(), currentUser.getUsername());
        
        Ticket updatedTicket = ticketService.updateTicketStatus(
                id, 
                TicketStatus.valueOf(statusUpdate.getStatus()), 
                currentUser, 
                statusUpdate.getReason()
        );
        
        return ResponseEntity.ok(updatedTicket);
    }
    
    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign ticket", description = "Assign a ticket to a user")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Ticket> assignTicket(
            @Parameter(description = "Ticket ID") @PathVariable Long id,
            @Parameter(description = "User ID to assign to") @RequestParam Long userId) {
        
        User currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        log.info("Assigning ticket {} to user {} by user: {}", 
                id, userId, currentUser.getUsername());
        
        // This would need user service to get the assignee user
        // For now, just return the ticket
        Ticket ticket = ticketService.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        return ResponseEntity.ok(ticket);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search tickets", description = "Search tickets by various criteria")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Page<Ticket>> searchTickets(
            @Parameter(description = "Search query") @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Searching tickets with query: {}", query);
        Page<Ticket> tickets = ticketService.searchTickets(query, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tickets", description = "Retrieve all overdue tickets")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<List<Ticket>> getOverdueTickets() {
        
        log.info("Fetching overdue tickets");
        List<Ticket> overdueTickets = ticketService.findOverdueTickets();
        return ResponseEntity.ok(overdueTickets);
    }
    
    @GetMapping("/unassigned")
    @Operation(summary = "Get unassigned tickets", description = "Retrieve all unassigned tickets")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<List<Ticket>> getUnassignedTickets() {
        
        log.info("Fetching unassigned tickets");
        List<Ticket> unassignedTickets = ticketService.findUnassignedTickets();
        return ResponseEntity.ok(unassignedTickets);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get ticket statistics", description = "Retrieve ticket statistics for dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<?> getTicketStatistics() {
        
        log.info("Fetching ticket statistics");
        
        var stats = new Object() {
            public final long total = ticketService.countAllTickets();
            public final long open = ticketService.countTicketsByStatus(TicketStatus.OPEN);
            public final long inProgress = ticketService.countTicketsByStatus(TicketStatus.IN_PROGRESS);
            public final long resolved = ticketService.countTicketsByStatus(TicketStatus.RESOLVED);
            public final long closed = ticketService.countTicketsByStatus(TicketStatus.CLOSED);
            public final long overdue = ticketService.countOverdueTickets();
        };
        
        return ResponseEntity.ok(stats);
    }
    
    // Helper methods
    private Ticket convertToEntity(TicketDTO dto) {
        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setPriority(Priority.valueOf(dto.getPriority()));
        ticket.setStatus(TicketStatus.valueOf(dto.getStatus()));
        ticket.setEstimatedCompletion(dto.getEstimatedCompletion());
        return ticket;
    }
    
    private void updateTicketFields(Ticket ticket, TicketDTO dto) {
        if (dto.getTitle() != null) {
            ticket.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            ticket.setDescription(dto.getDescription());
        }
        if (dto.getPriority() != null) {
            ticket.setPriority(Priority.valueOf(dto.getPriority()));
        }
        if (dto.getStatus() != null) {
            ticket.setStatus(TicketStatus.valueOf(dto.getStatus()));
        }
        if (dto.getEstimatedCompletion() != null) {
            ticket.setEstimatedCompletion(dto.getEstimatedCompletion());
        }
    }
}