package com.order.processing.observer;

import com.order.processing.model.Order;
import com.order.processing.state.OrderStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingOrderProcessorTest {

    @Mock
    private ScheduledExecutorService executorService;

    @Mock
    private Order order;

    private PendingOrderProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PendingOrderProcessor(executorService);
    }

    @Test
    void onOrderStatusChanged_PendingOrder_ShouldScheduleProcessing() {
        // Arrange
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);

        // Act
        processor.onOrderStatusChanged(order);

        // Assert
        verify(executorService).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void onOrderStatusChanged_NonPendingOrder_ShouldNotScheduleProcessing() {
        // Arrange
        when(order.getStatus()).thenReturn(OrderStatus.PROCESSING);

        // Act
        processor.onOrderStatusChanged(order);

        // Assert
        verify(executorService, never()).schedule(any(Runnable.class), anyLong(), any());
    }

    @Test
    void processOrder_StillPending_ShouldProcess() {
        // Arrange
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);

        // Act
        processor.onOrderStatusChanged(order);

        // Capture and execute the Runnable
        verify(executorService).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.MINUTES));
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);
        
        // Execute the scheduled task
        processor.onOrderStatusChanged(order);

        // Assert
        verify(order, times(2)).getStatus();
    }

    @Test
    void processOrder_NoLongerPending_ShouldNotProcess() {
        // Arrange
        when(order.getStatus()).thenReturn(OrderStatus.PENDING)
                              .thenReturn(OrderStatus.PROCESSING);

        // Act
        processor.onOrderStatusChanged(order);

        // Assert
        verify(executorService).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.MINUTES));
        verify(order, never()).processOrder();
    }
}