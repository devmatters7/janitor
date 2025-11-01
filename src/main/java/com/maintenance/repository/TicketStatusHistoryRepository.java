package com.maintenance.repository;

import com.maintenance.entity.Ticket;
import com.maintenance.entity.TicketStatusHistory;
import com.maintenance.entity.User;
import com.maintenance.entity.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketStatusHistoryRepository extends JpaRepository<TicketStatusHistory, Long> {
    
    List<TicketStatusHistory> findByTicketOrderByCreatedAtDesc(Ticket ticket);
    
    List<TicketStatusHistory> findByTicketAndChangedByOrderByCreatedAtDesc(Ticket ticket, User changedBy);
    
    @Query("SELECT h FROM TicketStatusHistory h WHERE h.ticket = :ticket AND h.createdAt >= :since ORDER BY h.createdAt DESC")
    List<TicketStatusHistory> findRecentHistoryByTicket(@Param("ticket") Ticket ticket, @Param("since") LocalDateTime since);
    
    @Query("SELECT h FROM TicketStatusHistory h WHERE h.changedBy = :user ORDER BY h.createdAt DESC")
    List<TicketStatusHistory> findByChangedByOrderByCreatedAtDesc(@Param("user") User user);
    
    @Query("SELECT h FROM TicketStatusHistory h WHERE h.ticket IN :tickets ORDER BY h.createdAt DESC")
    List<TicketStatusHistory> findByTicketsOrderByCreatedAtDesc(@Param("tickets") List<Ticket> tickets);
    
    @Query("SELECT h FROM TicketStatusHistory h WHERE h.oldStatus = :oldStatus AND h.newStatus = :newStatus ORDER BY h.createdAt DESC")
    List<TicketStatusHistory> findByStatusChange(@Param("oldStatus") TicketStatus oldStatus, @Param("newStatus") TicketStatus newStatus);
    
    @Query("SELECT COUNT(h) FROM TicketStatusHistory h WHERE h.ticket = :ticket")
    long countByTicket(@Param("ticket") Ticket ticket);
    
    @Query("SELECT h FROM TicketStatusHistory h WHERE h.ticket = :ticket AND h.createdAt = (SELECT MAX(h2.createdAt) FROM TicketStatusHistory h2 WHERE h2.ticket = :ticket)")
    Optional<TicketStatusHistory> findLatestHistoryByTicket(@Param("ticket") Ticket ticket);
    
    @Query("SELECT h FROM TicketStatusHistory h WHERE h.ticket = :ticket ORDER BY h.createdAt DESC")
    List<TicketStatusHistory> findByTicketOrderByCreatedAtDesc(@Param("ticket") Ticket ticket);
}