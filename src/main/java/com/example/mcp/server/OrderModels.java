package com.example.mcp.server;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

public final class OrderModels {
    private OrderModels() {}

    public enum OrderState { CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED }
    public enum ReplacementReason { DAMAGED, MISSING_ITEM, DEFECTIVE }

    @Schema(description = "Current fulfillment and delivery status of a single order")
    public record OrderStatusResult(
            @Schema(description = "Stable order ID", example = "ORD-1001")
            String orderId,
            @Schema(description = "Where the order currently sits in fulfillment", example = "SHIPPED")
            OrderState state,
            @Schema(description = "Expected delivery date", example = "2026-09-12")
            LocalDate estimatedDeliveryDate,
            @Schema(description = "When this status was last refreshed from the source system")
            Instant lastUpdatedAt,
            @Schema(description = "System of record the status was read from", example = "demo-order-db")
            String sourceSystem) {}

    @Schema(description = "Replacement request created for an order")
    public record ReplacementResult(
            @Schema(description = "ID of the replacement request", example = "REP-123456")
            String requestId,
            @Schema(description = "Order the replacement was raised against", example = "ORD-1001")
            String orderId,
            @Schema(description = "Why the replacement was requested", example = "DAMAGED")
            ReplacementReason reason,
            @Schema(description = "Status of the replacement request", example = "CREATED")
            String status,
            @Schema(description = "When the request was created")
            Instant createdAt) {}

    @Schema(description = "Body for creating a replacement request")
    public record ReplacementRequest(
            @Schema(
                description = "Why the replacement is needed",
                example = "DAMAGED",
                requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull
            ReplacementReason reason,

            @Schema(
                description = "Caller-generated key that makes the request safe to retry. "
                        + "Repeating a key returns the original replacement instead of creating a second one.",
                example = "b7f3c1e2-0f2a-4c5d-9c1a-1f2b3c4d5e6f",
                requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String idempotencyKey) {}
}
