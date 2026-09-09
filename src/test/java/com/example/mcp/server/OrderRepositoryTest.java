package com.example.mcp.server;

import static com.example.mcp.server.OrderModels.ReplacementReason.DAMAGED;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class OrderRepositoryTest {
    @Test
    void replacementCreationIsIdempotent() {
        OrderRepository repository = new OrderRepository();
        var first = repository.createIdempotently("tenant-a", "ORD-1001", DAMAGED, "same-key");
        var second = repository.createIdempotently("tenant-a", "ORD-1001", DAMAGED, "same-key");
        assertThat(second.requestId()).isEqualTo(first.requestId());
    }
}
