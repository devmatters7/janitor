package com.maintenance.repository;

import com.maintenance.entity.NotificationSettings;
import com.maintenance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {
    
    Optional<NotificationSettings> findByUser(User user);
    
    Optional<NotificationSettings> findByUserId(Long userId);
    
    List<NotificationSettings> findByEmailNotificationsTrue();
    
    List<NotificationSettings> findByTicketAssignedTrue();
}
