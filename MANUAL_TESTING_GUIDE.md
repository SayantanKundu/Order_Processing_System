# Manual Testing Guide - Order Processing System

## 🎯 Overview

This guide will walk you through testing every single flow manually from the terminal. Each test shows you exactly what commands to run and what debug output to expect.

## ⚙️ Prerequisites

1. **Build the project first:**
```bash
cd /Users/s0k09ns/projects/Order_Processing_System
mvn clean compile
```

2. **Understanding the Debug Logs:**

The system uses color-coded debug logs with timestamps:

- **🔵 [SERVICE]** - OrderService operations (create, cancel, list)
- **🟣 [FACTORY]** - Order/Item creation by Factory Pattern
- **🟡 [OBSERVER]** - Background processing, scheduling
- **🔴 [STATE]** - State transitions and validations
- **🟢 [MODEL]** - Order/OrderItem calculations
- **⚪ [MAIN]** - Application-level operations

Format: `[HH:mm:ss.SSS] [CATEGORY] [Component] Message`

---

## 📋 Test Scenarios

### Test 1: Create an Order (Happy Path)

**What we're testing:** Basic order creation with Factory and Observer patterns

**Steps:**

1. **Run the application in interactive mode:**
```bash
mvn exec:java -Dexec.mainClass="Main"
```

2. **Choose option `1` - Create Order**

3. **Enter items when prompted:**
```
Product ID: LAPTOP-001
Quantity: 2
Price: 999.99
Add another? (y/n): y

Product ID: MOUSE-001
Quantity: 1
Price: 29.99
Add another? (n): n
```

**Expected Debug Logs Flow:**

```
[10:30:15.234] [SERVICE] [createOrder] Received request with 2 items
[10:30:15.235] [FACTORY] [createOrder] Received request to create order with 2 items
[10:30:15.236] [FACTORY] [createOrder] Item 1 validated: LAPTOP-001 (qty: 2, price: $999.99)
[10:30:15.237] [FACTORY] [createOrder] Item 2 validated: MOUSE-001 (qty: 1, price: $29.99)
[10:30:15.238] [FACTORY] [createOrder] Creating Order object with PendingState
[10:30:15.239] [FACTORY] [OrderFactory] Created Order[abc12345] with 2 items, Total: $2029.97
[10:30:15.240] [SERVICE] [createOrder] Storing in ConcurrentHashMap (total orders before: 0)
[10:30:15.241] [SERVICE] [createOrder] Stored successfully (total orders now: 1)
[10:30:15.242] [SERVICE] [notifyObservers] Notifying 1 observer(s) about Order[abc12345]
[10:30:15.243] [SERVICE] [notifyObservers] Calling PendingOrderProcessor.onOrderStatusChanged()
[10:30:15.244] [OBSERVER] [onOrderStatusChanged] Order[abc12345] notification received with status: PENDING
[10:30:15.245] [OBSERVER] [onOrderStatusChanged] Order[abc12345] is PENDING, Scheduling processing in 5 minutes
[10:30:15.246] [OBSERVER] [scheduleProcessing] Order[abc12345] - Creating scheduled task for 5 minutes from now
[10:30:15.247] [OBSERVER] [scheduleProcessing] Order[abc12345] - Task scheduled successfully
```

**What to verify:**
- ✅ Order created with unique ID
- ✅ Total calculated correctly ($999.99 × 2 + $29.99 = $2029.97)
- ✅ Initial state is PENDING
- ✅ Observer notified and task scheduled for 5 minutes

**Copy the Order ID** - you'll need it for next tests!

---

### Test 2: Retrieve Order Details

**What we're testing:** Order lookup by ID

**Steps:**

1. **From the main menu, choose option `2` - Get Order**

2. **Enter the Order ID** from Test 1

**Expected Debug Logs:**

```
[10:31:00.100] [SERVICE] [getOrder] Looking up Order[abc12345] in storage
[10:31:00.101] [SERVICE] [getOrder] Order[abc12345] found in ConcurrentHashMap
```

**Console Output:**

```
Order Details:
══════════════════════════════════════════
Order ID: abc12345-6789-...
Status: PENDING
Items:
  - LAPTOP-001 × 2 @ $999.99 = $1999.98
  - MOUSE-001 × 1 @ $29.99 = $29.99
Total: $2029.97
Created: 2025-10-24T10:30:15
══════════════════════════════════════════
```

**What to verify:**
- ✅ Order found successfully
- ✅ All details match what you entered
- ✅ Status is still PENDING (background job hasn't run yet)

---

### Test 3: Cancel Order (Success - PENDING State)

**What we're testing:** Cancellation business rule - can only cancel PENDING orders

**Steps:**

1. **Choose option `5` - Cancel Order**

2. **Enter the Order ID** from Test 1

**Expected Debug Logs:**

```
[10:32:00.100] [SERVICE] [cancelOrder] Cancel request for Order[abc12345]
[10:32:00.101] [SERVICE] [cancelOrder] Current status: PENDING
[10:32:00.102] [SERVICE] [cancelOrder] Status check passed - PENDING orders can be cancelled
[10:32:00.103] [STATE] [PendingState.canTransitionTo] Checking PENDING → CancelledState: ✓ ALLOWED
[10:32:00.104] [SERVICE] [cancelOrder] State transition successful
[10:32:00.105] [SERVICE] [notifyObservers] Notifying 1 observer(s) about Order[abc12345]
[10:32:00.106] [OBSERVER] [onOrderStatusChanged] Order[abc12345] notification received with status: CANCELLED
[10:32:00.107] [OBSERVER] [onOrderStatusChanged] Order[abc12345] is not PENDING, no action taken
[10:32:00.108] [SERVICE] [cancelOrder] Order[abc12345] - Cancellation completed successfully
```

**What to verify:**
- ✅ State Pattern allowed PENDING → CANCELLED transition
- ✅ Observer was notified but did not schedule processing (order is cancelled)
- ✅ Service confirms cancellation success

---

### Test 4: Try to Cancel Non-PENDING Order (Business Rule Enforcement)

**What we're testing:** State Pattern preventing invalid cancellations

**Setup:** We need an order in PROCESSING state

1. **Create a new order** (Test 1 steps)
2. **Wait 5 minutes** for auto-processing OR manually process it (if you add a manual process option)

For quick testing, let's use the quick demo mode:

```bash
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="--quick-demo"
```

This will create an order and auto-process it after 1 minute.

**Expected Flow:**

```
[10:35:00.000] [FACTORY] [OrderFactory] Created Order[def67890] with 2 items
[10:35:00.001] [OBSERVER] [scheduleProcessing] Order[def67890] - Task scheduled for 1 minute

... Wait 1 minute ...

[10:36:00.000] [OBSERVER] [processOrder] Order[def67890] - Scheduled task triggered
[10:36:00.001] [OBSERVER] [processOrder] Order[def67890] - Status is still PENDING, proceeding
[10:36:00.002] [STATE] [PendingState.processOrder] Order[def67890] - Starting transition PENDING → PROCESSING
[10:36:00.003] [STATE] [ProcessingState.canTransitionTo] Checking PROCESSING → PROCESSING: ✓ ALLOWED
[10:36:00.004] [STATE] [PendingState.processOrder] Order[def67890] - Transition completed
[10:36:00.005] [OBSERVER] [processOrder] Order[def67890] - Auto-processing completed
```

Now try to cancel this PROCESSING order:

**Steps:**

1. **Choose option `5` - Cancel Order**
2. **Enter the Order ID**

**Expected Debug Logs:**

```
[10:37:00.000] [SERVICE] [cancelOrder] Cancel request for Order[def67890]
[10:37:00.001] [SERVICE] [cancelOrder] Current status: PROCESSING
[10:37:00.002] [SERVICE] [cancelOrder] Status check FAILED - Only PENDING orders can be cancelled
[10:37:00.003] [STATE] [ProcessingState.canTransitionTo] Checking PROCESSING → CancelledState: ✗ DENIED
[10:37:00.004] [SERVICE] [cancelOrder] Order[def67890] - Cancellation denied (not PENDING)
```

**Console Output:**

```
✗ Cannot cancel order: Order is in PROCESSING state
  Cancellation is only allowed for PENDING orders
```

**What to verify:**
- ✅ Service checked status BEFORE attempting state change
- ✅ State Pattern would have blocked it anyway (defense in depth)
- ✅ User gets clear error message
- ✅ Order remains in PROCESSING state

---

### Test 5: List All Orders

**What we're testing:** Storage and filtering capabilities

**Steps:**

1. **Create 3 orders** with different statuses:
   - Order A: Create and leave PENDING
   - Order B: Create and cancel (CANCELLED)
   - Order C: Create and wait for auto-processing (PROCESSING)

2. **Choose option `3` - List All Orders**

**Expected Debug Logs:**

```
[10:40:00.000] [SERVICE] [getAllOrders] Retrieving all orders from storage
[10:40:00.001] [SERVICE] [getAllOrders] Found 3 orders in ConcurrentHashMap
```

**Console Output:**

```
All Orders (3 total):
══════════════════════════════════════════
1. Order[abc12345...] - CANCELLED - $2029.97
2. Order[def67890...] - PROCESSING - $1500.00
3. Order[ghi11111...] - PENDING - $750.50
══════════════════════════════════════════
```

---

### Test 6: List Orders by Status (Filtered)

**Steps:**

1. **Choose option `4` - List Orders by Status**
2. **Enter status: PENDING**

**Expected Debug Logs:**

```
[10:41:00.000] [SERVICE] [getOrdersByStatus] Filtering orders by status: PENDING
[10:41:00.001] [SERVICE] [getOrdersByStatus] Checking Order[abc12345]: CANCELLED - Skip
[10:41:00.002] [SERVICE] [getOrdersByStatus] Checking Order[def67890]: PROCESSING - Skip
[10:41:00.003] [SERVICE] [getOrdersByStatus] Checking Order[ghi11111]: PENDING - Include
[10:41:00.004] [SERVICE] [getOrdersByStatus] Found 1 order(s) with status PENDING
```

**What to verify:**
- ✅ Stream filtering works correctly
- ✅ Only PENDING orders returned

---

### Test 7: Background Processing (Observer Pattern)

**What we're testing:** Automatic PENDING → PROCESSING after 5 minutes

**Setup:** For real-time testing with 5 minutes:

1. **Create a new order** (it will be PENDING)
2. **Set a timer for 5 minutes**
3. **Watch the console logs**

**OR for quick testing (1 minute):**

```bash
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="--quick-demo"
```

**Expected Timeline:**

```
T+0s: Order Created
════════════════════════════════════════════════════
[10:45:00.000] [FACTORY] [OrderFactory] Created Order[jkl22222]
[10:45:00.001] [OBSERVER] [scheduleProcessing] Order[jkl22222] - Task scheduled for 5 minutes

T+5m: Background Job Executes
════════════════════════════════════════════════════
[10:50:00.000] [OBSERVER] [processOrder] Order[jkl22222] - Scheduled task triggered
[10:50:00.001] [OBSERVER] [processOrder] Order[jkl22222] - Checking current status
[10:50:00.002] [OBSERVER] [processOrder] Order[jkl22222] - Status is still PENDING
[10:50:00.003] [STATE] [PendingState.processOrder] Order[jkl22222] - Starting transition
[10:50:00.004] [STATE] [PendingState.canTransitionTo] Checking PENDING → ProcessingState: ✓ ALLOWED
[10:50:00.005] [STATE] [ProcessingState] Order[jkl22222] - Entered PROCESSING state
[10:50:00.006] [OBSERVER] [processOrder] Order[jkl22222] - Auto-processing completed
```

**What to verify:**
- ✅ Observer scheduled task exactly 5 minutes in the future
- ✅ Task executed at the right time
- ✅ State transition happened correctly
- ✅ If order was cancelled before 5 minutes, processing should be skipped

---

### Test 8: Complete Order Lifecycle

**What we're testing:** Full journey from PENDING to DELIVERED

This test demonstrates all state transitions:

```
PENDING → PROCESSING → SHIPPED → DELIVERED
```

**Steps:**

1. **Create an order** (starts as PENDING)

**Debug Logs:**
```
[11:00:00.000] [FACTORY] Created Order[mno33333] in PENDING state
```

2. **Wait 5 minutes** for auto-processing (or use quick demo)

**Debug Logs:**
```
[11:05:00.000] [OBSERVER] Auto-processing Order[mno33333]
[11:05:00.001] [STATE] PENDING → PROCESSING ✓
```

3. **Manually ship the order:**
   - You'll need to add a "Ship Order" option in Main.java
   - OR use the test mode with manual state transitions

**Debug Logs:**
```
[11:10:00.000] [STATE] [ProcessingState.processOrder] Order[mno33333] - Starting transition PROCESSING → SHIPPED
[11:10:00.001] [STATE] [ProcessingState.canTransitionTo] Checking PROCESSING → ShippedState: ✓ ALLOWED
[11:10:00.002] [STATE] [ShippedState] Order[mno33333] - Entered SHIPPED state
```

4. **Mark as delivered:**

**Debug Logs:**
```
[11:15:00.000] [STATE] [ShippedState.processOrder] Order[mno33333] - Starting transition SHIPPED → DELIVERED
[11:15:00.001] [STATE] [ShippedState.canTransitionTo] Checking SHIPPED → DeliveredState: ✓ ALLOWED
[11:15:00.002] [STATE] [DeliveredState] Order[mno33333] - Entered DELIVERED state (FINAL)
```

**What to verify:**
- ✅ Each state transition is logged
- ✅ State Pattern validates each transition
- ✅ DELIVERED is a final state (no more transitions possible)

---

### Test 9: Inspect Order in Memory (Debug Mode)

**What we're testing:** Understanding the ConcurrentHashMap storage

**Steps:**

1. **Create multiple orders** (at least 3)

2. **Add this debug option to Main.java:**
```java
case 6:
    System.out.println("\n=== SYSTEM DEBUG INFO ===");
    System.out.println("Total orders in memory: " + orderService.getOrderCount());
    System.out.println("\nOrder Map Contents:");
    orderService.getAllOrders().forEach(order -> {
        System.out.println(String.format(
            "  Key: %s → Order[status=%s, items=%d, total=$%s]",
            order.getId(),
            order.getStatus(),
            order.getItemCount(),
            order.getTotalAmount()
        ));
    });
    break;
```

**Expected Output:**

```
=== SYSTEM DEBUG INFO ===
Total orders in memory: 3

Order Map Contents:
  Key: abc12345-6789-... → Order[status=CANCELLED, items=2, total=$2029.97]
  Key: def67890-1234-... → Order[status=PROCESSING, items=1, total=$1500.00]
  Key: ghi11111-5678-... → Order[status=PENDING, items=3, total=$750.50]
```

**What to verify:**
- ✅ All orders are in the ConcurrentHashMap
- ✅ Keys are full order IDs
- ✅ Values are complete Order objects
- ✅ Each order maintains its own state

---

## 🔍 Understanding the Debug Output

### Color Codes (in terminal)

- **Cyan**: State Pattern operations
- **Magenta**: Factory Pattern operations
- **Yellow**: Observer Pattern operations
- **Blue**: Service layer operations
- **Green**: Model calculations
- **Red**: Errors
- **White**: Main application

### Timestamp Format

`[HH:mm:ss.SSS]` - Shows exact execution time down to milliseconds

### Component Names

- `createOrder`, `cancelOrder`: Service methods
- `processOrder`, `canTransitionTo`: State Pattern methods
- `onOrderStatusChanged`, `scheduleProcessing`: Observer methods
- `OrderFactory`: Factory creation

---

## 🧪 Quick Test Script

Want to test everything at once? Run:

```bash
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="--full-test"
```

This will:
1. Create 3 orders
2. Cancel one immediately
3. Wait for auto-processing on another
4. Try to cancel the processed order (should fail)
5. List all orders
6. Show final state of all orders

---

## 📊 Test Result Checklist

After completing all tests, verify:

- [ ] ✅ **Factory Pattern**: Orders created consistently with correct initialization
- [ ] ✅ **State Pattern**: All valid transitions allowed, invalid transitions blocked
- [ ] ✅ **Observer Pattern**: Background processing scheduled and executed correctly
- [ ] ✅ **Business Rule**: Only PENDING orders can be cancelled
- [ ] ✅ **Thread Safety**: ConcurrentHashMap handled concurrent operations
- [ ] ✅ **Storage**: All orders stored and retrievable by ID
- [ ] ✅ **Filtering**: Can list orders by status
- [ ] ✅ **Complete Lifecycle**: Orders can move through all states to DELIVERED

---

## 🎯 Next Steps

1. **Save debug logs**: Redirect output to a file
   ```bash
   mvn exec:java -Dexec.mainClass="Main" 2>&1 | tee test_output.log
   ```

2. **Analyze patterns**: Look for the sequence of debug messages

3. **Verify timing**: Check timestamps to confirm 5-minute delays

4. **Test edge cases**:
   - Create order with 0 items (should fail)
   - Cancel order with invalid ID (should fail gracefully)
   - Try to transition DELIVERED to another state (should fail)

---

**Happy Testing! 🚀**

For questions or issues, check the debug logs - they tell the complete story of what's happening in your system.
