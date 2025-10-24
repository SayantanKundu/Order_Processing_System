package com.order.processing.observer;

import com.order.processing.model.Order;
import com.order.processing.state.OrderStatus;
import com.order.processing.util.DebugLogger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Observer Pattern: Watches for PENDING orders and schedules automatic processing.
 * 
 * Business Requirement:
 * "A background job should automatically update PENDING orders to PROCESSING every 5 minutes."
 * 
 * Implementation:
 * - When an order is created in PENDING state, this observer is notified
 * - It schedules a task to run after 5 minutes
 * - After 5 minutes, it checks if order is still PENDING and processes it
 */
public class PendingOrderProcessor implements OrderObserver {
    
    private final ScheduledExecutorService executorService;
    private final long delayMinutes;
    
    /**
     * Create a new processor with default 5-minute delay.
     * 
     * @param executorService The executor service for scheduling tasks
     */
    public PendingOrderProcessor(ScheduledExecutorService executorService) {
        this(executorService, 5);
    }
    
    /**
     * Create a new processor with custom delay (useful for testing).
     * 
     * @param executorService The executor service for scheduling tasks
     * @param delayMinutes Minutes to wait before processing
     */
    public PendingOrderProcessor(ScheduledExecutorService executorService, long delayMinutes) {
        if (executorService == null) {
            throw new IllegalArgumentException("ExecutorService cannot be null");
        }
        if (delayMinutes < 0) {
            throw new IllegalArgumentException("Delay minutes must be non-negative");
        }
        
        this.executorService = executorService;
        this.delayMinutes = delayMinutes;
    }
    
    @Override
    public void onOrderStatusChanged(Order order) {
        DebugLogger.log(DebugLogger.Category.OBSERVER, "onOrderStatusChanged", 
            String.format("Order[%s] notification received with status: %s", 
                order.getId().substring(0, 8), order.getStatus()));
        
        // Only schedule processing for PENDING orders
        if (order.getStatus() == OrderStatus.PENDING) {
            DebugLogger.logObserverNotification(order.getId(), 
                order.getStatus().name(), 
                "Scheduling processing in " + delayMinutes + " minutes");
            scheduleProcessing(order);
        } else {
            DebugLogger.log(DebugLogger.Category.OBSERVER, "onOrderStatusChanged", 
                String.format("Order[%s] is not PENDING, no action taken", 
                    order.getId().substring(0, 8)));
        }
    }
    
    /**
     * Schedule automatic processing of the order after the configured delay.
     */
    private void scheduleProcessing(Order order) {
        DebugLogger.log(DebugLogger.Category.OBSERVER, "scheduleProcessing", 
            String.format("Order[%s] - Creating scheduled task for %d minutes from now", 
                order.getId().substring(0, 8), delayMinutes));
        
        System.out.println(String.format(
            "⏰ Scheduled automatic processing for order %s in %d minute(s)",
            order.getId(), delayMinutes
        ));
        
        // Schedule the processing task
        executorService.schedule(
            () -> processOrder(order),
            delayMinutes,
            TimeUnit.MINUTES
        );
        
        DebugLogger.log(DebugLogger.Category.OBSERVER, "scheduleProcessing", 
            String.format("Order[%s] - Task scheduled successfully", 
                order.getId().substring(0, 8)));
    }
    
    /**
     * Process the order if it's still in PENDING state.
     * This is the task that runs after the delay.
     */
    private void processOrder(Order order) {
        DebugLogger.log(DebugLogger.Category.OBSERVER, "processOrder", 
            String.format("Order[%s] - Scheduled task triggered, checking current status", 
                order.getId().substring(0, 8)));
        
        try {
            // Check if order is still PENDING (might have been cancelled)
            if (order.getStatus() == OrderStatus.PENDING) {
                DebugLogger.log(DebugLogger.Category.OBSERVER, "processOrder", 
                    String.format("Order[%s] - Status is still PENDING, proceeding with auto-processing", 
                        order.getId().substring(0, 8)));
                
                System.out.println(String.format(
                    "🔄 Auto-processing order %s (PENDING → PROCESSING)",
                    order.getId()
                ));
                
                // Process the order (State Pattern handles the transition)
                order.processOrder();
                
                DebugLogger.log(DebugLogger.Category.OBSERVER, "processOrder", 
                    String.format("Order[%s] - Auto-processing completed successfully", 
                        order.getId().substring(0, 8)));
                
                System.out.println(String.format(
                    "✓ Order %s successfully moved to PROCESSING state",
                    order.getId()
                ));
            } else {
                DebugLogger.log(DebugLogger.Category.OBSERVER, "processOrder", 
                    String.format("Order[%s] - Status changed to %s, skipping auto-processing", 
                        order.getId().substring(0, 8), order.getStatus()));
                
                System.out.println(String.format(
                    "⊘ Order %s is no longer PENDING (current status: %s), skipping auto-processing",
                    order.getId(), order.getStatus()
                ));
            }
        } catch (Exception e) {
            DebugLogger.log(DebugLogger.Category.ERROR, "processOrder", 
                String.format("Order[%s] - Exception during auto-processing: %s", 
                    order.getId().substring(0, 8), e.getMessage()));
            
            System.err.println(String.format(
                "✗ Error auto-processing order %s: %s",
                order.getId(), e.getMessage()
            ));
        }
    }
    
    /**
     * Shutdown the executor service gracefully.
     * Should be called when the application is shutting down.
     */
    public void shutdown() {
        System.out.println("Shutting down PendingOrderProcessor...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
