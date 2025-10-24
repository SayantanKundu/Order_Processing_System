package com.order.processing.state;

import com.order.processing.model.Order;

public class DeliveredState implements OrderState {
    @Override
    public void processOrder(Order order) {
        // Final state - no further processing needed
    }

    @Override
    public boolean canTransitionTo(OrderState newState) {
        return false; // Cannot transition from delivered state
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERED;
    }
}