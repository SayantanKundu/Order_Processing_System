package com.order.processing.model;

import com.order.processing.state.OrderState;
import com.order.processing.state.OrderStatus;
import com.order.processing.util.DebugLogger;

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
        OrderStatus oldStatus = currentState.getStatus();
        
        DebugLogger.log(DebugLogger.Category.MODEL, "Order.setState", 
            String.format("Order[%s] - Attempting state change: %s → %s", 
                id.substring(0, 8), oldStatus, newState.getStatus()));
        
        if (currentState.canTransitionTo(newState)) {
            this.currentState = newState;
            this.lastModifiedAt = LocalDateTime.now();
            
            DebugLogger.logStateTransition(id, oldStatus.name(), newState.getStatus().name(), true);
            DebugLogger.log(DebugLogger.Category.MODEL, "Order.setState", 
                String.format("Order[%s] - State changed successfully", id.substring(0, 8)));
        } else {
            DebugLogger.logStateTransition(id, oldStatus.name(), newState.getStatus().name(), false);
            DebugLogger.log(DebugLogger.Category.ERROR, "Order.setState", 
                String.format("Order[%s] - Invalid state transition blocked", id.substring(0, 8)));
            
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
    
    public int getItemCount() {
        return items.size();
    }
    
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append(String.format("Order ID: %s\n", id));
        sb.append(String.format("Status: %s\n", getStatus()));
        sb.append(String.format("Created: %s\n", createdAt));
        sb.append(String.format("Last Modified: %s\n", lastModifiedAt));
        sb.append("\nItems:\n");
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            sb.append(String.format("  %d. %s × %d @ $%s = $%s\n",
                i + 1,
                item.getProductId(),
                item.getQuantity(),
                item.getPricePerUnit(),
                item.getTotalPrice()));
        }
        sb.append(String.format("\nTotal Amount: $%s\n", totalAmount));
        sb.append("═══════════════════════════════════════════════");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Order[id=%s, status=%s, items=%d, total=$%s]",
            id.substring(0, 8), getStatus(), items.size(), totalAmount);
    }
}