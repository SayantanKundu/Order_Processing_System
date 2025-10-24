package com.order.processing.factory;

import com.order.processing.model.Order;
import com.order.processing.model.OrderItem;
import com.order.processing.state.PendingState;
import com.order.processing.util.DebugLogger;

import java.math.BigDecimal;
import java.util.List;

/**
 * Factory Pattern: Standard implementation for creating orders.
 * 
 * This factory creates orders with:
 * - Automatically generated UUID
 * - Initial state: PENDING
 * - Current timestamp
 * - Validated items
 */
public class StandardOrderFactory implements OrderFactory {
    
    @Override
    public Order createOrder(List<OrderItem> items) {
        DebugLogger.log(DebugLogger.Category.FACTORY, "createOrder", 
            String.format("Received request to create order with %d items", 
                items != null ? items.size() : 0));
        
        if (items == null || items.isEmpty()) {
            DebugLogger.log(DebugLogger.Category.ERROR, "createOrder", 
                "Validation failed: items list is null or empty");
            throw new IllegalArgumentException("Cannot create order: items list is null or empty");
        }
        
        // Validate all items
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            if (item == null) {
                DebugLogger.log(DebugLogger.Category.ERROR, "createOrder", 
                    String.format("Validation failed: item at index %d is null", i));
                throw new IllegalArgumentException("Cannot create order: items list contains null item");
            }
            DebugLogger.log(DebugLogger.Category.FACTORY, "createOrder", 
                String.format("Item %d validated: %s (qty: %d, price: $%s)", 
                    i + 1, item.getProductId(), item.getQuantity(), item.getPricePerUnit()));
        }
        
        // Create order with PENDING state (initial state)
        DebugLogger.log(DebugLogger.Category.FACTORY, "createOrder", 
            "Creating Order object with PendingState");
        
        Order order = new Order(items, new PendingState());
        
        DebugLogger.logOrderCreation(order.getId(), 
            order.getItemCount(), 
            order.getTotalAmount().toString());
        
        System.out.println(String.format("✓ Created new order: %s with %d items (Total: $%s)",
                                        order.getId(),
                                        order.getItemCount(),
                                        order.getTotalAmount()));
        
        return order;
    }
    
    @Override
    public OrderItem createOrderItem(String productId, int quantity, BigDecimal pricePerUnit) {
        DebugLogger.log(DebugLogger.Category.FACTORY, "createOrderItem", 
            String.format("Creating OrderItem: product=%s, qty=%d, price=$%s", 
                productId, quantity, pricePerUnit));
        
        // OrderItem constructor handles validation
        OrderItem item = new OrderItem(productId, quantity, pricePerUnit);
        
        DebugLogger.log(DebugLogger.Category.FACTORY, "createOrderItem", 
            String.format("OrderItem created successfully: total=$%s", item.getTotalPrice()));
        
        return item;
    }
}

