package com.maintenance.repository;

import com.maintenance.entity.User;
import com.maintenance.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic queries
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    @EntityGraph(attributePaths = {"managedBuildings"})
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Role-based queries
    List<User> findByRole(Role role);
    
    Page<User> findByRole(Role role, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findActiveByRole(@Param("role") Role role);
    
    // Active users
    List<User> findByIsActiveTrue();
    
    Page<User> findByIsActiveTrue(Pageable pageable);
    
    // Search functionality
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsersList(@Param("search") String search);
    
    // Custom queries for specific use cases
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.managedBuildings WHERE u.id = :id")
    Optional<User> findByIdWithBuildings(@Param("id") Long id);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.reportedTickets WHERE u.id = :id")
    Optional<User> findByIdWithReportedTickets(@Param("id") Long id);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.assignedTickets WHERE u.id = :id")
    Optional<User> findByIdWithAssignedTickets(@Param("id") Long id);
    
    // Statistics
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
    
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.isActive = true GROUP BY u.role")
    List<Object[]> countUsersByRole();
    
    // Technicians available for assignment
    @Query("SELECT u FROM User u WHERE u.role = 'TECHNICIAN' AND u.isActive = true ORDER BY u.firstName, u.lastName")
    List<User> findAvailableTechnicians();
    
    // Managers
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' OR u.role = 'TECHNICIAN' AND u.isActive = true ORDER BY u.firstName, u.lastName")
    List<User> findManagers();
}