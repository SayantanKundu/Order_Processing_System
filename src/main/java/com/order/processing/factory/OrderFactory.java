package com.order.processing.factory;

import com.order.processing.model.Order;
import com.order.processing.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public interface OrderFactory {
    Order createOrder(List<OrderItem> items);
    OrderItem createOrderItem(String productId, int quantity, BigDecimal price);
}