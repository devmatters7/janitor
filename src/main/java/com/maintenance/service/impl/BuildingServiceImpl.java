package com.maintenance.service.impl;

import com.maintenance.entity.Building;
import com.maintenance.entity.User;
import com.maintenance.exception.ResourceNotFoundException;
import com.maintenance.repository.BuildingRepository;
import com.maintenance.service.BuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BuildingServiceImpl implements BuildingService {
    
    private final BuildingRepository buildingRepository;
    
    @Override
    @CacheEvict(value = "buildings", allEntries = true)
    public Building saveBuilding(Building building) {
        log.info("Saving new building: {}", building.getName());
        return buildingRepository.save(building);
    }
    
    @Override
    @CacheEvict(value = "buildings", allEntries = true)
    public Building updateBuilding(Long id, Building buildingDetails) {
        log.info("Updating building with id: {}", id);
        
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));
        
        building.setName(buildingDetails.getName());
        building.setAddress(buildingDetails.getAddress());
        building.setCity(buildingDetails.getCity());
        building.setState(buildingDetails.getState());
        building.setZipCode(buildingDetails.getZipCode());
        building.setManager(buildingDetails.getManager());
        
        return buildingRepository.save(building);
    }
    
    @Override
    @CacheEvict(value = "buildings", allEntries = true)
    public void deleteBuilding(Long id) {
        log.info("Deleting building with id: {}", id);
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));
        buildingRepository.delete(building);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "buildings", key = "#id")
    public Optional<Building> findById(Long id) {
        return buildingRepository.findByIdWithDetails(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Building> findAllBuildings() {
        return buildingRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Building> findAllBuildings(Pageable pageable) {
        return buildingRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Building> findByManager(User manager) {
        return buildingRepository.findByManager(manager);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Building> searchBuildings(String search, Pageable pageable) {
        return buildingRepository.searchBuildings(search, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Building> findActiveBuildings() {
        return buildingRepository.findByIsActiveTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Building> findActiveBuildings(Pageable pageable) {
        return buildingRepository.findByIsActiveTrue(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "buildingStats", key = "'countAll'")
    public long countAllBuildings() {
        return buildingRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "buildingStats", key = "'countActive'")
    public long countActiveBuildings() {
        return buildingRepository.countActiveBuildings();
    }
}