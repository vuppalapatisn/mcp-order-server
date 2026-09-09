package com.example.mcp.server;

/**
 * Raised when an order is missing or not visible to the calling tenant. Extends
 * {@link IllegalArgumentException} so existing MCP clients keep seeing the same
 * error shape they did before the REST API was added.
 */
public class OrderNotFoundException extends IllegalArgumentException {
    public OrderNotFoundException(String orderId) {
        super("Order not found or not authorized: " + orderId);
    }
}
