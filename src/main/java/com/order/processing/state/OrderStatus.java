package com.order.processing.state;

/**
 * Enum representing all possible order statuses in the system.
 * This works in conjunction with the State Pattern for status management.
 */
public enum OrderStatus {
    PENDING,      // Order created, awaiting processing
    PROCESSING,   // Order is being prepared
    SHIPPED,      // Order has been shipped
    DELIVERED,    // Order has been delivered to customer
    CANCELLED     // Order has been cancelled
}
