package com.maintenance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"building_id", "floor_number", "room_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    @NotNull(message = "Building is required")
    private Building building;
    
    @NotNull(message = "Floor number is required")
    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;
    
    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number must not exceed 20 characters")
    @Column(name = "room_number", nullable = false)
    private String roomNumber;
    
    @Size(max = 50, message = "Room type must not exceed 50 characters")
    @Column(name = "room_type")
    private String roomType;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Ticket> tickets = new ArrayList<>();
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Helper methods
    public String getFullRoomLocation() {
        return String.format("Floor %d - Room %s", floorNumber, roomNumber);
    }
    
    public String getBuildingAndRoom() {
        return String.format("%s - %s", building.getName(), getFullRoomLocation());
    }
    
    public int getActiveTicketCount() {
        return (int) tickets.stream()
                .filter(ticket -> ticket.getStatus() != com.maintenance.entity.enums.TicketStatus.COLOSED)
                .count();
    }
}