package com.order.processing.state;

import com.order.processing.model.Order;

/**
 * State Pattern: Interface for all order states.
 * Each concrete state class defines its own behavior and valid transitions.
 */
public interface OrderState {
    
    /**
     * Process the order, potentially transitioning to the next state.
     * Each state defines what "processing" means for that state.
     * 
     * @param order The order to process
     */
    void processOrder(Order order);
    
    /**
     * Check if this state can transition to the new state.
     * Enforces business rules about valid state transitions.
     * 
     * @param newState The state to transition to
     * @return true if transition is allowed, false otherwise
     */
    boolean canTransitionTo(OrderState newState);
    
    /**
     * Get the status enum value for this state.
     * 
     * @return The OrderStatus enum value
     */
    OrderStatus getStatus();
    
    /**
     * Get a human-readable description of what happens in this state.
     * 
     * @return Description of the state
     */
    String getDescription();
}
