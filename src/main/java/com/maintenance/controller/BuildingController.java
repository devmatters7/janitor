package com.maintenance.controller;

import com.maintenance.entity.Building;
import com.maintenance.entity.User;
import com.maintenance.service.BuildingService;
import com.maintenance.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Buildings", description = "Building management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BuildingController {
    
    private final BuildingService buildingService;
    private final UserService userService;
    
    @GetMapping
    @Operation(summary = "Get all buildings", description = "Retrieve all buildings with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Page<Building>> getAllBuildings(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching all buildings with pagination");
        Page<Building> buildings = buildingService.findAllBuildings(pageable);
        return ResponseEntity.ok(buildings);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get building by ID", description = "Retrieve a specific building by its ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Building> getBuildingById(
            @PathVariable Long id) {
        
        log.info("Fetching building with ID: {}", id);
        Optional<Building> building = buildingService.findById(id);
        
        return building.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create new building", description = "Create a new building")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Building> createBuilding(
            @Valid @RequestBody Building building) {
        
        log.info("Creating new building: {}", building.getName());
        Building createdBuilding = buildingService.saveBuilding(building);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBuilding);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update building", description = "Update an existing building")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Building> updateBuilding(
            @PathVariable Long id,
            @Valid @RequestBody Building buildingDetails) {
        
        log.info("Updating building with ID: {}", id);
        Building updatedBuilding = buildingService.updateBuilding(id, buildingDetails);
        return ResponseEntity.ok(updatedBuilding);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete building", description = "Delete a building (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBuilding(
            @PathVariable Long id) {
        
        log.info("Deleting building with ID: {}", id);
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search buildings", description = "Search buildings by name or address")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Page<Building>> searchBuildings(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Searching buildings with query: {}", query);
        Page<Building> buildings = buildingService.searchBuildings(query, pageable);
        return ResponseEntity.ok(buildings);
    }
    
    @GetMapping("/managed-by/{userId}")
    @Operation(summary = "Get buildings by manager", description = "Get buildings managed by a specific user")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<List<Building>> getBuildingsByManager(
            @PathVariable Long userId) {
        
        log.info("Fetching buildings managed by user: {}", userId);
        User manager = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Building> buildings = buildingService.findByManager(manager);
        return ResponseEntity.ok(buildings);
    }
}