package com.order.processing.model;

import com.order.processing.util.DebugLogger;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private final String productId;
    private final int quantity;
    private final BigDecimal pricePerUnit;
    private final BigDecimal totalPrice;

    public OrderItem(String productId, int quantity, BigDecimal pricePerUnit) {
        DebugLogger.log(DebugLogger.Category.MODEL, "OrderItem", 
            String.format("Creating item: %s (qty: %d, price: $%s)", 
                productId, quantity, pricePerUnit));
        
        if (productId == null || productId.trim().isEmpty()) {
            DebugLogger.log(DebugLogger.Category.ERROR, "OrderItem", 
                "Invalid product ID: null or empty");
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (quantity <= 0) {
            DebugLogger.log(DebugLogger.Category.ERROR, "OrderItem", 
                String.format("Invalid quantity for %s: %d (must be > 0)", productId, quantity));
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (pricePerUnit == null || pricePerUnit.compareTo(BigDecimal.ZERO) <= 0) {
            DebugLogger.log(DebugLogger.Category.ERROR, "OrderItem", 
                String.format("Invalid price for %s: %s (must be > 0)", productId, pricePerUnit));
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        
        this.productId = productId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
        
        DebugLogger.log(DebugLogger.Category.MODEL, "OrderItem", 
            String.format("Item created: %s, total price: $%s", productId, totalPrice));
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity &&
               Objects.equals(productId, orderItem.productId) &&
               Objects.equals(pricePerUnit, orderItem.pricePerUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, pricePerUnit);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
               "productId='" + productId + '\'' +
               ", quantity=" + quantity +
               ", pricePerUnit=" + pricePerUnit +
               ", totalPrice=" + totalPrice +
               '}';
    }
}