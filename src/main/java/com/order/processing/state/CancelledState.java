package com.order.processing.state;

import com.order.processing.model.Order;

public class CancelledState implements OrderState {
    @Override
    public void processOrder(Order order) {
        // Final state - no further processing needed
    }

    @Override
    public boolean canTransitionTo(OrderState newState) {
        return false; // Cannot transition from cancelled state
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}