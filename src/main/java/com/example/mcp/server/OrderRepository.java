package com.example.mcp.server;

import static com.example.mcp.server.OrderModels.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    private final Map<String, OrderStatusResult> orders = new ConcurrentHashMap<>();
    private final Map<String, ReplacementResult> replacementsByKey = new ConcurrentHashMap<>();

    public OrderRepository() {
        orders.put(key("tenant-a", "ORD-1001"), new OrderStatusResult(
                "ORD-1001", OrderState.SHIPPED, LocalDate.now().plusDays(2), Instant.now(), "demo-order-db"));
        orders.put(key("tenant-b", "ORD-2001"), new OrderStatusResult(
                "ORD-2001", OrderState.PROCESSING, LocalDate.now().plusDays(5), Instant.now(), "demo-order-db"));
    }

    public Optional<OrderStatusResult> find(String tenantId, String orderId) {
        return Optional.ofNullable(orders.get(key(tenantId, orderId)));
    }

    public ReplacementResult createIdempotently(
            String tenantId, String orderId, ReplacementReason reason, String idempotencyKey) {
        String key = tenantId + ":" + idempotencyKey;
        return replacementsByKey.computeIfAbsent(key, ignored -> new ReplacementResult(
                "REP-" + Math.abs(key.hashCode()), orderId, reason, "CREATED", Instant.now()));
    }

    private static String key(String tenantId, String orderId) {
        return tenantId + ":" + orderId;
    }
}
