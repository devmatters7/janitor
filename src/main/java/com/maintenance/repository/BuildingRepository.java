package com.maintenance.repository;

import com.maintenance.entity.Building;
import com.maintenance.entity.User;
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
public interface BuildingRepository extends JpaRepository<Building, Long> {
    
    // Basic queries
    List<Building> findByIsActiveTrue();
    
    Page<Building> findByIsActiveTrue(Pageable pageable);
    
    List<Building> findByManager(User manager);
    
    @EntityGraph(attributePaths = {"manager", "rooms"})
    Optional<Building> findById(Long id);
    
    // Search functionality
    @Query("SELECT b FROM Building b WHERE " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.address) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.city) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Building> searchBuildings(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT b FROM Building b WHERE b.manager = :manager AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.address) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.city) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Building> searchBuildingsByManager(@Param("search") String search, @Param("manager") User manager, Pageable pageable);
    
    // Custom queries with joins
    @Query("SELECT b FROM Building b LEFT JOIN FETCH b.manager LEFT JOIN FETCH b.rooms WHERE b.id = :id")
    Optional<Building> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT b FROM Building b LEFT JOIN FETCH b.tickets WHERE b.id = :id")
    Optional<Building> findByIdWithTickets(@Param("id") Long id);
    
    // Statistics
    @Query("SELECT COUNT(b) FROM Building b WHERE b.isActive = true")
    long countActiveBuildings();
    
    @Query("SELECT b, COUNT(t) FROM Building b LEFT JOIN b.tickets t WHERE b.isActive = true GROUP BY b")
    List<Object[]> countTicketsByBuilding();
    
    @Query("SELECT b, COUNT(r) FROM Building b LEFT JOIN b.rooms r WHERE b.isActive = true GROUP BY b")
    List<Object[]> countRoomsByBuilding();
    
    // Buildings with active tickets
    @Query("SELECT DISTINCT b FROM Building b JOIN b.tickets t WHERE t.status IN ('OPEN', 'IN_PROGRESS') AND b.isActive = true")
    List<Building> findBuildingsWithActiveTickets();
    
    // Buildings by manager with active tickets
    @Query("SELECT DISTINCT b FROM Building b JOIN b.tickets t WHERE b.manager = :manager AND t.status IN ('OPEN', 'IN_PROGRESS') AND b.isActive = true")
    List<Building> findBuildingsWithActiveTicketsByManager(@Param("manager") User manager);
    
    // Find buildings with overdue tickets
    @Query("SELECT DISTINCT b FROM Building b JOIN b.tickets t WHERE t.status IN ('OPEN', 'IN_PROGRESS') AND t.estimatedCompletion < CURRENT_TIMESTAMP AND b.isActive = true")
    List<Building> findBuildingsWithOverdueTickets();
}