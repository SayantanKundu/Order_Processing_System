# Order Processing System - Complete Understanding Guide

## Table of Contents
1. [System Overview](#system-overview)
2. [Valid Design Patterns](#valid-design-patterns)
3. [System Architecture Diagram](#system-architecture-diagram)
4. [Complete User Flow Examples](#complete-user-flow-examples)
5. [Component Interactions](#component-interactions)

---

## System Overview

This is an **E-commerce Order Processing System** that allows customers to:
- Place orders with multiple items
- Track order status
- Cancel orders (only when PENDING)
- Automatically process pending orders every 5 minutes

**Key Features:**
- In-memory storage (no database)
- Automatic background processing
- State-based order management
- Thread-safe operations

---

## Valid Design Patterns

### 1. 🎯 **State Pattern** (HIGHLY VALID)

#### What is it?
A pattern that allows an object to change its behavior when its internal state changes. Think of it like a traffic light - it behaves differently when it's red vs green.

#### Why do we use it?
Orders go through different stages: PENDING → PROCESSING → SHIPPED → DELIVERED. Each state has different rules:
- **PENDING**: Can be cancelled, will be auto-processed
- **PROCESSING**: Cannot be cancelled, will move to shipped
- **SHIPPED**: Cannot be cancelled, will move to delivered
- **DELIVERED**: Final state, cannot change
- **CANCELLED**: Final state, cannot change

#### How it works in our system:

```
┌─────────────────────────────────────────────────────────────┐
│                        Order Object                          │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Current State: [PendingState object]              │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                          │
                          │ delegates behavior to
                          ▼
         ┌────────────────────────────────────┐
         │       OrderState Interface         │
         │  - processOrder()                  │
         │  - canTransitionTo()               │
         │  - getStatus()                     │
         └────────────────────────────────────┘
                          │
         ┌────────────────┼────────────────┬─────────────┐
         │                │                │             │
    ┌────▼────┐    ┌─────▼──────┐   ┌────▼────┐  ┌─────▼─────┐
    │ Pending │    │ Processing │   │ Shipped │  │ Delivered │
    │  State  │    │   State    │   │  State  │  │   State   │
    └─────────┘    └────────────┘   └─────────┘  └───────────┘
         │
         └──────┐
         ┌──────▼──────┐
         │  Cancelled  │
         │    State    │
         └─────────────┘
```

#### Real-world Example:
```java
// Create an order - starts in PENDING state
Order order = new Order(items, new PendingState());

// Try to process it
order.processOrder(); // Changes to ProcessingState ✅

// Try to cancel it (now in PROCESSING state)
order.setState(new CancelledState()); // FAILS! ❌
// Throws exception: Cannot transition from PROCESSING to CANCELLED
```

#### Benefits:
✅ Enforces business rules (can only cancel PENDING orders)
✅ Makes valid transitions explicit
✅ Easy to add new states without breaking existing code
✅ Each state knows its own rules

---

### 2. 🏭 **Factory Pattern** (VALID)

#### What is it?
A pattern that creates objects without exposing the creation logic. Like a restaurant kitchen - you order food, but you don't need to know how it's made.

#### Why do we use it?
Creating an order involves several steps:
- Generate unique order ID
- Initialize items
- Set initial state (PENDING)
- Calculate total amount
- Set timestamps

Instead of repeating this logic everywhere, we centralize it in a factory.

#### How it works:

```
┌──────────────────────────────────────────────────────────┐
│              OrderFactory Interface                       │
│  + createOrder(items): Order                             │
│  + createOrderItem(productId, qty, price): OrderItem     │
└──────────────────────────────────────────────────────────┘
                          △
                          │ implements
                          │
┌─────────────────────────┴─────────────────────────────────┐
│           StandardOrderFactory                             │
│  + createOrder(items): Order {                            │
│      1. Create new Order                                  │
│      2. Set items                                         │
│      3. Set initial state = PENDING                       │
│      4. Generate UUID                                     │
│      5. Return configured order                           │
│    }                                                       │
└────────────────────────────────────────────────────────────┘
```

#### Real-world Example:
```java
// Without Factory (BAD - too much knowledge required)
Order order = new Order();
order.setId(UUID.randomUUID().toString());
order.setItems(items);
order.setState(new PendingState());
order.setCreatedAt(LocalDateTime.now());
order.calculateTotal();

// With Factory (GOOD - simple and clean)
Order order = orderFactory.createOrder(items);
// Everything is configured correctly!
```

#### Benefits:
✅ Hides complexity of object creation
✅ Ensures objects are created correctly every time
✅ Easy to change creation logic in one place
✅ Can create different types of orders in the future

---

### 3. 👁️ **Observer Pattern** (VALID)

#### What is it?
A pattern where objects "watch" other objects and react when something happens. Like subscribing to a YouTube channel - you get notified when new content is posted.

#### Why do we use it?
The requirement says: "A background job should automatically update PENDING orders to PROCESSING every 5 minutes."

We need something to watch for new orders and schedule their processing.

#### How it works:

```
┌──────────────────────────────────────────────────────────┐
│                    OrderService                           │
│  - orders: Map<String, Order>                            │
│  - observers: List<OrderObserver>                        │
│                                                           │
│  + createOrder(items) {                                  │
│      1. Create order via factory                         │
│      2. Store in map                                     │
│      3. notifyObservers(order) ←─────────┐             │
│    }                                       │             │
└────────────────────────────────────────────┼─────────────┘
                                             │
                              Notifies all observers
                                             │
                    ┌────────────────────────┼────────────────┐
                    │                        │                │
         ┌──────────▼────────┐    ┌─────────▼────────┐      │
         │ PendingOrder      │    │  EmailNotifier   │   (future)
         │   Processor       │    │    Observer      │
         │                   │    └──────────────────┘
         │ watches for       │
         │ PENDING orders    │
         │                   │
         │ Schedules process │
         │ after 5 minutes   │
         └───────────────────┘
```

#### Real-world Example:
```java
// Setup
OrderService orderService = new OrderService(factory);
PendingOrderProcessor processor = new PendingOrderProcessor(executor);
orderService.addObserver(processor); // Subscribe to order events

// Customer creates order
Order order = orderService.createOrder(items);
// ↓
// OrderService notifies all observers
// ↓
// PendingOrderProcessor sees it's PENDING
// ↓
// Schedules processing in 5 minutes
// ↓
// After 5 minutes: order.processOrder() → becomes PROCESSING
```

#### Benefits:
✅ Decouples order creation from background processing
✅ Easy to add more observers (e.g., email notifications, inventory updates)
✅ OrderService doesn't need to know about background jobs
✅ Clean separation of concerns

---

## System Architecture Diagram

### High-Level Component View

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│                  (REST API / Main Application)                   │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               │ calls methods
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                               │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │              OrderService (Main Controller)                │ │
│  │  - createOrder(items)                                      │ │
│  │  - getOrder(orderId)                                       │ │
│  │  - getAllOrders()                                          │ │
│  │  - getOrdersByStatus(status)                               │ │
│  │  - cancelOrder(orderId)                                    │ │
│  └───────────────────────────────────────────────────────────┘ │
└──────┬────────────────────────────────┬────────────────────┬────┘
       │                                │                    │
       │ uses                           │ notifies           │ uses
       ▼                                ▼                    ▼
┌──────────────┐            ┌────────────────────┐   ┌──────────────┐
│   Factory    │            │    Observers       │   │   Storage    │
│   Pattern    │            │   (Background)     │   │   (In-Mem)   │
│              │            │                    │   │              │
│ ┌──────────┐ │            │ ┌────────────────┐│   │ ┌──────────┐ │
│ │Standard  │ │            │ │ PendingOrder   ││   │ │Concurrent│ │
│ │Order     │ │            │ │  Processor     ││   │ │ HashMap  │ │
│ │Factory   │ │            │ └────────────────┘│   │ │<OrderID, │ │
│ └──────────┘ │            │                    │   │ │ Order>   │ │
│              │            │ Watches PENDING    │   │ └──────────┘ │
│ Creates:     │            │ orders & schedules │   │              │
│ - Orders     │            │ auto-processing    │   │ Thread-safe  │
│ - OrderItems │            │ every 5 minutes    │   │ storage      │
└──────────────┘            └────────────────────┘   └──────────────┘
       │                                                     │
       │ creates with                                       │
       ▼                                                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                 Order (Domain Model)                      │  │
│  │  - id: String                                             │  │
│  │  - items: List<OrderItem>                                │  │
│  │  - currentState: OrderState  ←── Uses State Pattern      │  │
│  │  - createdAt: LocalDateTime                              │  │
│  │  - totalAmount: BigDecimal                               │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│                              │ delegates to                      │
│                              ▼                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              State Pattern (Order Status)                 │  │
│  │                                                            │  │
│  │    ┌─────────┐  ┌────────────┐  ┌─────────┐             │  │
│  │    │ PENDING │→ │ PROCESSING │→ │ SHIPPED │→ DELIVERED  │  │
│  │    └────┬────┘  └────────────┘  └─────────┘             │  │
│  │         │                                                 │  │
│  │         ↓                                                 │  │
│  │    ┌─────────┐                                           │  │
│  │    │CANCELLED│                                           │  │
│  │    └─────────┘                                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow Diagram

```
┌──────────┐
│ Customer │
└────┬─────┘
     │
     │ 1. Create Order Request
     ▼
┌─────────────────┐
│  OrderService   │
└────┬───────┬────┘
     │       │
     │       │ 2. Notify
     │       ▼
     │   ┌──────────────────┐
     │   │ PendingOrder     │
     │   │   Processor      │
     │   └────┬─────────────┘
     │        │
     │        │ 3. Schedule (5 min)
     │        ▼
     │   ┌──────────────────┐
     │   │Scheduled Executor│
     │   └────┬─────────────┘
     │        │
     │ 4. Use Factory
     ▼        │
┌──────────────┐ │
│OrderFactory  │ │
└────┬─────────┘ │
     │           │
     │ 5. Create │
     ▼           │
┌──────────────┐ │
│   Order      │ │
│ [PENDING]    │ │
└────┬─────────┘ │
     │           │
     │ 6. Store  │
     ▼           │
┌──────────────┐ │
│ ConcurrentMap│ │
└──────────────┘ │
                 │
     ┌───────────┘
     │ 7. After 5 minutes
     ▼
┌──────────────┐
│   Order      │
│ [PROCESSING] │
└──────────────┘
```

---

## Complete User Flow Examples

### Example 1: Create an Order (Happy Path)

#### Scenario
Customer wants to buy 2 Laptops ($999 each) and 1 Mouse ($29).

#### Step-by-Step Flow

```
STEP 1: Customer Request Arrives
═══════════════════════════════════════════════════
Request: POST /orders
Body: {
  "items": [
    {"productId": "LAPTOP-001", "quantity": 2, "price": 999.00},
    {"productId": "MOUSE-001", "quantity": 1, "price": 29.00}
  ]
}


STEP 2: OrderService Receives Request
═══════════════════════════════════════════════════
Code: orderService.createOrder(items)

OrderService says: "I need to create an order, let me use the factory"


STEP 3: Factory Creates the Order
═══════════════════════════════════════════════════
StandardOrderFactory.createOrder() does:
  1. Creates new Order object
  2. Generates ID: "550e8400-e29b-41d4-a716-446655440000"
  3. Adds items to order
  4. Sets state: new PendingState()
  5. Sets timestamp: 2025-10-24 10:30:00
  6. Calculates total: $2027.00

Returns: Order object ready to use


STEP 4: OrderService Stores the Order
═══════════════════════════════════════════════════
orders.put("550e8400-e29b-41d4-a716-446655440000", order)

Order is now in memory storage (ConcurrentHashMap)


STEP 5: OrderService Notifies Observers
═══════════════════════════════════════════════════
orderService.notifyObservers(order)
  ↓
PendingOrderProcessor.onOrderStatusChanged(order)
  ↓
Checks: Is this order PENDING? YES!
  ↓
executor.schedule(processOrder, 5, MINUTES)

A timer is set: "Process this order at 10:35:00"


STEP 6: Response to Customer
═══════════════════════════════════════════════════
Response: {
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "total": 2027.00,
  "createdAt": "2025-10-24T10:30:00"
}

Customer sees: "Order created successfully! Status: PENDING"


STEP 7: Wait 5 Minutes... (Background Processing)
═══════════════════════════════════════════════════
Time: 10:35:00
Scheduled task runs: processOrder(order)
  ↓
order.processOrder()
  ↓
PendingState.processOrder() is called
  ↓
order.setState(new ProcessingState())
  ↓
Order status changes: PENDING → PROCESSING

Customer can now see: Status: PROCESSING
```

#### Code Visualization

```java
// What happens behind the scenes

// 1. Request arrives
List<OrderItem> items = Arrays.asList(
    new OrderItem("LAPTOP-001", 2, new BigDecimal("999.00")),
    new OrderItem("MOUSE-001", 1, new BigDecimal("29.00"))
);

// 2. Service creates order
Order order = orderService.createOrder(items);
// Inside this method:
//   - factory.createOrder(items) ← Factory Pattern
//   - orders.put(order.getId(), order) ← Storage
//   - notifyObservers(order) ← Observer Pattern
//   - PendingOrderProcessor schedules task

// 3. Order is created with PENDING state
System.out.println(order.getStatus()); // PENDING
System.out.println(order.getTotalAmount()); // 2027.00

// 4. After 5 minutes (automatic)
// PendingOrderProcessor executes:
order.processOrder(); // State Pattern handles transition

// 5. Order is now PROCESSING
System.out.println(order.getStatus()); // PROCESSING
```

---

### Example 2: Retrieve Order Details

#### Scenario
Customer wants to check their order status.

#### Flow

```
STEP 1: Customer Request
═══════════════════════════════════════════════════
Request: GET /orders/550e8400-e29b-41d4-a716-446655440000


STEP 2: OrderService Lookup
═══════════════════════════════════════════════════
Code: orderService.getOrder("550e8400-e29b-41d4-a716-446655440000")

OrderService looks in ConcurrentHashMap:
  orders.get("550e8400-e29b-41d4-a716-446655440000")

Time Complexity: O(1) - Instant lookup!


STEP 3: Order Found
═══════════════════════════════════════════════════
Order retrieved from memory
Current state: ProcessingState


STEP 4: Response
═══════════════════════════════════════════════════
Response: {
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PROCESSING",
  "items": [
    {"productId": "LAPTOP-001", "quantity": 2, "price": 999.00},
    {"productId": "MOUSE-001", "quantity": 1, "price": 29.00}
  ],
  "total": 2027.00,
  "createdAt": "2025-10-24T10:30:00",
  "lastModifiedAt": "2025-10-24T10:35:00"
}
```

---

### Example 3: Cancel Order (Success - PENDING)

#### Scenario
Customer changes their mind and wants to cancel their order immediately after placing it.

#### Flow

```
STEP 1: Customer Request
═══════════════════════════════════════════════════
Request: DELETE /orders/550e8400-e29b-41d4-a716-446655440000
Time: 10:32:00 (before auto-processing at 10:35:00)


STEP 2: OrderService Retrieves Order
═══════════════════════════════════════════════════
Optional<Order> orderOpt = orderService.getOrder("550e8400...");
Order found: ✅


STEP 3: Check Current Status
═══════════════════════════════════════════════════
Current status: PENDING
Can cancel? YES! ✅


STEP 4: Cancel Order
═══════════════════════════════════════════════════
orderService.cancelOrder("550e8400...")
  ↓
order.setState(new CancelledState())
  ↓
PendingState.canTransitionTo(CancelledState) → TRUE ✅
  ↓
State changes: PENDING → CANCELLED


STEP 5: Notify Observers
═══════════════════════════════════════════════════
notifyObservers(order)
PendingOrderProcessor sees CANCELLED status
  → Does nothing (order is cancelled, no need to process)


STEP 6: Response
═══════════════════════════════════════════════════
Response: {
  "success": true,
  "message": "Order cancelled successfully"
}

The scheduled processing task at 10:35:00 will check the status
and see it's CANCELLED, so it won't process it.
```

---

### Example 4: Cancel Order (Fail - PROCESSING)

#### Scenario
Customer tries to cancel after the order has been automatically processed.

#### Flow

```
STEP 1: Customer Request
═══════════════════════════════════════════════════
Request: DELETE /orders/550e8400-e29b-41d4-a716-446655440000
Time: 10:40:00 (after auto-processing at 10:35:00)


STEP 2: OrderService Retrieves Order
═══════════════════════════════════════════════════
Order found: ✅


STEP 3: Check Current Status
═══════════════════════════════════════════════════
Current status: PROCESSING (changed at 10:35:00)
Can cancel? NO! ❌


STEP 4: Cancellation Attempt
═══════════════════════════════════════════════════
orderService.cancelOrder("550e8400...")
  ↓
Checks: if (order.getStatus() == OrderStatus.PENDING)
  ↓
Result: FALSE (it's PROCESSING now)
  ↓
Returns: false


STEP 5: Response
═══════════════════════════════════════════════════
Response: {
  "success": false,
  "message": "Cannot cancel order. Order is already in PROCESSING status.",
  "currentStatus": "PROCESSING"
}

Customer sees: Order cannot be cancelled once processing has started.
```

#### Why This Happens (State Pattern in Action)

```java
// The State Pattern enforces this rule

// ProcessingState class
public class ProcessingState implements OrderState {
    @Override
    public boolean canTransitionTo(OrderState newState) {
        // Only allow transition to SHIPPED
        // CANCELLED is NOT allowed!
        return newState instanceof ShippedState;
    }
}

// If someone tries to force it:
order.setState(new CancelledState());
// Throws IllegalStateException:
// "Cannot transition from PROCESSING to CANCELLED"
```

---

### Example 5: List All Orders by Status

#### Scenario
Admin wants to see all PENDING orders.

#### Flow

```
STEP 1: Admin Request
═══════════════════════════════════════════════════
Request: GET /orders?status=PENDING


STEP 2: OrderService Filters
═══════════════════════════════════════════════════
Code: orderService.getOrdersByStatus(OrderStatus.PENDING)

Internally:
  orders.values().stream()
    .filter(order -> order.getStatus() == OrderStatus.PENDING)
    .toList()


STEP 3: Results
═══════════════════════════════════════════════════
Found 3 PENDING orders:

Response: {
  "orders": [
    {
      "orderId": "order-001",
      "status": "PENDING",
      "total": 1500.00,
      "createdAt": "2025-10-24T10:25:00"
    },
    {
      "orderId": "order-002",
      "status": "PENDING",
      "total": 750.00,
      "createdAt": "2025-10-24T10:28:00"
    },
    {
      "orderId": "order-003",
      "status": "PENDING",
      "total": 2027.00,
      "createdAt": "2025-10-24T10:30:00"
    }
  ],
  "count": 3
}
```

---

### Example 6: Complete Order Lifecycle

#### Scenario
Following one order from creation to delivery.

```
┌─────────────────────────────────────────────────────────────┐
│  TIME: 10:00 AM - ORDER CREATED                             │
├─────────────────────────────────────────────────────────────┤
│  Action: Customer places order                              │
│  Status: PENDING                                            │
│  System: Factory creates order → Observer schedules task   │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  TIME: 10:05 AM - AUTO-PROCESSING                           │
├─────────────────────────────────────────────────────────────┤
│  Action: Background job runs (5 minutes passed)            │
│  Status: PENDING → PROCESSING                               │
│  System: State Pattern validates transition                │
│  Note: Customer can no longer cancel!                      │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  TIME: 10:30 AM - WAREHOUSE SHIPS                           │
├─────────────────────────────────────────────────────────────┤
│  Action: Warehouse staff marks as shipped                  │
│  Status: PROCESSING → SHIPPED                               │
│  API Call: PUT /orders/{id}/status?status=SHIPPED          │
│  System: State Pattern validates transition                │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  TIME: 2:00 PM - CUSTOMER RECEIVES                          │
├─────────────────────────────────────────────────────────────┤
│  Action: Delivery confirmed                                │
│  Status: SHIPPED → DELIVERED                                │
│  API Call: PUT /orders/{id}/status?status=DELIVERED        │
│  System: State Pattern validates transition                │
│  Note: Final state - no more changes allowed               │
└─────────────────────────────────────────────────────────────┘
```

#### Complete Timeline Code

```java
// 10:00 AM - Order Created
Order order = orderService.createOrder(items);
System.out.println(order.getStatus()); // PENDING
// Observer scheduled processing for 10:05 AM

// 10:02 AM - Customer checks order
Optional<Order> found = orderService.getOrder(order.getId());
System.out.println(found.get().getStatus()); // Still PENDING

// 10:03 AM - Customer wants to cancel
boolean cancelled = orderService.cancelOrder(order.getId());
System.out.println(cancelled); // TRUE - Success! ✅

// Restart timeline with no cancellation...

// 10:05 AM - Background job runs automatically
// PendingOrderProcessor's scheduled task executes:
order.processOrder(); // State: PENDING → PROCESSING

// 10:06 AM - Customer tries to cancel now
boolean cancelled = orderService.cancelOrder(order.getId());
System.out.println(cancelled); // FALSE - Too late! ❌

// 10:30 AM - Warehouse ships the order
order.setState(new ShippedState()); // PROCESSING → SHIPPED

// 2:00 PM - Customer receives delivery
order.setState(new DeliveredState()); // SHIPPED → DELIVERED

// Final state
System.out.println(order.getStatus()); // DELIVERED
```

---

## Component Interactions

### How All Patterns Work Together

```
┌─────────────────────────────────────────────────────────────────┐
│                    COMPLETE FLOW DIAGRAM                         │
└─────────────────────────────────────────────────────────────────┘

    [Customer]
        │
        │ 1. Create Order Request
        ▼
┌───────────────────┐
│  OrderService     │───────┐
│  (Coordinator)    │       │ 2. Use Factory
└─────┬─────────────┘       │
      │                     ▼
      │              ┌──────────────┐
      │              │OrderFactory  │
      │              │(Creates Order)│
      │              └──────┬───────┘
      │                     │
      │                     │ 3. Returns Order
      │                     │    with PENDING state
      │              ┌──────▼───────┐
      │              │    Order     │
      │              │  ┌─────────┐ │
      │              │  │ PENDING │ │ ← State Pattern
      │              │  │  State  │ │
      │              │  └─────────┘ │
      │              └──────────────┘
      │                     │
      │ 4. Store Order      │
      ▼                     │
┌────────────────┐          │
│ ConcurrentMap  │          │
│ <ID, Order>    │          │
└────────────────┘          │
      │                     │
      │ 5. Notify Observers │
      ▼                     │
┌────────────────────┐      │
│ PendingOrder       │      │
│   Processor        │◄─────┘
│                    │
│ "I see a PENDING   │
│  order! Schedule   │
│  processing in 5   │
│  minutes"          │
└────────┬───────────┘
         │
         │ 6. Schedule Task
         ▼
┌────────────────────┐
│ ScheduledExecutor  │
│                    │
│ Timer: 5 minutes   │
└────────┬───────────┘
         │
         │ 7. After 5 minutes
         ▼
┌────────────────────┐
│ order.processOrder()│
└────────┬───────────┘
         │
         ▼
┌────────────────────┐
│    Order           │
│  ┌────────────┐    │
│  │PROCESSING  │    │ ← State Pattern changed state
│  │   State    │    │
│  └────────────┘    │
└────────────────────┘
```

### Pattern Collaboration Example

```java
public class SystemIntegrationExample {
    
    public void demonstratePatterns() {
        
        // SETUP: All patterns initialized
        StandardOrderFactory factory = new StandardOrderFactory();
        OrderService service = new OrderService(factory);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        PendingOrderProcessor processor = new PendingOrderProcessor(executor);
        
        // Observer Pattern: Register processor
        service.addObserver(processor);
        
        // Create order items
        List<OrderItem> items = Arrays.asList(
            factory.createOrderItem("PROD-1", 2, new BigDecimal("50.00"))
        );
        
        // ═══════════════════════════════════════════════════════════
        // PATTERN INTERACTION BEGINS
        // ═══════════════════════════════════════════════════════════
        
        // 1. Factory Pattern: Creates order with initial state
        Order order = service.createOrder(items);
        //    ↓
        //    Inside factory:
        //    - new Order(items, new PendingState()) ← State Pattern
        
        // 2. Observer Pattern: Notifies processor
        //    service.notifyObservers(order)
        //    ↓
        //    processor.onOrderStatusChanged(order)
        //    ↓
        //    Checks state: order.getStatus() == PENDING? YES
        //    ↓
        //    Schedules: executor.schedule(processOrder, 5, MINUTES)
        
        // 3. State Pattern: Enforces business rules
        System.out.println(order.getStatus()); // PENDING
        
        // Try to transition to CANCELLED (should work from PENDING)
        boolean canCancel = service.cancelOrder(order.getId());
        System.out.println(canCancel); // true ✅
        //    ↓
        //    Inside cancellation:
        //    order.setState(new CancelledState())
        //    ↓
        //    PendingState.canTransitionTo(CancelledState)? YES ✅
        
        // ═══════════════════════════════════════════════════════════
        // If order wasn't cancelled, after 5 minutes:
        // ═══════════════════════════════════════════════════════════
        
        // Scheduled task runs:
        order.processOrder();
        //    ↓
        //    PendingState.processOrder(order)
        //    ↓
        //    order.setState(new ProcessingState())
        //    ↓
        //    State changed: PENDING → PROCESSING
        
        System.out.println(order.getStatus()); // PROCESSING
        
        // Now try to cancel (should fail)
        boolean canCancelNow = service.cancelOrder(order.getId());
        System.out.println(canCancelNow); // false ❌
        //    ↓
        //    Service checks: order.getStatus() == PENDING? NO
        //    ↓
        //    Returns false without attempting state change
    }
}
```

---

## Key Takeaways

### ✅ What Makes This Design Good?

1. **State Pattern** ensures orders follow valid status transitions
   - Prevents bugs (can't ship a cancelled order)
   - Enforces business rules (can only cancel PENDING orders)

2. **Factory Pattern** ensures orders are created consistently
   - No missing fields
   - Always starts in correct state
   - IDs are always generated

3. **Observer Pattern** decouples concerns
   - OrderService doesn't need to know about background jobs
   - Easy to add new behaviors (email notifications, inventory updates)
   - Clean separation of responsibilities

4. **Thread-Safe Storage**
   - ConcurrentHashMap allows multiple customers to place orders simultaneously
   - No race conditions

### 🎯 How to Remember This

Think of it like a restaurant:

- **Factory Pattern** = Kitchen (consistently prepares orders)
- **State Pattern** = Order status board (Order Placed → Cooking → Ready → Delivered)
- **Observer Pattern** = Kitchen bell (notifies staff when order is ready)
- **OrderService** = Waiter (coordinates everything)

---

## Questions to Test Your Understanding

1. **Why can't we cancel an order in PROCESSING status?**
   - Answer: The State Pattern enforces this via `ProcessingState.canTransitionTo()` which only allows transition to `ShippedState`, not `CancelledState`.

2. **What happens if two customers try to create orders at the same time?**
   - Answer: ConcurrentHashMap handles this safely. Each order gets a unique ID and is stored independently.

3. **How does the system know to process PENDING orders every 5 minutes?**
   - Answer: The Observer Pattern. When an order is created, `PendingOrderProcessor` is notified, sees it's PENDING, and schedules a processing task using `ScheduledExecutorService`.

4. **What's the benefit of using Factory instead of `new Order()` directly?**
   - Answer: Factory ensures orders are always created correctly with proper initialization, initial state, and generated ID. Centralizes creation logic.

---

**Document Version**: 1.0  
**Last Updated**: October 24, 2025  
**Author**: System Documentation Team
