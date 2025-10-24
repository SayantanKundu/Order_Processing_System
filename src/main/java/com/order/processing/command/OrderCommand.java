package com.order.processing.command;

public interface OrderCommand {
    void execute();
    void undo();
}