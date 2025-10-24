package com.order.processing.state;

import com.order.processing.model.Order;

public class ShippedState implements OrderState {
    @Override
    public void processOrder(Order order) {
        // Logic for processing a shipped order
        // This would typically transition to DeliveredState
        order.setState(new DeliveredState());
    }

    @Override
    public boolean canTransitionTo(OrderState newState) {
        return newState instanceof DeliveredState;
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.SHIPPED;
    }
}