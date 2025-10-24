package com.order.processing.state;

import com.order.processing.model.Order;
import com.order.processing.util.DebugLogger;

/**
 * State Pattern: DELIVERED state implementation.
 * 
 * Business Rules:
 * - This is a FINAL state - order has been delivered successfully
 * - No transitions allowed from this state
 * - Cannot be cancelled or changed
 */
public class DeliveredState implements OrderState {
    
    @Override
    public void processOrder(Order order) {
        // Already delivered, no further processing needed
        DebugLogger.log(DebugLogger.Category.STATE, "DeliveredState.processOrder", 
            String.format("Order[%s] - Already in DELIVERED state (FINAL), no action needed", 
                order.getId().substring(0, 8)));
        
        System.out.println("Order " + order.getId() + 
                         " is already delivered. No further action needed.");
    }
    
    @Override
    public boolean canTransitionTo(OrderState newState) {
        // DELIVERED is a final state - no transitions allowed
        String targetState = newState.getClass().getSimpleName();
        DebugLogger.log(DebugLogger.Category.STATE, "DeliveredState.canTransitionTo", 
            String.format("Checking DELIVERED → %s: ✗ DENIED (FINAL STATE)", targetState));
        
        return false;
    }
    
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERED;
    }
    
    @Override
    public String getDescription() {
        return "Order has been delivered successfully. This is a final state.";
    }
    
    @Override
    public String toString() {
        return "DeliveredState";
    }
}
