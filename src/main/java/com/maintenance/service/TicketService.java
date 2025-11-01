package com.maintenance.service;

import com.maintenance.entity.Ticket;
import com.maintenance.entity.User;
import com.maintenance.entity.enums.Priority;
import com.maintenance.entity.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TicketService {
    
    // Basic CRUD operations
    Ticket createTicket(Ticket ticket);
    
    Ticket updateTicket(Long id, Ticket ticket);
    
    void deleteTicket(Long id);
    
    Optional<Ticket> findById(Long id);
    
    // List operations
    List<Ticket> findAllTickets();
    
    Page<Ticket> findAllTickets(Pageable pageable);
    
    List<Ticket> findTicketsByReporter(User reporter);
    
    Page<Ticket> findTicketsByReporter(User reporter, Pageable pageable);
    
    List<Ticket> findTicketsByAssignee(User assignee);
    
    Page<Ticket> findTicketsByAssignee(User assignee, Pageable pageable);
    
    List<Ticket> findTicketsByStatus(TicketStatus status);
    
    Page<Ticket> findTicketsByStatus(TicketStatus status, Pageable pageable);
    
    List<Ticket> findTicketsByPriority(Priority priority);
    
    Page<Ticket> findTicketsByPriority(Priority priority, Pageable pageable);
    
    // Status-based operations
    Ticket updateTicketStatus(Long ticketId, TicketStatus newStatus, User changedBy, String reason);
    
    Ticket assignTicket(Long ticketId, User assignee, User assignedBy);
    
    Ticket unassignTicket(Long ticketId, User changedBy);
    
    // Search functionality
    List<Ticket> searchTickets(String search);
    
    Page<Ticket> searchTickets(String search, Pageable pageable);
    
    // Overdue and due soon tickets
    List<Ticket> findOverdueTickets();
    
    List<Ticket> findOverdueTicketsByAssignee(User assignee);
    
    List<Ticket> findTicketsDueSoon(int days);
    
    // Recent and trending
    List<Ticket> findRecentTickets(int limit);
    
    List<Ticket> findRecentlyUpdated(int limit);
    
    // Unassigned tickets
    List<Ticket> findUnassignedTickets();
    
    // Statistics
    long countAllTickets();
    
    long countTicketsByStatus(TicketStatus status);
    
    long countTicketsByReporter(User reporter);
    
    long countTicketsByAssignee(User assignee);
    
    long countTicketsByReporterAndStatus(User reporter, TicketStatus status);
    
    long countTicketsByAssigneeAndStatus(User assignee, TicketStatus status);
    
    long countOverdueTickets();
    
    long countOverdueTicketsByAssignee(User assignee);
    
    // Dashboard data
    Map<String, Long> getTicketCountByStatus();
    
    Map<String, Long> getTicketCountByPriority();
    
    Map<String, Long> getTicketCountByCategory();
    
    Map<String, Long> getMonthlyTicketCount(int months);
}