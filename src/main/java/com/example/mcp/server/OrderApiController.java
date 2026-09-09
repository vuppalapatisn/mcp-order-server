package com.example.mcp.server;

import static com.example.mcp.server.OrderModels.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP surface for the same order operations exposed as MCP tools, so the API can
 * be browsed and exercised through Swagger UI at /swagger-ui.html.
 */
@RestController
@RequestMapping(path = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Orders", description = "Read order status and raise replacement requests")
public class OrderApiController {
    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order status",
        description = "Returns the current fulfillment and delivery status for one order. Read-only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order status returned"),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found or not authorized for the caller",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OrderStatusResult getOrderStatus(
            @Parameter(description = "Stable order ID", example = "ORD-1001")
            @PathVariable String orderId) {

        return orderService.getOrderStatus(orderId);
    }

    @PostMapping(path = "/{orderId}/replacements", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a replacement request",
        description = "Creates a replacement request for an eligible order. This changes business "
                + "state, so callers must have explicit user approval. The idempotency key makes "
                + "retries safe: reusing a key returns the original request.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Replacement request created (or replayed)"),
        @ApiResponse(
            responseCode = "400",
            description = "Request body failed validation",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found or not authorized for the caller",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ReplacementResult createReplacement(
            @Parameter(description = "Stable order ID", example = "ORD-1001")
            @PathVariable String orderId,
            @Valid @RequestBody ReplacementRequest request) {

        return orderService.createReplacement(orderId, request.reason(), request.idempotencyKey());
    }
}
