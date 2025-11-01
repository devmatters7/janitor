package com.maintenance.repository;

import com.maintenance.entity.*;
import com.maintenance.entity.enums.Priority;
import com.maintenance.entity.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    // Basic queries with pagination
    Page<Ticket> findByReporter(User reporter, Pageable pageable);
    
    Page<Ticket> findByAssignee(User assignee, Pageable pageable);
    
    Page<Ticket> findByBuilding(Building building, Pageable pageable);
    
    Page<Ticket> findByRoom(Room room, Pageable pageable);
    
    Page<Ticket> findByCategory(TicketCategory category, Pageable pageable);
    
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    
    Page<Ticket> findByPriority(Priority priority, Pageable pageable);
    
    // Status-based queries
    List<Ticket> findByStatusIn(List<TicketStatus> statuses);
    
    Page<Ticket> findByStatusIn(List<TicketStatus> statuses, Pageable pageable);
    
    // Overdue tickets
    List<Ticket> findByStatusInAndEstimatedCompletionBefore(
            List<TicketStatus> statuses, LocalDateTime date);
    
    // Recent tickets
    List<Ticket> findByCreatedAtAfter(LocalDateTime date);
    
    // Tickets by building and status
    Page<Ticket> findByBuildingAndStatus(Building building, TicketStatus status, Pageable pageable);
    
    // Tickets by assignee and status
    Page<Ticket> findByAssigneeAndStatus(User assignee, TicketStatus status, Pageable pageable);
    
    // Complex queries
    @EntityGraph(attributePaths = {"reporter", "assignee", "building", "room", "category"})
    Optional<Ticket> findById(Long id);
    
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.reporter LEFT JOIN FETCH t.assignee " +
           "LEFT JOIN FETCH t.building LEFT JOIN FETCH t.room LEFT JOIN FETCH t.category " +
           "WHERE t.id = :id")
    Optional<Ticket> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.comments LEFT JOIN FETCH t.attachments WHERE t.id = :id")
    Optional<Ticket> findByIdWithCommentsAndAttachments(@Param("id") Long id);
    
    // Search functionality
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Ticket> searchTickets(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Ticket> searchTicketsList(@Param("search") String search);
    
    @Query("SELECT t FROM Ticket t WHERE t.reporter = :reporter AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Ticket> searchTicketsByReporter(@Param("search") String search, @Param("reporter") User reporter, Pageable pageable);
    
    // Dashboard queries
    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countTicketsByStatus();
    
    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> countTicketsByPriority();
    
    @Query("SELECT t.category.name, COUNT(t) FROM Ticket t GROUP BY t.category")
    List<Object[]> countTicketsByCategory();
    
    @Query("SELECT FUNCTION('MONTH', t.createdAt), COUNT(t) FROM Ticket t " +
           "WHERE t.createdAt >= :startDate GROUP BY FUNCTION('MONTH', t.createdAt)")
    List<Object[]> countTicketsByMonth(@Param("startDate") LocalDateTime startDate);
    
    // Overdue tickets
    @Query("SELECT t FROM Ticket t WHERE t.status IN ('OPEN', 'IN_PROGRESS') AND t.estimatedCompletion < :now")
    List<Ticket> findOverdueTickets(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Ticket t WHERE t.assignee = :assignee AND t.status IN ('OPEN', 'IN_PROGRESS') AND t.estimatedCompletion < :now")
    List<Ticket> findOverdueTicketsByAssignee(@Param("assignee") User assignee, @Param("now") LocalDateTime now);
    
    // Tickets due soon
    @Query("SELECT t FROM Ticket t WHERE t.status IN ('OPEN', 'IN_PROGRESS') AND t.estimatedCompletion BETWEEN :now AND :dueDate")
    List<Ticket> findTicketsDueSoon(@Param("now") LocalDateTime now, @Param("dueDate") LocalDateTime dueDate);
    
    // Unassigned tickets
    @Query("SELECT t FROM Ticket t WHERE t.assignee IS NULL AND t.status = 'OPEN'")
    List<Ticket> findUnassignedTickets();
    
    // Statistics for user
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.reporter = :reporter")
    long countByReporter(@Param("reporter") User reporter);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignee = :assignee")
    long countByAssignee(@Param("assignee") User assignee);
    
    @Query("SELECT t.status, COUNT(t) FROM Ticket t WHERE t.reporter = :reporter GROUP BY t.status")
    List<Object[]> countTicketsByStatusForReporter(@Param("reporter") User reporter);
    
    @Query("SELECT t.status, COUNT(t) FROM Ticket t WHERE t.assignee = :assignee GROUP BY t.status")
    List<Object[]> countTicketsByStatusForAssignee(@Param("assignee") User assignee);
    
    // Recent activity
    @Query("SELECT t FROM Ticket t WHERE t.updatedAt > :since ORDER BY t.updatedAt DESC")
    List<Ticket> findRecentlyUpdated(@Param("since") LocalDateTime since);
}