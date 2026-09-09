package com.example.mcp.server;

import static com.example.mcp.server.OrderModels.*;

import org.springframework.stereotype.Service;

/**
 * Single place where order use cases live, so the MCP tools and the REST API
 * always behave identically.
 */
@Service
public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public OrderStatusResult getOrderStatus(String orderId) {
        RequestIdentity identity = RequestIdentity.demoIdentity();
        return repository.find(identity.tenantId(), orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public ReplacementResult createReplacement(
            String orderId, ReplacementReason reason, String idempotencyKey) {

        RequestIdentity identity = RequestIdentity.demoIdentity();
        repository.find(identity.tenantId(), orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return repository.createIdempotently(identity.tenantId(), orderId, reason, idempotencyKey);
    }
}
