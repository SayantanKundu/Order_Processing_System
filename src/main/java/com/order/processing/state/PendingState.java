package com.order.processing.state;

import com.order.processing.model.Order;
import com.order.processing.util.DebugLogger;

/**
 * State Pattern: PENDING state implementation.
 * 
 * Business Rules:
 * - This is the initial state when an order is created
 * - Can transition to PROCESSING (after 5 minutes or manual processing)
 * - Can transition to CANCELLED (customer cancels)
 * - Cannot transition to SHIPPED or DELIVERED directly
 */
public class PendingState implements OrderState {
    
    @Override
    public void processOrder(Order order) {
        // When processing a pending order, move it to PROCESSING state
        DebugLogger.log(DebugLogger.Category.STATE, "PendingState.processOrder", 
            String.format("Order[%s] - Starting transition PENDING → PROCESSING", 
                order.getId().substring(0, 8)));
        
        System.out.println("Processing pending order " + order.getId() + 
                         " - Moving to PROCESSING state");
        order.setState(new ProcessingState());
        
        DebugLogger.log(DebugLogger.Category.STATE, "PendingState.processOrder", 
            String.format("Order[%s] - Transition completed successfully", 
                order.getId().substring(0, 8)));
    }
    
    @Override
    public boolean canTransitionTo(OrderState newState) {
        // PENDING can only transition to PROCESSING or CANCELLED
        boolean allowed = newState instanceof ProcessingState || 
                         newState instanceof CancelledState;
        
        String targetState = newState.getClass().getSimpleName();
        DebugLogger.log(DebugLogger.Category.STATE, "PendingState.canTransitionTo", 
            String.format("Checking PENDING → %s: %s", targetState, allowed ? "✓ ALLOWED" : "✗ DENIED"));
        
        return allowed;
    }
    
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }
    
    @Override
    public String getDescription() {
        return "Order is pending and awaiting processing. Can be cancelled by customer.";
    }
    
    @Override
    public String toString() {
        return "PendingState";
    }
}
