# mcp-order-server

[![Build](https://github.com/vuppalapatisn/mcp-order-server/actions/workflows/build.yml/badge.svg)](https://github.com/vuppalapatisn/mcp-order-server/actions/workflows/build.yml)

Spring Boot 4 service that exposes demo order operations two ways:

- **MCP tools** (`spring-ai-starter-mcp-server-webmvc`, streamable HTTP) — `orders_get_status` and `orders_create_replacement`
- **REST API** documented with OpenAPI 3 / Swagger UI

Both surfaces call the same `OrderService`, so they cannot drift apart.

## Run it

```bash
mvn spring-boot:run
```

The service listens on port `8081`.

| Surface | URL |
| --- | --- |
| Landing page | http://localhost:8081/ |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | http://localhost:8081/v3/api-docs |
| Health | http://localhost:8081/actuator/health |

## MCP transport

`spring.ai.mcp.server.protocol` selects the transport and **only one is served at a time**.
Override it per deployment with the `MCP_PROTOCOL` environment variable — no rebuild needed.

| `MCP_PROTOCOL` | Client URL | `GET /mcp` returns | Session handling |
| --- | --- | --- | --- |
| `STREAMABLE` (default) | `POST /mcp` | `400 Invalid Accept header` | `mcp-session-id` header required after `initialize` |
| `SSE` | `GET /sse` then `POST /mcp/message` | `404` — `/mcp` does not exist | `sessionId` query param from the `endpoint` event |
| `STATELESS` | `POST /mcp` | `405 Method Not Allowed` | none — no handshake needed |

`/mcp` speaks JSON-RPC, not HTML. Opening it in a browser sends `Accept: text/html` and is
**supposed** to fail — that is not a deployment problem. The service root (`/`) serves a
landing page that reports the active protocol and the correct URL.

### Picking a protocol

- **`STATELESS` is the most forgiving** and the best default for simple request/response
  tools like these: plain JSON replies, no session to lose, no SSE negotiation, and
  `tools/call` works on the very first request. It gives up server-initiated notifications,
  which neither tool here uses. It also survives Render's free-tier spin-down, which
  otherwise discards `STREAMABLE` sessions.
- **`STREAMABLE`** is the current MCP standard — use it for clients that support it.
- **`SSE`** is the legacy transport. Point the client at `/sse`, **not** `/mcp`.

### MCP Inspector

The transport dropdown must match the server, and the URL changes with it:

| Server mode | Inspector transport | Inspector URL |
| --- | --- | --- |
| `STREAMABLE` / `STATELESS` | Streamable HTTP | `https://<host>/mcp` |
| `SSE` | SSE | `https://<host>/sse` |

Choosing "SSE" against a `STREAMABLE` server is what produces
`Invalid Accept header. Expected TEXT_EVENT_STREAM`.

### Calling it directly

```bash
# STATELESS: one request, no handshake
curl -X POST https://<host>/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"orders_get_status","arguments":{"orderId":"ORD-1001"}}}'
```

```bash
# Claude Code
claude mcp add --transport http order-server https://<host>/mcp
```

## REST API

| Method | Path | Notes |
| --- | --- | --- |
| `GET` | `/api/v1/orders/{orderId}` | Read-only order status. `404` with an RFC 9457 problem body if the order is unknown to the caller's tenant. |
| `POST` | `/api/v1/orders/{orderId}/replacements` | Creates a replacement request. Requires `reason` and `idempotencyKey`; reusing a key replays the original request instead of creating a second one. |

```bash
curl http://localhost:8081/api/v1/orders/ORD-1001

curl -X POST http://localhost:8081/api/v1/orders/ORD-1001/replacements \
  -H 'Content-Type: application/json' \
  -d '{"reason":"DAMAGED","idempotencyKey":"1f2b3c4d-5e6f"}'
```

Seeded demo orders: `ORD-1001` (tenant-a) and `ORD-2001` (tenant-b). Requests currently
resolve to a fixed demo identity in `RequestIdentity.demoIdentity()` — replace that with
credentials propagated from the caller before this goes anywhere real, and set
`springdoc.api-docs.enabled=false` if you don't want the spec published in production.

## Build

```bash
mvn -B clean verify
```

CI runs on every push and pull request to `main` ([.github/workflows/build.yml](.github/workflows/build.yml)):
it builds and tests on JDK 21, boots the jar to export the generated `openapi.json`
as a build artifact alongside the application jar, and builds the container image.
