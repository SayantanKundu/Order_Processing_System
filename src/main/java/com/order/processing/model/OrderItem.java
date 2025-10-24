package com.order.processing.model;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private final String productId;
    private final int quantity;
    private final BigDecimal pricePerUnit;
    private final BigDecimal totalPrice;

    public OrderItem(String productId, int quantity, BigDecimal pricePerUnit) {
        this.productId = productId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
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