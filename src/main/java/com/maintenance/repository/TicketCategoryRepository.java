package com.maintenance.repository;

import com.maintenance.entity.TicketCategory;
import com.maintenance.entity.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
    
    Optional<TicketCategory> findByName(String name);
    
    List<TicketCategory> findByIsActiveTrue();
    
    List<TicketCategory> findByDefaultPriority(Priority defaultPriority);
}
