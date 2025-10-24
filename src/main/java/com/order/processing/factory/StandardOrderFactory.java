package com.order.processing.factory;

import com.order.processing.model.Order;
import com.order.processing.model.OrderItem;
import com.order.processing.state.OrderState;
import com.order.processing.state.PendingState;

import java.math.BigDecimal;
import java.util.List;

public class StandardOrderFactory implements OrderFactory {
    @Override
    public Order createOrder(List<OrderItem> items) {
        return new Order(items, new PendingState());
    }

    @Override
    public OrderItem createOrderItem(String productId, int quantity, BigDecimal price) {
        return new OrderItem(productId, quantity, price);
    }
}