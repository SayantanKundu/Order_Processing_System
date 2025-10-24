package com.order.processing.observer;

import com.order.processing.model.Order;

/**
 * Observer Pattern: Interface for objects that want to be notified about order changes.
 * 
 * Benefits:
 * - Decouples order management from order processing
 * - Easy to add new observers (e.g., email notifications, inventory updates)
 * - Supports multiple observers watching the same events
 */
public interface OrderObserver {
    
    /**
     * Called when an order's status changes.
     * Observers can react to specific status changes.
     * 
     * @param order The order that changed
     */
    void onOrderStatusChanged(Order order);
}
