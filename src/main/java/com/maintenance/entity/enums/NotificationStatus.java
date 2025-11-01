package com.maintenance.entity.enums;

public enum NotificationStatus {
    PENDING,    // Email is queued and waiting to be sent
    SENT,       // Email was successfully sent
    FAILED      // Email sending failed
}
