package com.maintenance.service;

import com.maintenance.entity.User;
import com.maintenance.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    
    // Basic CRUD operations
    User saveUser(User user);
    
    User updateUser(Long id, User user);
    
    void deleteUser(Long id);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    // List operations
    List<User> findAllUsers();
    
    Page<User> findAllUsers(Pageable pageable);
    
    List<User> findUsersByRole(Role role);
    
    Page<User> findUsersByRole(Role role, Pageable pageable);
    
    List<User> findActiveUsers();
    
    Page<User> findActiveUsers(Pageable pageable);
    
    // Search functionality
    List<User> searchUsers(String search);
    
    Page<User> searchUsers(String search, Pageable pageable);
    
    // Specific user types
    List<User> findAvailableTechnicians();
    
    List<User> findManagers();
    
    // Statistics
    long countAllUsers();
    
    long countUsersByRole(Role role);
    
    long countActiveUsers();
    
    // Validation
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Password management
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    void resetPassword(Long userId, String newPassword);
    
    // Account management
    void activateUser(Long userId);
    
    void deactivateUser(Long userId);
}