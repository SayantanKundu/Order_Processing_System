package com.order.processing.state;

import com.order.processing.model.Order;
import com.order.processing.util.DebugLogger;

/**
 * State Pattern: PROCESSING state implementation.
 * 
 * Business Rules:
 * - Order is being prepared/processed
 * - Can ONLY transition to SHIPPED (cannot be cancelled once processing starts)
 * - This enforces the requirement: "cancel only if PENDING"
 */
public class ProcessingState implements OrderState {
    
    @Override
    public void processOrder(Order order) {
        // When processing an order in PROCESSING state, move it to SHIPPED
        DebugLogger.log(DebugLogger.Category.STATE, "ProcessingState.processOrder", 
            String.format("Order[%s] - Starting transition PROCESSING → SHIPPED", 
                order.getId().substring(0, 8)));
        
        System.out.println("Order " + order.getId() + 
                         " processing complete - Moving to SHIPPED state");
        order.setState(new ShippedState());
        
        DebugLogger.log(DebugLogger.Category.STATE, "ProcessingState.processOrder", 
            String.format("Order[%s] - Transition completed successfully", 
                order.getId().substring(0, 8)));
    }
    
    @Override
    public boolean canTransitionTo(OrderState newState) {
        // PROCESSING can ONLY transition to SHIPPED
        // CANNOT be cancelled once processing starts (business requirement)
        boolean allowed = newState instanceof ShippedState;
        
        String targetState = newState.getClass().getSimpleName();
        DebugLogger.log(DebugLogger.Category.STATE, "ProcessingState.canTransitionTo", 
            String.format("Checking PROCESSING → %s: %s", targetState, allowed ? "✓ ALLOWED" : "✗ DENIED"));
        
        return allowed;
    }
    
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PROCESSING;
    }
    
    @Override
    public String getDescription() {
        return "Order is being processed. Cannot be cancelled at this stage.";
    }
    
    @Override
    public String toString() {
        return "ProcessingState";
    }
}
