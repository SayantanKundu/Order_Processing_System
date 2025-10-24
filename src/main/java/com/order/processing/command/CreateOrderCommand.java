package com.order.processing.command;

import com.order.processing.model.Order;
import com.order.processing.model.OrderItem;
import com.order.processing.service.OrderService;

import java.util.List;

public class CreateOrderCommand implements OrderCommand {
    private final OrderService orderService;
    private final List<OrderItem> items;
    private Order createdOrder;

    public CreateOrderCommand(OrderService orderService, List<OrderItem> items) {
        this.orderService = orderService;
        this.items = items;
    }

    @Override
    public void execute() {
        createdOrder = orderService.createOrder(items);
    }

    @Override
    public void undo() {
        if (createdOrder != null) {
            orderService.cancelOrder(createdOrder.getId());
        }
    }

    public String getCreatedOrderId() {
        return createdOrder != null ? createdOrder.getId() : null;
    }
}