package com.maintenance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NotificationSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User is required")
    private User user;
    
    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications = true;
    
    @Column(name = "ticket_assigned", nullable = false)
    private Boolean ticketAssigned = true;
    
    @Column(name = "ticket_status_changed", nullable = false)
    private Boolean ticketStatusChanged = true;
    
    @Column(name = "ticket_comment_added", nullable = false)
    private Boolean ticketCommentAdded = true;
    
    @Column(name = "ticket_overdue", nullable = false)
    private Boolean ticketOverdue = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean shouldSendEmailForAssignment() {
        return emailNotifications && ticketAssigned;
    }
    
    public boolean shouldSendEmailForStatusChange() {
        return emailNotifications && ticketStatusChanged;
    }
    
    public boolean shouldSendEmailForComment() {
        return emailNotifications && ticketCommentAdded;
    }
    
    public boolean shouldSendEmailForOverdue() {
        return emailNotifications && ticketOverdue;
    }
}