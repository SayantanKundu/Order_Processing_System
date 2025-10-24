package com.order.processing;

import com.order.processing.command.CreateOrderCommand;
import com.order.processing.command.OrderCommand;
import com.order.processing.factory.OrderFactory;
import com.order.processing.factory.StandardOrderFactory;
import com.order.processing.model.OrderItem;
import com.order.processing.observer.PendingOrderProcessor;
import com.order.processing.service.OrderService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Initialize components
        OrderFactory factory = new StandardOrderFactory();
        OrderService orderService = new OrderService(factory);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        
        // Set up order processor
        PendingOrderProcessor processor = new PendingOrderProcessor(executor);
        orderService.addObserver(processor);

        // Create sample order
        OrderItem item1 = factory.createOrderItem("PROD-1", 2, new BigDecimal("29.99"));
        OrderItem item2 = factory.createOrderItem("PROD-2", 1, new BigDecimal("49.99"));
        
        // Create and execute order command
        OrderCommand createCommand = new CreateOrderCommand(orderService, Arrays.asList(item1, item2));
        createCommand.execute();

        // Wait for a while to see status changes
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(6));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Cleanup
        executor.shutdown();
    }
}