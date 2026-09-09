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
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | http://localhost:8081/v3/api-docs |
| Health | http://localhost:8081/actuator/health |

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
