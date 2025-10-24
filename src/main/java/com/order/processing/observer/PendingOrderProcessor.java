package com.order.processing.observer;

import com.order.processing.model.Order;
import com.order.processing.state.OrderStatus;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PendingOrderProcessor implements OrderObserver {
    private final ScheduledExecutorService executor;

    public PendingOrderProcessor(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void onOrderStatusChanged(Order order) {
        if (order.getStatus() == OrderStatus.PENDING) {
            // Schedule processing after 5 minutes
            executor.schedule(
                () -> processOrder(order),
                5,
                TimeUnit.MINUTES
            );
        }
    }

    private void processOrder(Order order) {
        if (order.getStatus() == OrderStatus.PENDING) {
            order.processOrder();
        }
    }
}