package com.order.processing.state;

import com.order.processing.model.Order;

public class ProcessingState implements OrderState {
    @Override
    public void processOrder(Order order) {
        // Logic for processing an order in processing state
        // This would typically transition to ShippedState
        order.setState(new ShippedState());
    }

    @Override
    public boolean canTransitionTo(OrderState newState) {
        return newState instanceof ShippedState || 
               newState instanceof CancelledState;
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PROCESSING;
    }
}