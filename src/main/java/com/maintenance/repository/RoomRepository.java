package com.maintenance.repository;

import com.maintenance.entity.Building;
import com.maintenance.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByBuilding(Building building);
    
    List<Room> findByBuildingAndFloorNumber(Building building, Integer floorNumber);
    
    List<Room> findByBuildingAndIsActiveTrue(Building building);
    
    @Query("SELECT r FROM Room r WHERE r.building = :building AND " +
           "(LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Room> searchRoomsByBuilding(@Param("building") Building building, @Param("search") String search);
    
    @Query("SELECT r FROM Room r WHERE r.building.id = :buildingId AND r.floorNumber = :floorNumber AND r.roomNumber = :roomNumber")
    List<Room> findByBuildingIdAndFloorNumberAndRoomNumber(@Param("buildingId") Long buildingId, 
                                                          @Param("floorNumber") Integer floorNumber, 
                                                          @Param("roomNumber") String roomNumber);
}
