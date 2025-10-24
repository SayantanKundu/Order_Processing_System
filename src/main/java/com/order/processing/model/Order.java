package com.order.processing.model;

import com.order.processing.state.OrderState;
import com.order.processing.state.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {
    private final String id;
    private final List<OrderItem> items;
    private final LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private OrderState currentState;
    private final BigDecimal totalAmount;

    public Order(List<OrderItem> items, OrderState initialState) {
        this.id = UUID.randomUUID().toString();
        this.items = new ArrayList<>(items);
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = this.createdAt;
        this.currentState = initialState;
        this.totalAmount = calculateTotalAmount();
    }

    private BigDecimal calculateTotalAmount() {
        return items.stream()
                   .map(OrderItem::getTotalPrice)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void setState(OrderState newState) {
        if (currentState.canTransitionTo(newState)) {
            this.currentState = newState;
            this.lastModifiedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException(
                "Cannot transition from " + currentState.getStatus() + 
                " to " + newState.getStatus()
            );
        }
    }

    public void processOrder() {
        currentState.processOrder(this);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public OrderState getCurrentState() {
        return currentState;
    }

    public OrderStatus getStatus() {
        return currentState.getStatus();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}