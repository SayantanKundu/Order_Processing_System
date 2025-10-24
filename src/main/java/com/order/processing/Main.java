package com.order.processing;

import com.order.processing.factory.OrderFactory;
import com.order.processing.factory.StandardOrderFactory;
import com.order.processing.model.Order;
import com.order.processing.model.OrderItem;
import com.order.processing.observer.PendingOrderProcessor;
import com.order.processing.service.OrderService;
import com.order.processing.state.OrderStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main application demonstrating the Order Processing System.
 * 
 * This demonstrates all requirements:
 * 1. Create an order with multiple items
 * 2. Retrieve order details by ID
 * 3. Update order status (automatic PENDING → PROCESSING after 5 minutes)
 * 4. List all orders with optional status filtering
 * 5. Cancel an order (only if PENDING)
 */
public class Main {
    
    private static OrderService orderService;
    private static OrderFactory orderFactory;
    private static PendingOrderProcessor pendingProcessor;
    private static ScheduledExecutorService executorService;
    
    public static void main(String[] args) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   E-COMMERCE ORDER PROCESSING SYSTEM                     ║");
        System.out.println("║   Demonstrating Design Patterns                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
        
        // Initialize system
        initializeSystem();
        
        // Run demonstration mode or interactive mode
        if (args.length > 0 && args[0].equals("--demo")) {
            runDemonstrationMode();
        } else if (args.length > 0 && args[0].equals("--quick-demo")) {
            runQuickDemo();
        } else {
            runInteractiveMode();
        }
        
        // Cleanup
        cleanup();
    }
    
    /**
     * Initialize the system with all components.
     */
    private static void initializeSystem() {
        System.out.println("Initializing Order Processing System...\n");
        
        // Create factory (Factory Pattern)
        orderFactory = new StandardOrderFactory();
        System.out.println("✓ Factory initialized");
        
        // Create service
        orderService = new OrderService(orderFactory);
        System.out.println("✓ OrderService initialized");
        
        // Create executor for background processing
        executorService = Executors.newScheduledThreadPool(2);
        System.out.println("✓ Background executor initialized");
        
        // Create and register observer (Observer Pattern)
        // Using 1 minute for demo instead of 5 minutes
        pendingProcessor = new PendingOrderProcessor(executorService, 1);
        orderService.addObserver(pendingProcessor);
        System.out.println("✓ Background processor registered (1-minute delay for demo)");
        
        System.out.println("\n✓ System initialization complete!\n");
    }
    
    /**
     * Run a quick demonstration of all features.
     */
    private static void runQuickDemo() {
        System.out.println("═════════════════════════════════════════════════════════════");
        System.out.println("                    QUICK DEMO MODE");
        System.out.println("═════════════════════════════════════════════════════════════\n");
        
        // 1. Create an order
        System.out.println("DEMO 1: Creating an order with multiple items");
        List<OrderItem> items = Arrays.asList(
            orderFactory.createOrderItem("LAPTOP-001", 1, new BigDecimal("999.99")),
            orderFactory.createOrderItem("MOUSE-001", 2, new BigDecimal("29.99"))
        );
        Order order = orderService.createOrder(items);
        System.out.println(order.toDetailedString());
        
        // 2. Retrieve order
        System.out.println("\nDEMO 2: Retrieving order by ID");
        Optional<Order> retrieved = orderService.getOrder(order.getId());
        if (retrieved.isPresent()) {
            System.out.println("✓ Order retrieved successfully");
            System.out.println("  Status: " + retrieved.get().getStatus());
        }
        
        // 3. Try to cancel (should succeed - PENDING)
        System.out.println("\nDEMO 3: Cancelling a PENDING order");
        boolean cancelled = orderService.cancelOrder(order.getId());
        System.out.println("Result: " + (cancelled ? "SUCCESS" : "FAILED"));
        
        // 4. Create another order and wait for auto-processing
        System.out.println("\nDEMO 4: Testing automatic processing");
        List<OrderItem> items2 = Arrays.asList(
            orderFactory.createOrderItem("PHONE-001", 1, new BigDecimal("699.99"))
        );
        Order order2 = orderService.createOrder(items2);
        System.out.println("Created order: " + order2.getId());
        System.out.println("Waiting for automatic processing (1 minute)...");
        
        try {
            Thread.sleep(65000); // Wait 65 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\nChecking order status after wait:");
        orderService.getOrder(order2.getId()).ifPresent(o -> {
            System.out.println("Order status: " + o.getStatus());
        });
        
        // 5. Try to cancel processed order (should fail)
        System.out.println("\nDEMO 5: Trying to cancel a PROCESSING order");
        boolean cancelled2 = orderService.cancelOrder(order2.getId());
        System.out.println("Result: " + (cancelled2 ? "SUCCESS" : "FAILED (as expected)"));
        
        // 6. List orders
        System.out.println("\nDEMO 6: Listing all orders");
        displayAllOrders();
        
        System.out.println("\n═════════════════════════════════════════════════════════════");
        System.out.println("                    DEMO COMPLETE");
        System.out.println("═════════════════════════════════════════════════════════════\n");
    }
    
    /**
     * Run full demonstration mode with explanations.
     */
    private static void runDemonstrationMode() {
        System.out.println("═════════════════════════════════════════════════════════════");
        System.out.println("              FULL DEMONSTRATION MODE");
        System.out.println("   This demonstrates all requirements and design patterns");
        System.out.println("═════════════════════════════════════════════════════════════\n");
        
        pause("Press Enter to start demonstration...");
        
        demonstrateRequirement1();
        demonstrateRequirement2();
        demonstrateRequirement3();
        demonstrateRequirement4();
        demonstrateRequirement5();
        
        System.out.println("\n═════════════════════════════════════════════════════════════");
        System.out.println("           ALL REQUIREMENTS DEMONSTRATED!");
        System.out.println("═════════════════════════════════════════════════════════════\n");
    }
    
    /**
     * Requirement 1: Create an order with multiple items.
     */
    private static void demonstrateRequirement1() {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ REQUIREMENT 1: Create an order with multiple items     │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println("\nPattern Used: FACTORY PATTERN");
        System.out.println("- OrderFactory creates Order and OrderItem objects");
        System.out.println("- Ensures proper initialization and validation\n");
        
        pause("Press Enter to create an order...");
        
        // Create order items using Factory
        List<OrderItem> items = Arrays.asList(
            orderFactory.createOrderItem("LAPTOP-001", 2, new BigDecimal("999.99")),
            orderFactory.createOrderItem("MOUSE-001", 3, new BigDecimal("29.99")),
            orderFactory.createOrderItem("KEYBOARD-001", 1, new BigDecimal("79.99"))
        );
        
        // Create order
        Order order = orderService.createOrder(items);
        System.out.println(order.toDetailedString());
        
        pause("Press Enter to continue...");
    }
    
    /**
     * Requirement 2: Retrieve order details by ID.
     */
    private static void demonstrateRequirement2() {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ REQUIREMENT 2: Retrieve order details by ID            │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println("\nData Structure: CONCURRENT HASHMAP");
        System.out.println("- O(1) lookup time");
        System.out.println("- Thread-safe for concurrent access\n");
        
        // Get the first order
        if (orderService.getOrderCount() > 0) {
            Order order = orderService.getAllOrders().get(0);
            String orderId = order.getId();
            
            pause("Press Enter to retrieve order " + orderId + "...");
            
            Optional<Order> retrieved = orderService.getOrder(orderId);
            if (retrieved.isPresent()) {
                System.out.println("✓ Order retrieved successfully!");
                System.out.println(retrieved.get().toDetailedString());
            }
        }
        
        pause("Press Enter to continue...");
    }
    
    /**
     * Requirement 3: Automatic status update PENDING → PROCESSING after 5 minutes.
     */
    private static void demonstrateRequirement3() {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ REQUIREMENT 3: Auto-update PENDING → PROCESSING        │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println("\nPatterns Used:");
        System.out.println("- OBSERVER PATTERN: PendingOrderProcessor watches for new orders");
        System.out.println("- STATE PATTERN: Validates and executes state transitions\n");
        System.out.println("Note: Demo uses 1-minute delay instead of 5 minutes\n");
        
        pause("Press Enter to create an order and wait for auto-processing...");
        
        // Create a new order
        List<OrderItem> items = Arrays.asList(
            orderFactory.createOrderItem("MONITOR-001", 1, new BigDecimal("299.99"))
        );
        Order order = orderService.createOrder(items);
        System.out.println("Created order: " + order.getId());
        System.out.println("Initial status: " + order.getStatus());
        
        System.out.println("\nWaiting for automatic processing (1 minute)...");
        System.out.println("(The Observer scheduled a background task)");
        
        // Wait for auto-processing
        try {
            for (int i = 60; i > 0; i -= 10) {
                System.out.println("  " + i + " seconds remaining...");
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\nChecking order status now:");
        orderService.getOrder(order.getId()).ifPresent(o -> {
            System.out.println("Current status: " + o.getStatus());
            if (o.getStatus() == OrderStatus.PROCESSING) {
                System.out.println("✓ Automatic processing succeeded!");
            }
        });
        
        pause("Press Enter to continue...");
    }
    
    /**
     * Requirement 4: List all orders with optional filtering.
     */
    private static void demonstrateRequirement4() {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ REQUIREMENT 4: List all orders with filtering          │");
        System.out.println("└─────────────────────────────────────────────────────────┘\n");
        
        pause("Press Enter to view all orders...");
        
        displayAllOrders();
        
        System.out.println("\nFiltering by status:");
        for (OrderStatus status : OrderStatus.values()) {
            List<Order> filtered = orderService.getOrdersByStatus(status);
            if (!filtered.isEmpty()) {
                System.out.println("  " + status + ": " + filtered.size() + " order(s)");
            }
        }
        
        pause("Press Enter to continue...");
    }
    
    /**
     * Requirement 5: Cancel order (only if PENDING).
     */
    private static void demonstrateRequirement5() {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│ REQUIREMENT 5: Cancel order (only if PENDING)          │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println("\nPattern Used: STATE PATTERN");
        System.out.println("- Only PENDING state allows transition to CANCELLED");
        System.out.println("- State Pattern enforces this business rule\n");
        
        // Test 1: Cancel PENDING order (should succeed)
        System.out.println("Test 1: Cancelling a PENDING order");
        pause("Press Enter to create and cancel a PENDING order...");
        
        List<OrderItem> items1 = Arrays.asList(
            orderFactory.createOrderItem("TEST-001", 1, new BigDecimal("10.00"))
        );
        Order pendingOrder = orderService.createOrder(items1);
        System.out.println("Created order: " + pendingOrder.getId());
        System.out.println("Status: " + pendingOrder.getStatus());
        
        boolean result1 = orderService.cancelOrder(pendingOrder.getId());
        System.out.println("Cancellation result: " + (result1 ? "SUCCESS ✓" : "FAILED ✗"));
        
        // Test 2: Try to cancel PROCESSING order (should fail)
        System.out.println("\nTest 2: Trying to cancel a PROCESSING order");
        pause("Press Enter to test cancelling a PROCESSING order...");
        
        // Find a PROCESSING order
        Optional<Order> processingOrder = orderService.getOrdersByStatus(OrderStatus.PROCESSING)
            .stream()
            .findFirst();
        
        if (processingOrder.isPresent()) {
            String orderId = processingOrder.get().getId();
            System.out.println("Found order: " + orderId);
            System.out.println("Status: " + processingOrder.get().getStatus());
            
            boolean result2 = orderService.cancelOrder(orderId);
            System.out.println("Cancellation result: " + (result2 ? "SUCCESS ✓" : "FAILED ✗ (as expected)"));
            
            if (!result2) {
                System.out.println("\n✓ Business rule enforced:");
                System.out.println("  Orders can only be cancelled when in PENDING status!");
            }
        } else {
            System.out.println("No PROCESSING orders available for testing");
        }
        
        pause("Press Enter to finish...");
    }
    
    /**
     * Run interactive mode where user can execute commands.
     */
    private static void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        System.out.println("═════════════════════════════════════════════════════════════");
        System.out.println("                   INTERACTIVE MODE");
        System.out.println("═════════════════════════════════════════════════════════════\n");
        
        while (running) {
            displayMenu();
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    createOrderInteractive(scanner);
                    break;
                case "2":
                    retrieveOrderInteractive(scanner);
                    break;
                case "3":
                    listAllOrders();
                    break;
                case "4":
                    listOrdersByStatus(scanner);
                    break;
                case "5":
                    cancelOrderInteractive(scanner);
                    break;
                case "6":
                    displayStatistics();
                    break;
                case "7":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }
    
    private static void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                      MAIN MENU                           ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Create Order                                         ║");
        System.out.println("║  2. Retrieve Order by ID                                 ║");
        System.out.println("║  3. List All Orders                                      ║");
        System.out.println("║  4. List Orders by Status                                ║");
        System.out.println("║  5. Cancel Order                                         ║");
        System.out.println("║  6. View Statistics                                      ║");
        System.out.println("║  7. Exit                                                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }
    
    private static void createOrderInteractive(Scanner scanner) {
        System.out.println("\n--- Create New Order ---");
        // Implementation simplified for demo
        List<OrderItem> items = Arrays.asList(
            orderFactory.createOrderItem("SAMPLE-" + System.currentTimeMillis(), 
                                        1, 
                                        new BigDecimal("99.99"))
        );
        Order order = orderService.createOrder(items);
        System.out.println(order.toDetailedString());
    }
    
    private static void retrieveOrderInteractive(Scanner scanner) {
        System.out.println("\n--- Retrieve Order ---");
        System.out.print("Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        
        Optional<Order> order = orderService.getOrder(orderId);
        if (order.isPresent()) {
            System.out.println(order.get().toDetailedString());
        } else {
            System.out.println("Order not found: " + orderId);
        }
    }
    
    private static void listAllOrders() {
        displayAllOrders();
    }
    
    private static void listOrdersByStatus(Scanner scanner) {
        System.out.println("\n--- List Orders by Status ---");
        System.out.println("Available statuses:");
        for (OrderStatus status : OrderStatus.values()) {
            System.out.println("  - " + status);
        }
        System.out.print("Enter status: ");
        String statusStr = scanner.nextLine().trim().toUpperCase();
        
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr);
            List<Order> orders = orderService.getOrdersByStatus(status);
            System.out.println("\nFound " + orders.size() + " order(s) with status " + status);
            orders.forEach(order -> System.out.println("  - " + order));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status: " + statusStr);
        }
    }
    
    private static void cancelOrderInteractive(Scanner scanner) {
        System.out.println("\n--- Cancel Order ---");
        System.out.print("Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        
        boolean result = orderService.cancelOrder(orderId);
        if (result) {
            System.out.println("Order cancelled successfully!");
        } else {
            System.out.println("Failed to cancel order (might not be in PENDING status)");
        }
    }
    
    private static void displayStatistics() {
        System.out.println("\n--- System Statistics ---");
        OrderService.OrderStatistics stats = orderService.getStatistics();
        System.out.println("Total Orders: " + stats.getTotalOrders());
        System.out.println("  PENDING: " + stats.getPendingOrders());
        System.out.println("  PROCESSING: " + stats.getProcessingOrders());
        System.out.println("  SHIPPED: " + stats.getShippedOrders());
        System.out.println("  DELIVERED: " + stats.getDeliveredOrders());
        System.out.println("  CANCELLED: " + stats.getCancelledOrders());
    }
    
    private static void displayAllOrders() {
        List<Order> allOrders = orderService.getAllOrders();
        System.out.println("\n--- All Orders (" + allOrders.size() + ") ---");
        if (allOrders.isEmpty()) {
            System.out.println("No orders in system");
        } else {
            allOrders.forEach(System.out::println);
        }
    }
    
    private static void pause(String message) {
        System.out.print("\n" + message);
        try {
            System.in.read();
            System.in.skip(System.in.available());
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private static void cleanup() {
        System.out.println("\nShutting down system...");
        if (pendingProcessor != null) {
            pendingProcessor.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("✓ System shutdown complete");
        System.out.println("\nThank you for using the Order Processing System!\n");
    }
}
