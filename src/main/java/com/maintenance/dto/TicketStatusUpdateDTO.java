package com.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusUpdateDTO {
    
    @NotBlank(message = "Status is required")
    private String status;
    
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}
