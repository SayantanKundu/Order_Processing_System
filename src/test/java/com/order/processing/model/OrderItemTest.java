package com.order.processing.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    void constructor_ShouldCalculateTotalPrice() {
        // Arrange & Act
        OrderItem item = new OrderItem("TEST-1", 2, new BigDecimal("10.00"));

        // Assert
        assertEquals(new BigDecimal("20.00"), item.getTotalPrice());
    }

    @Test
    void equals_SameValues_ShouldBeEqual() {
        // Arrange
        OrderItem item1 = new OrderItem("TEST-1", 2, new BigDecimal("10.00"));
        OrderItem item2 = new OrderItem("TEST-1", 2, new BigDecimal("10.00"));

        // Assert
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void equals_DifferentValues_ShouldNotBeEqual() {
        // Arrange
        OrderItem item1 = new OrderItem("TEST-1", 2, new BigDecimal("10.00"));
        OrderItem item2 = new OrderItem("TEST-2", 2, new BigDecimal("10.00"));

        // Assert
        assertNotEquals(item1, item2);
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void totalPrice_VariousQuantities_ShouldCalculateCorrectly(
            String productId, int quantity, BigDecimal price, BigDecimal expectedTotal) {
        // Arrange & Act
        OrderItem item = new OrderItem(productId, quantity, price);

        // Assert
        assertEquals(expectedTotal, item.getTotalPrice());
    }

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
            Arguments.of("TEST-1", 1, new BigDecimal("10.00"), new BigDecimal("10.00")),
            Arguments.of("TEST-2", 2, new BigDecimal("10.00"), new BigDecimal("20.00")),
            Arguments.of("TEST-3", 0, new BigDecimal("10.00"), new BigDecimal("0.00")),
            Arguments.of("TEST-4", 3, new BigDecimal("0.00"), new BigDecimal("0.00"))
        );
    }
}