package com.order.processing.service;

import com.order.processing.factory.OrderFactory;
import com.order.processing.model.Order;
import com.order.processing.model.OrderItem;
import com.order.processing.observer.OrderObserver;
import com.order.processing.state.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderFactory orderFactory;

    @Mock
    private OrderObserver observer;

    @Mock
    private Order mockOrder;

    private OrderService orderService;
    private List<OrderItem> testItems;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderFactory);
        orderService.addObserver(observer);
        
        // Create test items
        testItems = Arrays.asList(
            new OrderItem("TEST-1", 2, new BigDecimal("10.00")),
            new OrderItem("TEST-2", 1, new BigDecimal("20.00"))
        );
    }

    @Test
    void createOrder_ShouldCreateAndNotifyObservers() {
        // Arrange
        when(orderFactory.createOrder(testItems)).thenReturn(mockOrder);
        when(mockOrder.getId()).thenReturn("test-order-id");

        // Act
        Order result = orderService.createOrder(testItems);

        // Assert
        assertNotNull(result);
        verify(orderFactory).createOrder(testItems);
        verify(observer).onOrderStatusChanged(mockOrder);
    }

    @Test
    void getOrder_ExistingOrder_ShouldReturnOrder() {
        // Arrange
        when(orderFactory.createOrder(testItems)).thenReturn(mockOrder);
        when(mockOrder.getId()).thenReturn("test-order-id");
        Order createdOrder = orderService.createOrder(testItems);

        // Act
        Optional<Order> result = orderService.getOrder("test-order-id");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockOrder, result.get());
    }

    @Test
    void getOrder_NonExistingOrder_ShouldReturnEmpty() {
        // Act
        Optional<Order> result = orderService.getOrder("non-existing-id");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void cancelOrder_PendingOrder_ShouldCancel() {
        // Arrange
        when(orderFactory.createOrder(testItems)).thenReturn(mockOrder);
        when(mockOrder.getId()).thenReturn("test-order-id");
        when(mockOrder.getStatus()).thenReturn(OrderStatus.PENDING);
        Order order = orderService.createOrder(testItems);

        // Act
        boolean result = orderService.cancelOrder("test-order-id");

        // Assert
        assertTrue(result);
        verify(observer, times(2)).onOrderStatusChanged(mockOrder);
    }

    @Test
    void cancelOrder_NonPendingOrder_ShouldNotCancel() {
        // Arrange
        when(orderFactory.createOrder(testItems)).thenReturn(mockOrder);
        when(mockOrder.getId()).thenReturn("test-order-id");
        when(mockOrder.getStatus()).thenReturn(OrderStatus.PROCESSING);
        Order order = orderService.createOrder(testItems);

        // Act
        boolean result = orderService.cancelOrder("test-order-id");

        // Assert
        assertFalse(result);
        verify(observer, times(1)).onOrderStatusChanged(mockOrder);
    }

    @Test
    void getOrdersByStatus_ShouldReturnMatchingOrders() {
        // Arrange
        when(orderFactory.createOrder(testItems)).thenReturn(mockOrder);
        when(mockOrder.getId()).thenReturn("test-order-id");
        when(mockOrder.getStatus()).thenReturn(OrderStatus.PENDING);
        orderService.createOrder(testItems);

        // Act
        List<Order> pendingOrders = orderService.getOrdersByStatus(OrderStatus.PENDING);
        List<Order> processingOrders = orderService.getOrdersByStatus(OrderStatus.PROCESSING);

        // Assert
        assertEquals(1, pendingOrders.size());
        assertTrue(processingOrders.isEmpty());
    }
}