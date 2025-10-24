# E-Commerce Order Processing System

A complete, production-ready order processing system demonstrating design patterns and best practices.

## 🎯 Overview

This system manages e-commerce orders through their complete lifecycle, from creation to delivery. It implements three key design patterns:

- **State Pattern** - Manages order status transitions and enforces business rules
- **Factory Pattern** - Ensures consistent object creation
- **Observer Pattern** - Handles automatic background processing

## ✨ Features

✅ Create orders with multiple items  
✅ Retrieve order details by ID  
✅ Automatic status updates (PENDING → PROCESSING after 5 minutes)  
✅ List and filter orders by status  
✅ Cancel orders (only when PENDING)  
✅ Thread-safe concurrent operations  
✅ Clean architecture with separation of concerns  

## 🚀 Quick Start

### Run the Quick Demo
```bash
mvn clean compile exec:java -Dexec.mainClass="Main" -Dexec.args="--quick-demo"
```

This demonstrates all features in ~1 minute including:
- Order creation
- Order retrieval
- Cancellation (success & failure scenarios)
- Automatic background processing
- Order listing

### Run the Full Demo
```bash
mvn clean compile exec:java -Dexec.mainClass="Main" -Dexec.args="--demo"
```

Interactive step-by-step demonstration with detailed explanations.

### Run Interactive Mode
```bash
mvn clean compile exec:java -Dexec.mainClass="Main"
```

Menu-driven interface to manually test all features.

## 📋 Requirements Met

All 5 requirements are fully implemented:

| # | Requirement | Status |
|---|------------|--------|
| 1 | Create order with multiple items | ✅ Complete |
| 2 | Retrieve order by ID | ✅ Complete |
| 3 | Auto-update PENDING → PROCESSING (5 min) | ✅ Complete |
| 4 | List all orders / filter by status | ✅ Complete |
| 5 | Cancel order (only if PENDING) | ✅ Complete |

## 📚 Documentation

Comprehensive documentation is available:

1. **[SYSTEM_UNDERSTANDING_GUIDE.md](SYSTEM_UNDERSTANDING_GUIDE.md)**
   - Complete learning guide with examples
   - Pattern explanations and diagrams
   - User flow scenarios

2. **[VISUAL_DIAGRAMS.md](VISUAL_DIAGRAMS.md)**
   - Quick reference diagrams
   - State transition visuals
   - Timeline views

3. **[COMPLETE_FLOW.md](COMPLETE_FLOW.md)**
   - End-to-end flow documentation
   - Detailed scenarios

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT / API LAYER                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│  SERVICE LAYER                                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  OrderService (Main Controller)                        │ │
│  │  - Thread-safe operations                              │ │
│  │  - Observer management                                 │ │
│  └────────────────────────────────────────────────────────┘ │
└─────┬──────────────────────┬──────────────────────┬─────────┘
      │                      │                      │
      ▼                      ▼                      ▼
┌──────────┐         ┌──────────────┐      ┌──────────────┐
│ Factory  │         │  Observer    │      │   Storage    │
│ Pattern  │         │  Pattern     │      │ (In-Memory)  │
└──────────┘         └──────────────┘      └──────────────┘
```

## 🎨 Design Patterns

### State Pattern
Manages order status transitions and enforces business rules.

```
PENDING → PROCESSING → SHIPPED → DELIVERED
   ↓
CANCELLED
```

Only PENDING orders can be cancelled (enforced by the pattern).

### Factory Pattern
Centralizes order and order item creation with consistent initialization.

### Observer Pattern
Enables automatic background processing. When an order is created, the `PendingOrderProcessor` observer is notified and schedules processing after 5 minutes.

## 🔧 Technical Details

- **Java Version**: 17+
- **Build Tool**: Maven
- **Storage**: In-memory (ConcurrentHashMap)
- **Threading**: ScheduledExecutorService for background tasks
- **Thread Safety**: Synchronized methods + concurrent collections

## 📁 Project Structure

```
src/main/java/
├── Main.java (Entry point)
└── com/order/processing/
    ├── Main.java (Application)
    ├── state/           # State Pattern
    │   ├── OrderStatus.java
    │   ├── OrderState.java
    │   └── [5 state implementations]
    ├── model/           # Domain Models
    │   ├── Order.java
    │   └── OrderItem.java
    ├── factory/         # Factory Pattern
    │   ├── OrderFactory.java
    │   └── StandardOrderFactory.java
    ├── observer/        # Observer Pattern
    │   ├── OrderObserver.java
    │   └── PendingOrderProcessor.java
    └── service/         # Business Logic
        └── OrderService.java
```

## 💡 Example Usage

```java
// Create factory and service
OrderFactory factory = new StandardOrderFactory();
OrderService orderService = new OrderService(factory);

// Register observer for background processing
ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
PendingOrderProcessor processor = new PendingOrderProcessor(executor);
orderService.addObserver(processor);

// Create an order
List<OrderItem> items = Arrays.asList(
    factory.createOrderItem("LAPTOP-001", 1, new BigDecimal("999.99"))
);
Order order = orderService.createOrder(items);

// Retrieve order
Optional<Order> retrieved = orderService.getOrder(order.getId());

// Cancel order (only if PENDING)
boolean cancelled = orderService.cancelOrder(order.getId());

// List orders by status
List<Order> pendingOrders = orderService.getOrdersByStatus(OrderStatus.PENDING);
```

## 🧪 Verification

The system has been fully tested and verified:

✅ Compilation with no errors  
✅ All requirements functional  
✅ State transitions validated  
✅ Business rules enforced  
✅ Thread safety confirmed  

## 📝 Requirements

- Java 17 or higher
- Maven 3.6 or higher

## 👨‍💻 Development

### Build
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Package
```bash
mvn package
```

## 📄 License

This is a demo project for educational purposes.

---

**Version**: 1.0.0  
**Status**: ✅ Complete & Production-Ready  
**Last Updated**: December 2024
