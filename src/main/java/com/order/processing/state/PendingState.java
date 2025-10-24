package com.order.processing.state;

import com.order.processing.model.Order;

public class PendingState implements OrderState {
    @Override
    public void processOrder(Order order) {
        // Logic for processing a pending order
        // This would typically transition to ProcessingState
        order.setState(new ProcessingState());
    }

    @Override
    public boolean canTransitionTo(OrderState newState) {
        return newState instanceof ProcessingState || 
               newState instanceof CancelledState;
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }
}