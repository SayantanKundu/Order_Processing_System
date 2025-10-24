package com.order.processing.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized debug logging utility with timestamps and categories
 */
public class DebugLogger {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    
    public enum Category {
        STATE(CYAN, "STATE"),
        FACTORY(MAGENTA, "FACTORY"),
        OBSERVER(YELLOW, "OBSERVER"),
        SERVICE(BLUE, "SERVICE"),
        MODEL(GREEN, "MODEL"),
        MAIN(WHITE, "MAIN"),
        ERROR(RED, "ERROR");
        
        private final String color;
        private final String label;
        
        Category(String color, String label) {
            this.color = color;
            this.label = label;
        }
    }
    
    public static void log(Category category, String component, String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        System.out.println(String.format(
            "%s[%s] [%s] [%s]%s %s",
            category.color,
            timestamp,
            category.label,
            component,
            RESET,
            message
        ));
    }
    
    public static void logStateTransition(String orderId, String fromState, String toState, boolean allowed) {
        String status = allowed ? GREEN + "✓ ALLOWED" + RESET : RED + "✗ BLOCKED" + RESET;
        log(Category.STATE, "Transition", 
            String.format("Order[%s] %s → %s [%s]", 
                orderId.substring(0, 8), fromState, toState, status));
    }
    
    public static void logOrderCreation(String orderId, int itemCount, String total) {
        log(Category.FACTORY, "OrderFactory", 
            String.format("Created Order[%s] with %d items, Total: $%s", 
                orderId.substring(0, 8), itemCount, total));
    }
    
    public static void logObserverNotification(String orderId, String status, String action) {
        log(Category.OBSERVER, "PendingProcessor", 
            String.format("Order[%s] status=%s → %s", 
                orderId.substring(0, 8), status, action));
    }
    
    public static void logServiceOperation(String operation, String orderId, String details) {
        log(Category.SERVICE, operation, 
            String.format("Order[%s] - %s", orderId.substring(0, 8), details));
    }
    
    public static void separator() {
        System.out.println(CYAN + "═".repeat(100) + RESET);
    }
    
    public static void section(String title) {
        System.out.println();
        separator();
        System.out.println(YELLOW + "║ " + title);
        separator();
    }
}
