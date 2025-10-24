# Order Processing System - Visual Diagrams Reference

## Quick Reference: All System Diagrams

---

## 1. State Transition Diagram

### Valid Order Status Flow

```
                    ┌──────────────────────────────────────┐
                    │         ORDER LIFECYCLE              │
                    └──────────────────────────────────────┘

                         ╔═══════════╗
                         ║  PENDING  ║ ← Order Created
                         ╚═════╤═════╝
                               │
                    ┌──────────┼──────────┐
                    │          │          │
                    │          │          │
         Can Cancel │          │ After    │ Cannot Cancel
         (User)     │          │ 5 min    │ (Auto)
                    │          │          │
                    ▼          ▼          │
              ╔═══════════╗  ╔═══════════╗│
              ║ CANCELLED ║  ║PROCESSING ║│
              ╚═══════════╝  ╚═════╤═════╝│
                    ║              │      │
                    ║              │      │
              [FINAL STATE]        │      │
                                   │      │
                                   ▼      │
                             ╔═══════════╗│
                             ║  SHIPPED  ║│
                             ╚═════╤═════╝│
                                   │      │
                                   │      │
                                   ▼      │
                             ╔═══════════╗│
                             ║ DELIVERED ║│
                             ╚═══════════╝│
                                   ║      │
                             [FINAL STATE]│
                                          │
                                          │
                          ╔═══════════════════════════════╗
                          ║  CANCELLATION RULES:          ║
                          ║  ✅ PENDING → CANCELLED      ║
                          ║  ❌ PROCESSING → CANCELLED   ║
                          ║  ❌ SHIPPED → CANCELLED      ║
                          ║  ❌ DELIVERED → CANCELLED    ║
                          ╚═══════════════════════════════╝
```

---

## 2. System Component Architecture

```
╔══════════════════════════════════════════════════════════════╗
║                      CLIENT / API LAYER                       ║
║  (REST endpoints, CLI, or Main application)                  ║
╚═════════════════════════╤════════════════════════════════════╝
                          │
                          │ HTTP / Method Calls
                          │
╔═════════════════════════▼════════════════════════════════════╗
║                     SERVICE LAYER                             ║
║  ┌────────────────────────────────────────────────────────┐ ║
║  │              OrderService (Central Hub)                 │ ║
║  │  ┌──────────────────────────────────────────────────┐  │ ║
║  │  │ Methods:                                          │  │ ║
║  │  │  • createOrder(items): Order                      │  │ ║
║  │  │  • getOrder(id): Optional<Order>                  │  │ ║
║  │  │  • getAllOrders(): List<Order>                    │  │ ║
║  │  │  • getOrdersByStatus(status): List<Order>         │  │ ║
║  │  │  • cancelOrder(id): boolean                       │  │ ║
║  │  └──────────────────────────────────────────────────┘  │ ║
║  └────────────────────────────────────────────────────────┘ ║
╚═══╤════════════════════════╤════════════════════════╤════════╝
    │                        │                        │
    │                        │                        │
╔═══▼════════════╗    ╔══════▼════════════╗    ╔═════▼═══════╗
║ FACTORY        ║    ║ OBSERVER          ║    ║ STORAGE     ║
║ PATTERN        ║    ║ PATTERN           ║    ║ LAYER       ║
║                ║    ║                   ║    ║             ║
║ ┌────────────┐ ║    ║ ┌───────────────┐║    ║┌───────────┐║
║ │ Standard   │ ║    ║ │PendingOrder   │║    ║│Concurrent │║
║ │ Order      │ ║    ║ │Processor      │║    ║│  HashMap  │║
║ │ Factory    │ ║    ║ │               │║    ║│           │║
║ └────────────┘ ║    ║ │Watches PENDING│║    ║│<OrderID,  │║
║                ║    ║ │Schedules auto │║    ║│ Order>    │║
║ Creates:       ║    ║ │processing     │║    ║│           │║
║ • Orders       ║    ║ │every 5 min    │║    ║│Thread-safe│║
║ • OrderItems   ║    ║ └───────────────┘║    ║└───────────┘║
║ • Initial State║    ║                   ║    ║             ║
╚════════════════╝    ╚═══════════════════╝    ╚═════════════╝
    │
    │ Creates with
    │
╔═══▼══════════════════════════════════════════════════════════╗
║                    DOMAIN MODEL LAYER                         ║
║  ┌────────────────────────────────────────────────────────┐  ║
║  │                  Order (Entity)                         │  ║
║  │  ┌──────────────────────────────────────────────────┐  │  ║
║  │  │ - id: String (UUID)                               │  │  ║
║  │  │ - items: List<OrderItem> (Immutable)             │  │  ║
║  │  │ - currentState: OrderState                        │  │  ║
║  │  │ - createdAt: LocalDateTime                        │  │  ║
║  │  │ - lastModifiedAt: LocalDateTime                   │  │  ║
║  │  │ - totalAmount: BigDecimal                         │  │  ║
║  │  └──────────────────────────────────────────────────┘  │  ║
║  └──────────────────────┬─────────────────────────────────┘  ║
║                         │                                     ║
║                         │ Delegates status behavior to        ║
║                         │                                     ║
║  ┌──────────────────────▼─────────────────────────────────┐  ║
║  │           STATE PATTERN (Status Management)            │  ║
║  │                                                         │  ║
║  │    ┌─────────┐      ┌────────────┐      ┌─────────┐   │  ║
║  │    │ PENDING │─────►│ PROCESSING │─────►│ SHIPPED │   │  ║
║  │    └────┬────┘      └────────────┘      └────┬────┘   │  ║
║  │         │                                     │        │  ║
║  │         │                                     ▼        │  ║
║  │         │                               ┌──────────┐   │  ║
║  │         │                               │DELIVERED │   │  ║
║  │         │                               └──────────┘   │  ║
║  │         ▼                                              │  ║
║  │    ┌──────────┐                                        │  ║
║  │    │CANCELLED │                                        │  ║
║  │    └──────────┘                                        │  ║
║  └─────────────────────────────────────────────────────────┘  ║
╚═══════════════════════════════════════════════════════════════╝
```

---

## 3. Data Flow: Create Order Request

```
┌──────────┐
│ Customer │ "I want to buy 2 items"
└─────┬────┘
      │
      │ POST /orders + item data
      │
      ▼
┌─────────────────────────────────────────────────────┐
│             OrderService.createOrder()              │
│                                                     │
│ Step 1: Validate input ✓                           │
└───────────────────────┬─────────────────────────────┘
                        │
                        │ "Create an order for me"
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│         OrderFactory.createOrder(items)             │
│                                                     │
│ Step 2:                                             │
│  • Generate UUID → "550e8400..."                   │
│  • Create Order object                              │
│  • Set items (immutable list)                       │
│  • Set state → new PendingState()                  │
│  • Calculate total amount                           │
│  • Set timestamps                                   │
└───────────────────────┬─────────────────────────────┘
                        │
                        │ Returns: Order object
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│     orders.put(order.getId(), order)                │
│                                                     │
│ Step 3: Store in ConcurrentHashMap                 │
└───────────────────────┬─────────────────────────────┘
                        │
                        │ Order stored ✓
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│         notifyObservers(order)                      │
│                                                     │
│ Step 4: Tell all observers about new order         │
└───────────────────────┬─────────────────────────────┘
                        │
                        │ Event: "New order created"
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│   PendingOrderProcessor.onOrderStatusChanged()     │
│                                                     │
│ Step 5:                                             │
│  • Check: Is status PENDING? → YES                 │
│  • Schedule task for 5 minutes later               │
│  • executor.schedule(processOrder, 5, MINUTES)     │
└───────────────────────┬─────────────────────────────┘
                        │
                        │ Task scheduled ✓
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│           Return order to customer                  │
│                                                     │
│ Step 6: Response with order details                │
└───────────────────────┬─────────────────────────────┘
                        │
                        │ JSON response
                        │
                        ▼
                  ┌──────────┐
                  │ Customer │ "Order created! ID: 550e8400..."
                  └──────────┘

┌────────────────────────────────────────────────────────┐
│         MEANWHILE... (5 minutes later)                 │
└────────────────────────────────────────────────────────┘
                        │
                        │ Timer fires
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│   ScheduledExecutorService executes task            │
│                                                     │
│   order.processOrder()                              │
└───────────────────────┬─────────────────────────────┘
                        │
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│   PendingState.processOrder(order)                  │
│                                                     │
│   • Validate transition allowed                     │
│   • order.setState(new ProcessingState())          │
│   • Update lastModifiedAt timestamp                 │
└───────────────────────┬─────────────────────────────┘
                        │
                        │
                        ▼
                  ┌──────────┐
                  │  Order   │
                  │ [STATUS: │
                  │PROCESSING│
                  └──────────┘
```

---

## 4. Pattern Interaction Diagram

```
╔════════════════════════════════════════════════════════════════╗
║          HOW ALL THREE PATTERNS WORK TOGETHER                  ║
╚════════════════════════════════════════════════════════════════╝


    ┌───────────────────────────────────────────────────────┐
    │         1. FACTORY PATTERN (Creation)                 │
    │         "I create orders correctly"                   │
    └───────────────────┬───────────────────────────────────┘
                        │
                        │ Creates
                        ▼
               ┌──────────────────┐
               │      Order       │
               │  ┌────────────┐  │
               │  │  PENDING   │←─┼────┐
               │  │   State    │  │    │
               │  └────────────┘  │    │
               └──────────────────┘    │
                        │              │
                        │              │
    ┌───────────────────┼──────────────┼───────────────────┐
    │         2. OBSERVER PATTERN      │                   │
    │         "I watch for new orders" │                   │
    └───────────────────┬──────────────┼───────────────────┘
                        │              │
                        │ Monitors     │
                        ▼              │
          ┌──────────────────────┐    │
          │ PendingOrderProcessor│    │
          │                      │    │
          │ "I see a PENDING     │    │
          │  order! I'll process │    │
          │  it in 5 minutes"    │    │
          └──────────┬───────────┘    │
                     │                │
                     │ Schedules      │
                     ▼                │
          ┌──────────────────────┐   │
          │   After 5 minutes    │   │
          │   Call:              │   │
          │   order.processOrder()│   │
          └──────────┬───────────┘   │
                     │                │
                     │ Triggers       │
                     │                │
    ┌────────────────┼────────────────┼───────────────────┐
    │         3. STATE PATTERN         │                   │
    │         "I manage valid status changes"             │
    └────────────────┬────────────────┴───────────────────┘
                     │
                     │ Validates & Executes
                     ▼
        ┌──────────────────────────────┐
        │ PendingState.processOrder()  │
        │                              │
        │ "Can I transition to         │
        │  PROCESSING? Let me check"   │
        │                              │
        │ canTransitionTo(Processing)  │
        │ → YES ✓                      │
        │                              │
        │ setState(ProcessingState)    │
        └──────────────┬───────────────┘
                       │
                       │ Result
                       ▼
              ┌──────────────────┐
              │      Order       │
              │  ┌────────────┐  │
              │  │PROCESSING  │  │
              │  │   State    │  │
              │  └────────────┘  │
              └──────────────────┘


╔════════════════════════════════════════════════════════════════╗
║                    KEY BENEFITS                                 ║
╠════════════════════════════════════════════════════════════════╣
║ Factory:  Consistent object creation                           ║
║ Observer: Decoupled background processing                      ║
║ State:    Enforced business rules                              ║
║                                                                 ║
║ Together: Clean, maintainable, extensible system               ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 5. Request-Response Flow Comparison

### ✅ Successful Cancellation (PENDING Order)

```
Customer Request              System Processing              Response
─────────────────────────────────────────────────────────────────────

DELETE /orders/123            OrderService.cancelOrder(123)   
"Cancel my order"         →   
                              1. Get order from storage
                                 ↓
Time: 10:02 AM                2. Check status
(2 min after creation)           order.getStatus() → PENDING ✓
                                 ↓
                              3. Validate: Can cancel PENDING? YES
                                 ↓
                              4. Change state
                                 order.setState(new CancelledState())
                                 ↓
                              5. PendingState validates:
                                 canTransitionTo(Cancelled) → TRUE ✓
                                 ↓
                              6. State changed: PENDING → CANCELLED
                              
                          ←   { "success": true,
                                "message": "Order cancelled" }
"Order cancelled! ✓"
```

### ❌ Failed Cancellation (PROCESSING Order)

```
Customer Request              System Processing              Response
─────────────────────────────────────────────────────────────────────

DELETE /orders/123            OrderService.cancelOrder(123)   
"Cancel my order"         →   
                              1. Get order from storage
                                 ↓
Time: 10:07 AM                2. Check status
(7 min after creation)           order.getStatus() → PROCESSING
(Already auto-processed)         ↓
                              3. Validate: Can cancel PROCESSING?
                                 if (status == PENDING) → FALSE ❌
                                 ↓
                              4. Return false
                                 (No state change attempted)
                              
                          ←   { "success": false,
                                "message": "Cannot cancel",
                                "reason": "Order already processing" }
"Cannot cancel ❌"
```

---

## 6. Threading Model

```
┌────────────────────────────────────────────────────────────────┐
│                    MULTI-THREADED SYSTEM                        │
└────────────────────────────────────────────────────────────────┘

┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Customer A  │  │ Customer B  │  │ Customer C  │
│   Thread    │  │   Thread    │  │   Thread    │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       │ Create Order   │ Create Order   │ Get Order
       │                │                │
       └────────────────┼────────────────┘
                        │
                        ▼
┌───────────────────────────────────────────────────────────┐
│              OrderService (Thread-Safe)                    │
│                                                            │
│  Synchronized access via ConcurrentHashMap                │
└───────────────────────┬───────────────────────────────────┘
                        │
                        ▼
┌───────────────────────────────────────────────────────────┐
│         ConcurrentHashMap<String, Order>                  │
│                                                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐               │
│  │Order A   │  │Order B   │  │Order C   │               │
│  │[PENDING] │  │[PENDING] │  │[PROCESS] │               │
│  └──────────┘  └──────────┘  └──────────┘               │
│                                                            │
│  Thread-safe operations - no data corruption!             │
└────────────────────────────────────────────────────────────┘

                        ▲
                        │ Concurrent access safe!
                        │
         ┌──────────────┼──────────────┬──────────────┐
         │              │              │              │
┌────────▼──────┐ ┌────▼────────┐ ┌──▼──────────┐ ┌─▼───────┐
│ Background    │ │Background   │ │Background   │ │Scheduler│
│ Task for      │ │Task for     │ │Task for     │ │ Thread  │
│ Order A       │ │Order B      │ │Order C      │ │         │
│ (5 min timer) │ │(5 min timer)│ │(5 min timer)│ │ Pool    │
└───────────────┘ └─────────────┘ └─────────────┘ └─────────┘
```

---

## 7. Timeline: Complete Order Journey

```
┌─────────────────────────────────────────────────────────────────┐
│          TIMELINE VIEW: ONE ORDER'S COMPLETE JOURNEY            │
└─────────────────────────────────────────────────────────────────┘

10:00 AM │ ┌─────────────────────────────────────────────────┐
         │ │ Customer places order                          │
         │ │ Status: PENDING                                 │
         │ │ System: Factory creates, Observer schedules    │
         │ └─────────────────────────────────────────────────┘
         │
10:01 AM │ [Customer can cancel: ✅ YES]
         │
10:02 AM │ [Customer can cancel: ✅ YES]
         │
10:03 AM │ [Customer can cancel: ✅ YES]
         │
10:04 AM │ [Customer can cancel: ✅ YES]
         │
10:05 AM │ ┌─────────────────────────────────────────────────┐
         │ │ ⚡ AUTOMATIC PROCESSING TRIGGERED                │
         │ │ Background job executes                         │
         │ │ Status: PENDING → PROCESSING                    │
         │ │ State Pattern validates transition ✓            │
         │ └─────────────────────────────────────────────────┘
         │
10:06 AM │ [Customer can cancel: ❌ NO - Too late!]
         │
10:15 AM │ [Status: Still PROCESSING]
         │
10:30 AM │ ┌─────────────────────────────────────────────────┐
         │ │ Warehouse marks as shipped                      │
         │ │ Status: PROCESSING → SHIPPED                    │
         │ └─────────────────────────────────────────────────┘
         │
11:00 AM │ [Status: SHIPPED - In transit]
         │
12:00 PM │ [Status: SHIPPED - In transit]
         │
 1:00 PM │ [Status: SHIPPED - In transit]
         │
 2:00 PM │ ┌─────────────────────────────────────────────────┐
         │ │ Customer receives package                       │
         │ │ Status: SHIPPED → DELIVERED                     │
         │ │ ✓ FINAL STATE - Order complete!                │
         │ └─────────────────────────────────────────────────┘
         │
 2:01 PM │ [Status: DELIVERED - Cannot change anymore]
         │
         ▼


╔════════════════════════════════════════════════════════════════╗
║                     CANCELLATION WINDOW                         ║
╠════════════════════════════════════════════════════════════════╣
║                                                                 ║
║  10:00 AM ────────────────► 10:05 AM                           ║
║     │                          │                                ║
║     │   CAN CANCEL (5 min)     │                               ║
║     │   ✅✅✅✅✅             │                               ║
║     │                          │                                ║
║  Order                    Auto-process                          ║
║  Created                  triggered                             ║
║                                                                 ║
║                                                                 ║
║  10:05 AM ────────────────────────────────► END                ║
║     │                                         │                 ║
║     │    CANNOT CANCEL (Forever)              │                 ║
║     │    ❌❌❌❌❌❌❌❌❌❌                  │                 ║
║     │                                         │                 ║
║  Processing                               Delivered             ║
║  Started                                  (Final)               ║
║                                                                 ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 8. Error Handling Flow

```
┌──────────────────────────────────────────────────────────────┐
│              ERROR SCENARIOS & HANDLING                       │
└──────────────────────────────────────────────────────────────┘


Scenario 1: Cancel Non-Existent Order
═════════════════════════════════════════════════════════════════
Request: DELETE /orders/invalid-id-999

   orderService.cancelOrder("invalid-id-999")
         ↓
   Optional<Order> order = getOrder("invalid-id-999")
         ↓
   Result: Optional.empty()
         ↓
   Return: false (no exception thrown)
         ↓
   Response: { "success": false, "error": "Order not found" }


Scenario 2: Invalid State Transition (Force)
═════════════════════════════════════════════════════════════════
Code: order.setState(new CancelledState())  // Order is PROCESSING

   Order.setState(new CancelledState())
         ↓
   currentState.canTransitionTo(CancelledState)
         ↓
   ProcessingState.canTransitionTo(CancelledState) → FALSE
         ↓
   Throws: IllegalStateException
         ↓
   Message: "Cannot transition from PROCESSING to CANCELLED"


Scenario 3: Empty Order Items
═════════════════════════════════════════════════════════════════
Request: POST /orders { "items": [] }

   Validation should happen BEFORE factory
         ↓
   if (items.isEmpty()) throw new ValidationException()
         ↓
   Response: { "success": false, "error": "Order must have items" }


Scenario 4: Concurrent Modification (Thread-Safe)
═════════════════════════════════════════════════════════════════
Thread A: Cancel order      │  Thread B: Process order
                           │
getOrder(id) ────────────┐  │  getOrder(id) ────────────┐
                         ▼  │                           ▼
Check: status == PENDING?  │  Check: status == PENDING?
        YES ✓              │          YES ✓
                           │
setState(Cancelled) ────┐  │  setState(Processing) ───┐
                        │  │                          │
                        └──┼──► ConcurrentHashMap    │
                           │    (Thread-safe)        │
                           │                          │
Result: ONE wins ─────────┼──────────────────────────┘
        The other fails   │  (Atomic operations ensure
        gracefully        │   data consistency)
```

---

## 9. Memory Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                    IN-MEMORY DATA STRUCTURE                      │
└─────────────────────────────────────────────────────────────────┘

OrderService
    │
    ├─► orders: ConcurrentHashMap
    │       │
    │       ├─► Key: "order-001"
    │       │   Value: Order {
    │       │             id: "order-001"
    │       │             items: [Item1, Item2]
    │       │             currentState: PendingState
    │       │             totalAmount: 1500.00
    │       │             createdAt: 2025-10-24T10:00:00
    │       │           }
    │       │
    │       ├─► Key: "order-002"
    │       │   Value: Order {
    │       │             id: "order-002"
    │       │             items: [Item1]
    │       │             currentState: ProcessingState
    │       │             totalAmount: 750.00
    │       │             createdAt: 2025-10-24T10:05:00
    │       │           }
    │       │
    │       └─► Key: "order-003"
    │           Value: Order { ... }
    │
    └─► observers: List<OrderObserver>
            │
            └─► [0] PendingOrderProcessor
                     │
                     └─► executor: ScheduledExecutorService
                              │
                              ├─► Scheduled Task 1 (order-001)
                              ├─► Scheduled Task 2 (order-002)
                              └─► Scheduled Task 3 (order-003)


Memory Characteristics:
═══════════════════════════════════════════════════════════════════
• Time Complexity: O(1) for order lookup by ID
• Space Complexity: O(n) where n = number of orders
• Thread Safety: ConcurrentHashMap provides lock-free reads
• Scalability: Limited by available RAM
```

---

## Summary: Pattern Responsibilities

```
╔═══════════════════════════════════════════════════════════════╗
║                    PATTERN CHEAT SHEET                        ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  🏭 FACTORY PATTERN                                           ║
║     Responsibility: Create objects consistently               ║
║     Benefit: No invalid orders in system                      ║
║     When: Every order creation                                ║
║                                                               ║
║  ─────────────────────────────────────────────────────────   ║
║                                                               ║
║  🎯 STATE PATTERN                                             ║
║     Responsibility: Manage status transitions                 ║
║     Benefit: Enforces business rules                          ║
║     When: Any status change attempt                           ║
║                                                               ║
║  ─────────────────────────────────────────────────────────   ║
║                                                               ║
║  👁️ OBSERVER PATTERN                                          ║
║     Responsibility: React to order events                     ║
║     Benefit: Decoupled background processing                  ║
║     When: Order created or status changed                     ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

---

**Use these diagrams as quick references while coding or discussing the system!**

**Document Version**: 1.0  
**Last Updated**: October 24, 2025
