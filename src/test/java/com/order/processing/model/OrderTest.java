package com.order.processing.model;

import com.order.processing.state.OrderState;
import com.order.processing.state.PendingState;
import com.order.processing.state.ProcessingState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTest {

    @Mock
    private OrderState mockState;

    private Order order;
    private List<OrderItem> items;

    @BeforeEach
    void setUp() {
        items = Arrays.asList(
            new OrderItem("TEST-1", 2, new BigDecimal("10.00")),
            new OrderItem("TEST-2", 1, new BigDecimal("20.00"))
        );
        order = new Order(items, new PendingState());
    }

    @Test
    void constructor_ShouldInitializeCorrectly() {
        // Assert
        assertNotNull(order.getId());
        assertEquals(items, order.getItems());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getLastModifiedAt());
        assertEquals(new BigDecimal("40.00"), order.getTotalAmount());
    }

    @Test
    void setState_ValidTransition_ShouldChangeState() {
        // Arrange
        OrderState newState = new ProcessingState();
        when(mockState.canTransitionTo(newState)).thenReturn(true);
        order = new Order(items, mockState);

        // Act
        order.setState(newState);

        // Assert
        assertEquals(newState, order.getCurrentState());
    }

    @Test
    void setState_InvalidTransition_ShouldThrowException() {
        // Arrange
        OrderState newState = new ProcessingState();
        when(mockState.canTransitionTo(newState)).thenReturn(false);
        order = new Order(items, mockState);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> order.setState(newState));
    }

    @Test
    void processOrder_ShouldDelegateToState() {
        // Arrange
        order = new Order(items, mockState);

        // Act
        order.processOrder();

        // Assert
        verify(mockState).processOrder(order);
    }

    @Test
    void getItems_ShouldReturnUnmodifiableList() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, 
            () -> order.getItems().add(new OrderItem("TEST-3", 1, BigDecimal.ONE)));
    }
}