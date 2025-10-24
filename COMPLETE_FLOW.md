# Complete Order Flow - Pattern Integration Example

## Scenario: Customer Places and Processes an Order

This document demonstrates how all patterns work together in a complete flow, from order creation to delivery.

## 1. Initial Order Creation Flow

```java
// 1. Client code initiates order creation
public class OrderController {
    private final OrderCommandInvoker commandInvoker;
    private final OrderFactory orderFactory;

    public String createOrder(List<OrderItemRequest> itemRequests) {
        // Using Factory Pattern to create order items
        List<OrderItem> items = itemRequests.stream()
            .map(req -> orderFactory.createOrderItem(
                req.getProductId(), 
                req.getQuantity(), 
                req.getPrice()))
            .collect(Collectors.toList());

        // Using Command Pattern to create and execute order
        CreateOrderCommand createCommand = new CreateOrderCommand(orderService, items);
        commandInvoker.addCommand(createCommand);
        commandInvoker.processCommands();

        return createCommand.getCreatedOrderId();
    }
}

// 2. Command Pattern Implementation
public class CreateOrderCommand implements OrderCommand {
    private final OrderService orderService;
    private final List<OrderItem> items;
    private Order createdOrder;

    @Override
    public void execute() {
        // Using Factory Pattern to create the order
        createdOrder = orderFactory.createOrder(items);
        
        // Using Observer Pattern to notify system of new order
        orderService.saveOrder(createdOrder);
    }

    @Override
    public void undo() {
        if (createdOrder != null) {
            orderService.cancelOrder(createdOrder.getId());
        }
    }
}

// 3. Factory creates order with initial state
public class StandardOrderFactory implements OrderFactory {
    @Override
    public Order createOrder(List<OrderItem> items) {
        Order order = new Order();
        order.setItems(ImmutableList.copyOf(items));  // Immutable items list
        order.setState(new PendingState());  // State Pattern
        order.setId(generateOrderId());
        return order;
    }
}
```

## 2. Order Processing Flow

```java
// 1. Background Processing Observer
public class PendingOrderProcessor implements OrderObserver {
    private final OrderProcessor processor;
    private final ScheduledExecutorService executor;

    @Override
    public void onOrderStatusChanged(Order order) {
        if (order.getState() instanceof PendingState) {
            // Schedule processing after 5 minutes
            executor.schedule(
                () -> processOrder(order),
                5, TimeUnit.MINUTES
            );
        }
    }

    private void processOrder(Order order) {
        // Using Strategy Pattern to determine processing approach
        OrderProcessingStrategy strategy = determineStrategy(order);
        processor.setStrategy(strategy);
        processor.process(order);
    }

    private OrderProcessingStrategy determineStrategy(Order order) {
        return order.isPriority() ? 
            new PriorityProcessingStrategy() : 
            new StandardProcessingStrategy();
    }
}

// 2. State Transition Implementation
public class ProcessingState implements OrderState {
    @Override
    public void processOrder(Order order) {
        // Process the order
        if (validateProcessing(order)) {
            order.setState(new ShippedState());
        }
    }

    @Override
    public boolean canTransitionTo(OrderState newState) {
        return newState instanceof ShippedState || 
               newState instanceof CancelledState;
    }
}
```

## 3. Complete Flow Example

```java
// Example showing all patterns working together
public class OrderProcessingExample {
    public static void demonstrate() {
        // 1. Setup components
        OrderFactory factory = new StandardOrderFactory();
        OrderCommandInvoker invoker = new OrderCommandInvoker();
        OrderProcessor processor = new OrderProcessor();
        
        // 2. Create order items using Factory
        OrderItem item1 = factory.createOrderItem("PROD-1", 2, new BigDecimal("29.99"));
        OrderItem item2 = factory.createOrderItem("PROD-2", 1, new BigDecimal("49.99"));
        List<OrderItem> items = Arrays.asList(item1, item2);

        // 3. Create order using Command Pattern
        CreateOrderCommand createCommand = new CreateOrderCommand(orderService, items);
        invoker.addCommand(createCommand);
        invoker.processCommands();
        Order order = createCommand.getCreatedOrder();

        // 4. Order starts in PENDING state (State Pattern)
        assert order.getState() instanceof PendingState;

        // 5. Observer notices new PENDING order
        orderObserver.onOrderStatusChanged(order);  // Triggers scheduled processing

        // 6. After 5 minutes, processing begins
        // Strategy Pattern determines processing approach
        OrderProcessingStrategy strategy = new StandardProcessingStrategy();
        processor.setStrategy(strategy);
        processor.process(order);

        // 7. State transitions through the flow
        assert order.getState() instanceof ProcessingState;
        // ... processing continues ...
        assert order.getState() instanceof ShippedState;
        // ... delivery occurs ...
        assert order.getState() instanceof DeliveredState;
    }
}
```

## Real-Time Flow Visualization

```
┌─────────────────┐     ┌──────────────────┐     ┌────────────────┐
│  Client Request │ --> │ OrderController  │ --> │ CommandInvoker │
└─────────────────┘     └──────────────────┘     └────────────────┘
                                                         │
                                                         ▼
┌─────────────────┐     ┌──────────────────┐     ┌────────────────┐
│   OrderFactory  │ <-- │  CreateCommand   │ --> │  OrderService  │
└─────────────────┘     └──────────────────┘     └────────────────┘
        │                                                │
        ▼                                                ▼
┌─────────────────┐     ┌──────────────────┐     ┌────────────────┐
│    New Order    │ --> │   PendingState   │ --> │OrderObserver(s)│
└─────────────────┘     └──────────────────┘     └────────────────┘
                                                         │
                                                         ▼
┌─────────────────┐     ┌──────────────────┐     ┌────────────────┐
│OrderProcessor   │ <-- │ProcessingStrategy│ <-- │  Scheduled Job  │
└─────────────────┘     └──────────────────┘     └────────────────┘
```

## Data Flow Through Patterns

1. **Command Pattern (Entry Point)**
   - Receives order request
   - Creates command object
   - Queues for execution

2. **Factory Pattern (Creation)**
   - Creates order and items
   - Sets initial state
   - Ensures proper initialization

3. **State Pattern (Status Management)**
   - Manages order status
   - Enforces valid transitions
   - Handles state-specific behavior

4. **Observer Pattern (Background Processing)**
   - Monitors new orders
   - Schedules processing
   - Notifies relevant components

5. **Strategy Pattern (Processing Logic)**
   - Determines processing approach
   - Executes processing logic
   - Handles different order types

## Key Benefits of Integration

1. **Separation of Concerns**
   - Each pattern handles specific responsibility
   - Clean, maintainable code
   - Easy to modify individual components

2. **Flexibility**
   - Easy to add new order types
   - Easy to modify processing strategies
   - Easy to add new status types

3. **Testability**
   - Each component can be tested in isolation
   - Clear boundaries between components
   - Easy to mock dependencies

4. **Scalability**
   - Components can be distributed
   - Easy to add new features
   - Clear extension points

Would you like me to:
1. Elaborate on any specific part of the flow?
2. Show more detailed implementations of any component?
3. Add more examples of pattern interactions?
4. Start implementing the actual classes?