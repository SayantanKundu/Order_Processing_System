package com.order.processing.service;

import com.order.processing.model.Order;
import com.order.processing.model.OrderItem;
import com.order.processing.state.CancelledState;
import com.order.processing.state.OrderStatus;
import com.order.processing.factory.OrderFactory;
import com.order.processing.observer.OrderObserver;
import com.order.processing.util.DebugLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final OrderFactory orderFactory;
    private final List<OrderObserver> observers = new ArrayList<>();

    public OrderService(OrderFactory orderFactory) {
        this.orderFactory = orderFactory;
        DebugLogger.log(DebugLogger.Category.SERVICE, "OrderService", 
            "Service initialized with " + orderFactory.getClass().getSimpleName());
    }

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
        DebugLogger.log(DebugLogger.Category.SERVICE, "addObserver", 
            "Registered observer: " + observer.getClass().getSimpleName());
    }

    public Order createOrder(List<OrderItem> items) {
        DebugLogger.section("CREATE ORDER REQUEST");
        DebugLogger.log(DebugLogger.Category.SERVICE, "createOrder", 
            String.format("Received request with %d items", items != null ? items.size() : 0));
        
        Order order = orderFactory.createOrder(items);
        
        DebugLogger.logServiceOperation("createOrder", order.getId(), 
            String.format("Storing in ConcurrentHashMap (total orders before: %d)", orders.size()));
        orders.put(order.getId(), order);
        DebugLogger.logServiceOperation("createOrder", order.getId(), 
            String.format("Stored successfully (total orders now: %d)", orders.size()));
        
        notifyObservers(order);
        
        DebugLogger.log(DebugLogger.Category.SERVICE, "createOrder", 
            String.format("Order[%s] creation complete", order.getId().substring(0, 8)));
        
        return order;
    }

    public Optional<Order> getOrder(String orderId) {
        if (orderId == null) {
            DebugLogger.log(DebugLogger.Category.ERROR, "getOrder", "Order ID is null");
            return Optional.empty();
        }
        
        DebugLogger.log(DebugLogger.Category.SERVICE, "getOrder", 
            String.format("Looking up Order[%s]", orderId.substring(0, 8)));
        
        Optional<Order> result = Optional.ofNullable(orders.get(orderId));
        
        if (result.isPresent()) {
            DebugLogger.log(DebugLogger.Category.SERVICE, "getOrder", 
                String.format("Order[%s] found with status: %s", 
                    orderId.substring(0, 8), result.get().getStatus()));
        } else {
            DebugLogger.log(DebugLogger.Category.SERVICE, "getOrder", 
                String.format("Order[%s] not found", orderId.substring(0, 8)));
        }
        
        return result;
    }

    public List<Order> getAllOrders() {
        DebugLogger.log(DebugLogger.Category.SERVICE, "getAllOrders", 
            String.format("Retrieving all orders (total: %d)", orders.size()));
        return new ArrayList<>(orders.values());
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        DebugLogger.log(DebugLogger.Category.SERVICE, "getOrdersByStatus", 
            String.format("Filtering orders by status: %s", status));
        
        List<Order> filtered = orders.values().stream()
                    .filter(order -> order.getStatus() == status)
                    .toList();
        
        DebugLogger.log(DebugLogger.Category.SERVICE, "getOrdersByStatus", 
            String.format("Found %d order(s) with status %s", filtered.size(), status));
        
        return filtered;
    }

    public boolean cancelOrder(String orderId) {
        DebugLogger.section("CANCEL ORDER REQUEST");
        
        if (orderId == null) {
            DebugLogger.log(DebugLogger.Category.ERROR, "cancelOrder", "Order ID is null");
            return false;
        }
        
        DebugLogger.logServiceOperation("cancelOrder", orderId, "Cancel request received");
        
        return getOrder(orderId).map(order -> {
            OrderStatus currentStatus = order.getStatus();
            DebugLogger.logServiceOperation("cancelOrder", orderId, 
                String.format("Current status: %s", currentStatus));
            
            if (currentStatus == OrderStatus.PENDING) {
                DebugLogger.logServiceOperation("cancelOrder", orderId, 
                    "Status is PENDING - cancellation allowed");
                
                order.setState(new CancelledState());
                notifyObservers(order);
                
                DebugLogger.logStateTransition(orderId, "PENDING", "CANCELLED", true);
                DebugLogger.logServiceOperation("cancelOrder", orderId, "Cancellation successful");
                return true;
            }
            
            DebugLogger.logServiceOperation("cancelOrder", orderId, 
                String.format("Status is %s - cancellation DENIED (only PENDING orders can be cancelled)", currentStatus));
            return false;
        }).orElseGet(() -> {
            DebugLogger.log(DebugLogger.Category.ERROR, "cancelOrder", 
                String.format("Order[%s] not found", orderId.substring(0, 8)));
            return false;
        });
    }

    private void notifyObservers(Order order) {
        DebugLogger.log(DebugLogger.Category.SERVICE, "notifyObservers", 
            String.format("Notifying %d observer(s) about Order[%s]", 
                observers.size(), order.getId().substring(0, 8)));
        
        for (OrderObserver observer : observers) {
            DebugLogger.log(DebugLogger.Category.SERVICE, "notifyObservers", 
                String.format("Calling %s.onOrderStatusChanged()", observer.getClass().getSimpleName()));
            observer.onOrderStatusChanged(order);
        }
    }
    
    public int getOrderCount() {
        return orders.size();
    }
    
    public static class OrderStatistics {
        private final int totalOrders;
        private final int pendingOrders;
        private final int processingOrders;
        private final int shippedOrders;
        private final int deliveredOrders;
        private final int cancelledOrders;
        
        public OrderStatistics(int totalOrders, int pendingOrders, int processingOrders,
                             int shippedOrders, int deliveredOrders, int cancelledOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.processingOrders = processingOrders;
            this.shippedOrders = shippedOrders;
            this.deliveredOrders = deliveredOrders;
            this.cancelledOrders = cancelledOrders;
        }
        
        public int getTotalOrders() { return totalOrders; }
        public int getPendingOrders() { return pendingOrders; }
        public int getProcessingOrders() { return processingOrders; }
        public int getShippedOrders() { return shippedOrders; }
        public int getDeliveredOrders() { return deliveredOrders; }
        public int getCancelledOrders() { return cancelledOrders; }
        
        @Override
        public String toString() {
            return String.format(
                "Statistics: Total=%d, Pending=%d, Processing=%d, Shipped=%d, Delivered=%d, Cancelled=%d",
                totalOrders, pendingOrders, processingOrders, shippedOrders, deliveredOrders, cancelledOrders
            );
        }
    }
    
    public OrderStatistics getStatistics() {
        int total = orders.size();
        int pending = (int) orders.values().stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        int processing = (int) orders.values().stream().filter(o -> o.getStatus() == OrderStatus.PROCESSING).count();
        int shipped = (int) orders.values().stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        int delivered = (int) orders.values().stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        int cancelled = (int) orders.values().stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        
        return new OrderStatistics(total, pending, processing, shipped, delivered, cancelled);
    }
}