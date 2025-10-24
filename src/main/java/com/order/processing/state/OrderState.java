package com.order.processing.state;

import com.order.processing.model.Order;

public interface OrderState {
    void processOrder(Order order);
    boolean canTransitionTo(OrderState newState);
    OrderStatus getStatus();
}