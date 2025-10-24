package com.order.processing.factory;

import com.order.processing.model.Order;
import com.order.processing.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * Factory Pattern: Interface for creating orders and order items.
 * 
 * Benefits:
 * - Centralizes object creation logic
 * - Ensures objects are created with proper initialization
 * - Easy to extend with different factory implementations
 */
public interface OrderFactory {
    
    /**
     * Create a new order with the given items.
     * The order will be created in its initial state (PENDING).
     * 
     * @param items List of order items
     * @return A new Order instance
     * @throws IllegalArgumentException if items is null or empty
     */
    Order createOrder(List<OrderItem> items);
    
    /**
     * Create a new order item.
     * 
     * @param productId Product identifier
     * @param quantity Quantity of the product
     * @param pricePerUnit Price per unit
     * @return A new OrderItem instance
     * @throws IllegalArgumentException if validation fails
     */
    OrderItem createOrderItem(String productId, int quantity, BigDecimal pricePerUnit);
}
