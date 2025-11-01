package com.maintenance.entity.enums;

public enum TicketStatus {
    OPEN,         // Ticket created and waiting for assignment
    IN_PROGRESS,  // Ticket assigned and being worked on
    ON_HOLD,      // Ticket temporarily paused
    RESOLVED,     // Issue has been resolved
    CLOSED        // Ticket is completely closed
}