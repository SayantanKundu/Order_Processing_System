package com.order.processing.state;

import com.order.processing.model.Order;
import com.order.processing.util.DebugLogger;

/**
 * State Pattern: CANCELLED state implementation.
 * 
 * Business Rules:
 * - This is a FINAL state - order has been cancelled
 * - Can only be reached from PENDING state (business requirement)
 * - No transitions allowed from this state
 */
public class CancelledState implements OrderState {
    
    @Override
    public void processOrder(Order order) {
        // Cancelled orders cannot be processed
        DebugLogger.log(DebugLogger.Category.STATE, "CancelledState.processOrder", 
            String.format("Order[%s] - Already in CANCELLED state (FINAL), no action allowed", 
                order.getId().substring(0, 8)));
        
        System.out.println("Order " + order.getId() + 
                         " is cancelled. No processing allowed.");
    }
    
    @Override
    public boolean canTransitionTo(OrderState newState) {
        // CANCELLED is a final state - no transitions allowed
        String targetState = newState.getClass().getSimpleName();
        DebugLogger.log(DebugLogger.Category.STATE, "CancelledState.canTransitionTo", 
            String.format("Checking CANCELLED → %s: ✗ DENIED (FINAL STATE)", targetState));
        
        return false;
    }
    
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
    
    @Override
    public String getDescription() {
        return "Order has been cancelled. This is a final state.";
    }
    
    @Override
    public String toString() {
        return "CancelledState";
    }
}
