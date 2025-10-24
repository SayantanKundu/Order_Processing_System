package com.order.processing.state;

import com.order.processing.model.Order;
import com.order.processing.util.DebugLogger;

/**
 * State Pattern: SHIPPED state implementation.
 * 
 * Business Rules:
 * - Order has been shipped to customer
 * - Can ONLY transition to DELIVERED
 * - Cannot be cancelled (already shipped)
 */
public class ShippedState implements OrderState {
    
    @Override
    public void processOrder(Order order) {
        // When processing a shipped order, mark it as delivered
        DebugLogger.log(DebugLogger.Category.STATE, "ShippedState.processOrder", 
            String.format("Order[%s] - Starting transition SHIPPED → DELIVERED", 
                order.getId().substring(0, 8)));
        
        System.out.println("Order " + order.getId() + 
                         " has been delivered - Moving to DELIVERED state");
        order.setState(new DeliveredState());
        
        DebugLogger.log(DebugLogger.Category.STATE, "ShippedState.processOrder", 
            String.format("Order[%s] - Transition completed successfully", 
                order.getId().substring(0, 8)));
    }
    
    @Override
    public boolean canTransitionTo(OrderState newState) {
        // SHIPPED can ONLY transition to DELIVERED
        boolean allowed = newState instanceof DeliveredState;
        
        String targetState = newState.getClass().getSimpleName();
        DebugLogger.log(DebugLogger.Category.STATE, "ShippedState.canTransitionTo", 
            String.format("Checking SHIPPED → %s: %s", targetState, allowed ? "✓ ALLOWED" : "✗ DENIED"));
        
        return allowed;
    }
    
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.SHIPPED;
    }
    
    @Override
    public String getDescription() {
        return "Order has been shipped and is in transit to customer.";
    }
    
    @Override
    public String toString() {
        return "ShippedState";
    }
}
