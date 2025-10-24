package com.order.processing.service;

import com.order.processing.model.Order;
import com.order.processing.model.OrderItem;
import com.order.processing.state.CancelledState;
import com.order.processing.state.OrderStatus;
import com.order.processing.factory.OrderFactory;
import com.order.processing.observer.OrderObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final OrderFactory orderFactory;
    private final List<OrderObserver> observers = new ArrayList<>();

    public OrderService(OrderFactory orderFactory) {
        this.orderFactory = orderFactory;
    }

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public Order createOrder(List<OrderItem> items) {
        Order order = orderFactory.createOrder(items);
        orders.put(order.getId(), order);
        notifyObservers(order);
        return order;
    }

    public Optional<Order> getOrder(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orders.values().stream()
                    .filter(order -> order.getStatus() == status)
                    .toList();
    }

    public boolean cancelOrder(String orderId) {
        return getOrder(orderId).map(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setState(new CancelledState());
                notifyObservers(order);
                return true;
            }
            return false;
        }).orElse(false);
    }

    private void notifyObservers(Order order) {
        observers.forEach(observer -> observer.onOrderStatusChanged(order));
    }
}