package com.maintenance.repository;

import com.maintenance.entity.Comment;
import com.maintenance.entity.Ticket;
import com.maintenance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByTicketOrderByCreatedAtDesc(Ticket ticket);
    
    List<Comment> findByAuthorOrderByCreatedAtDesc(User author);
    
    List<Comment> findByTicketAndIsInternalFalseOrderByCreatedAtDesc(Ticket ticket);
    
    @Query("SELECT c FROM Comment c WHERE c.ticket = :ticket AND c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByTicket(@Param("ticket") Ticket ticket, @Param("since") java.time.LocalDateTime since);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.ticket = :ticket")
    long countByTicket(@Param("ticket") Ticket ticket);
}
