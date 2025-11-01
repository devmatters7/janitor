package com.maintenance.service;

import com.maintenance.entity.Building;
import com.maintenance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BuildingService {
    
    Building saveBuilding(Building building);
    
    Building updateBuilding(Long id, Building building);
    
    void deleteBuilding(Long id);
    
    Optional<Building> findById(Long id);
    
    List<Building> findAllBuildings();
    
    Page<Building> findAllBuildings(Pageable pageable);
    
    List<Building> findByManager(User manager);
    
    Page<Building> searchBuildings(String search, Pageable pageable);
    
    List<Building> findActiveBuildings();
    
    Page<Building> findActiveBuildings(Pageable pageable);
    
    long countAllBuildings();
    
    long countActiveBuildings();
}