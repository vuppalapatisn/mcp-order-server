package com.example.mcp.server;

import java.time.Instant;
import java.time.LocalDate;

public final class OrderModels {
    private OrderModels() {}

    public enum OrderState { CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED }
    public enum ReplacementReason { DAMAGED, MISSING_ITEM, DEFECTIVE }

    public record OrderStatusResult(
            String orderId,
            OrderState state,
            LocalDate estimatedDeliveryDate,
            Instant lastUpdatedAt,
            String sourceSystem) {}

    public record ReplacementResult(
            String requestId,
            String orderId,
            ReplacementReason reason,
            String status,
            Instant createdAt) {}
}
