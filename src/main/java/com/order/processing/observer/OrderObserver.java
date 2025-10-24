package com.order.processing.observer;

import com.order.processing.model.Order;

public interface OrderObserver {
    void onOrderStatusChanged(Order order);
}