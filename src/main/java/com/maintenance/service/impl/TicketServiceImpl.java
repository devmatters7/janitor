package com.maintenance.service.impl;

import com.maintenance.entity.*;
import com.maintenance.entity.enums.Priority;
import com.maintenance.entity.enums.TicketStatus;
import com.maintenance.exception.ResourceNotFoundException;
import com.maintenance.repository.TicketRepository;
import com.maintenance.repository.TicketStatusHistoryRepository;
import com.maintenance.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketServiceImpl implements TicketService {
    
    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository statusHistoryRepository;
    
    @Override
    @CacheEvict(value = {"tickets", "ticketStats"}, allEntries = true)
    public Ticket createTicket(Ticket ticket) {
        log.info("Creating new ticket: {}", ticket.getTitle());
        
        // Set default values if not provided
        if (ticket.getPriority() == null) {
            ticket.setPriority(ticket.getCategory().getDefaultPriority());
        }
        
        if (ticket.getStatus() == null) {
            ticket.setStatus(TicketStatus.OPEN);
        }
        
        // Save the ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Create initial status history entry
        createStatusHistoryEntry(savedTicket, null, savedTicket.getStatus(), 
                savedTicket.getReporter(), "Ticket created");
        
        return savedTicket;
    }
    
    @Override
    @CacheEvict(value = {"tickets", "ticketStats"}, allEntries = true)
    public Ticket updateTicket(Long id, Ticket ticketDetails) {
        log.info("Updating ticket with id: {}", id);
        
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        
        // Store old status for history
        TicketStatus oldStatus = ticket.getStatus();
        
        // Update ticket details
        ticket.setTitle(ticketDetails.getTitle());
        ticket.setDescription(ticketDetails.getDescription());
        ticket.setCategory(ticketDetails.getCategory());
        ticket.setPriority(ticketDetails.getPriority());
        ticket.setStatus(ticketDetails.getStatus());
        ticket.setBuilding(ticketDetails.getBuilding());
        ticket.setRoom(ticketDetails.getRoom());
        ticket.setEstimatedCompletion(ticketDetails.getEstimatedCompletion());
        ticket.setResolutionNotes(ticketDetails.getResolutionNotes());
        
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // Create status history entry if status changed
        if (oldStatus != ticketDetails.getStatus()) {
            createStatusHistoryEntry(updatedTicket, oldStatus, ticketDetails.getStatus(), 
                    ticketDetails.getReporter(), "Status updated");
        }
        
        return updatedTicket;
    }
    
    @Override
    @CacheEvict(value = {"tickets", "ticketStats"}, allEntries = true)
    public void deleteTicket(Long id) {
        log.info("Deleting ticket with id: {}", id);
        
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        
        ticketRepository.delete(ticket);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tickets", key = "#id")
    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findByIdWithDetails(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tickets")
    public List<Ticket> findAllTickets() {
        return ticketRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> findAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByReporter(User reporter) {
        return ticketRepository.findByReporter(reporter);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> findTicketsByReporter(User reporter, Pageable pageable) {
        return ticketRepository.findByReporter(reporter, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByAssignee(User assignee) {
        return ticketRepository.findByAssignee(assignee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> findTicketsByAssignee(User assignee, Pageable pageable) {
        return ticketRepository.findByAssignee(assignee, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> findTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByPriority(Priority priority) {
        return ticketRepository.findByPriority(priority);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> findTicketsByPriority(Priority priority, Pageable pageable) {
        return ticketRepository.findByPriority(priority, pageable);
    }
    
    @Override
    @CacheEvict(value = {"tickets", "ticketStats"}, allEntries = true)
    public Ticket updateTicketStatus(Long ticketId, TicketStatus newStatus, User changedBy, String reason) {
        log.info("Updating ticket {} status to {}", ticketId, newStatus);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        
        // Update completion dates
        if (newStatus == TicketStatus.RESOLVED && ticket.getActualCompletion() == null) {
            ticket.setActualCompletion(LocalDateTime.now());
        }
        
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // Create status history entry
        createStatusHistoryEntry(updatedTicket, oldStatus, newStatus, changedBy, reason);
        
        return updatedTicket;
    }
    
    @Override
    @CacheEvict(value = {"tickets", "ticketStats"}, allEntries = true)
    public Ticket assignTicket(Long ticketId, User assignee, User assignedBy) {
        log.info("Assigning ticket {} to user {}", ticketId, assignee.getUsername());
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        
        ticket.setAssignee(assignee);
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // Create status history entry
        createStatusHistoryEntry(updatedTicket, updatedTicket.getStatus(), updatedTicket.getStatus(), 
                assignedBy, "Ticket assigned to " + assignee.getFullName());
        
        return updatedTicket;
    }
    
    @Override
    @CacheEvict(value = {"tickets", "ticketStats"}, allEntries = true)
    public Ticket unassignTicket(Long ticketId, User changedBy) {
        log.info("Unassigning ticket {}", ticketId);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        
        User previousAssignee = ticket.getAssignee();
        ticket.setAssignee(null);
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // Create status history entry
        String reason = previousAssignee != null ? 
                "Ticket unassigned from " + previousAssignee.getFullName() : "Ticket unassigned";
        createStatusHistoryEntry(updatedTicket, updatedTicket.getStatus(), updatedTicket.getStatus(), 
                changedBy, reason);
        
        return updatedTicket;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> searchTickets(String search) {
        return ticketRepository.searchUsersList(search);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> searchTickets(String search, Pageable pageable) {
        return ticketRepository.searchTickets(search, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findOverdueTickets() {
        return ticketRepository.findOverdueTickets(LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findOverdueTicketsByAssignee(User assignee) {
        return ticketRepository.findOverdueTicketsByAssignee(assignee, LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsDueSoon(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(days);
        return ticketRepository.findTicketsDueSoon(now, dueDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findRecentTickets(int limit) {
        return ticketRepository.findRecentTickets(limit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findRecentlyUpdated(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return ticketRepository.findRecentlyUpdated(since).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findUnassignedTickets() {
        return ticketRepository.findUnassignedTickets();
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ticketStats", key = "'countAll'")
    public long countAllTickets() {
        return ticketRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ticketStats", key = "'countByStatus_' + #status")
    public long countTicketsByStatus(TicketStatus status) {
        return ticketRepository.countByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTicketsByReporter(User reporter) {
        return ticketRepository.countByReporter(reporter);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTicketsByAssignee(User assignee) {
        return ticketRepository.countByAssignee(assignee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTicketsByReporterAndStatus(User reporter, TicketStatus status) {
        return ticketRepository.countByReporterAndStatus(reporter, status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTicketsByAssigneeAndStatus(User assignee, TicketStatus status) {
        return ticketRepository.countByAssigneeAndStatus(assignee, status);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ticketStats", key = "'countOverdue'")
    public long countOverdueTickets() {
        return ticketRepository.findOverdueTickets(LocalDateTime.now()).size();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countOverdueTicketsByAssignee(User assignee) {
        return ticketRepository.findOverdueTicketsByAssignee(assignee, LocalDateTime.now()).size();
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ticketStats", key = "'countByStatus'")
    public Map<String, Long> getTicketCountByStatus() {
        List<Object[]> results = ticketRepository.countTicketsByStatus();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> ((TicketStatus) row[0]).name(),
                        row -> (Long) row[1]
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ticketStats", key = "'countByPriority'")
    public Map<String, Long> getTicketCountByPriority() {
        List<Object[]> results = ticketRepository.countTicketsByPriority();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> ((Priority) row[0]).name(),
                        row -> (Long) row[1]
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ticketStats", key = "'countByCategory'")
    public Map<String, Long> getTicketCountByCategory() {
        List<Object[]> results = ticketRepository.countTicketsByCategory();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ticketStats", key = "'monthlyCount_' + #months")
    public Map<String, Long> getMonthlyTicketCount(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = ticketRepository.countTicketsByMonth(startDate);
        
        Map<String, Long> monthlyCount = new LinkedHashMap<>();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        // Initialize with all months
        for (int i = 0; i < months; i++) {
            LocalDateTime month = LocalDateTime.now().minusMonths(months - 1 - i);
            String monthKey = monthNames[month.getMonthValue() - 1] + " " + month.getYear();
            monthlyCount.put(monthKey, 0L);
        }
        
        // Update with actual counts
        for (Object[] row : results) {
            Integer monthNum = (Integer) row[0];
            Long count = (Long) row[1];
            String monthKey = monthNames[monthNum - 1] + " " + LocalDateTime.now().getYear();
            monthlyCount.put(monthKey, count);
        }
        
        return monthlyCount;
    }
    
    private void createStatusHistoryEntry(Ticket ticket, TicketStatus oldStatus, 
                                        TicketStatus newStatus, User changedBy, String reason) {
        TicketStatusHistory history = new TicketStatusHistory();
        history.setTicket(ticket);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangeReason(reason);
        
        statusHistoryRepository.save(history);
    }
}