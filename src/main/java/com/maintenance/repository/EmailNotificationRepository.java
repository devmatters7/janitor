package com.maintenance.repository;

import com.maintenance.entity.EmailNotification;
import com.maintenance.entity.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {
    
    List<EmailNotification> findByStatusOrderByCreatedAtAsc(NotificationStatus status);
    
    List<EmailNotification> findByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
            NotificationStatus status, LocalDateTime date);
    
    @Query("SELECT e FROM EmailNotification e WHERE e.status = :status AND e.createdAt >= :since")
    List<EmailNotification> findByStatusAndCreatedAtAfter(
            @Param("status") NotificationStatus status, 
            @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(e) FROM EmailNotification e WHERE e.status = :status")
    long countByStatus(@Param("status") NotificationStatus status);
}
