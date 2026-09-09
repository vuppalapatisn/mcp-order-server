package com.example.mcp.server;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Landing page for the service root. The MCP endpoint is JSON-RPC only and answers a
 * browser's {@code Accept: text/html} with a bare 400, so this page spells out which
 * transport is actually active and which URL a client should point at.
 */
@Hidden
@RestController
public class ServiceIndexController {

    private final String protocol;
    private final String sseEndpoint;
    private final String sseMessageEndpoint;
    private final String streamableEndpoint;

    public ServiceIndexController(
            @Value("${spring.ai.mcp.server.protocol:STREAMABLE}") String protocol,
            @Value("${spring.ai.mcp.server.sse-endpoint:/sse}") String sseEndpoint,
            @Value("${spring.ai.mcp.server.sse-message-endpoint:/mcp/message}") String sseMessageEndpoint,
            @Value("${spring.ai.mcp.server.streamable-http.mcp-endpoint:/mcp}") String streamableEndpoint) {

        this.protocol = protocol.toUpperCase();
        this.sseEndpoint = sseEndpoint;
        this.sseMessageEndpoint = sseMessageEndpoint;
        this.streamableEndpoint = streamableEndpoint;
    }

    @GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>mcp-order-server</title>
                  <style>
                    body { font-family: system-ui, sans-serif; max-width: 46rem; margin: 3rem auto; padding: 0 1rem; line-height: 1.5; }
                    code { background: #f4f4f5; padding: .1rem .3rem; border-radius: 3px; }
                    pre { background: #f4f4f5; padding: .75rem; border-radius: 5px; overflow-x: auto; }
                    .active { color: #15803d; font-weight: 600; }
                    .off { color: #a1a1aa; }
                    table { border-collapse: collapse; width: 100%%; }
                    td, th { text-align: left; padding: .35rem .5rem; border-bottom: 1px solid #e4e4e7; }
                  </style>
                </head>
                <body>
                  <h1>mcp-order-server</h1>
                  <p>Order operations exposed as MCP tools and as a documented REST API.</p>

                  <h2>MCP transport</h2>
                  <p>Active protocol: <span class="active">%1$s</span> &mdash; only one is served at a time.
                     Switch it with the <code>MCP_PROTOCOL</code> environment variable.</p>
                  <table>
                    <tr><th>Protocol</th><th>Client URL</th><th>Status</th></tr>
                    <tr><td>STREAMABLE</td><td><code>%2$s</code> (POST)</td><td>%5$s</td></tr>
                    <tr><td>SSE</td><td><code>%3$s</code> (GET) + <code>%4$s</code> (POST)</td><td>%6$s</td></tr>
                    <tr><td>STATELESS</td><td><code>%2$s</code> (POST, no session)</td><td>%7$s</td></tr>
                  </table>
                  <p><code>%2$s</code> speaks JSON-RPC, not HTML &mdash; opening it in a browser returns
                     <em>Invalid Accept header</em> by design. POST it with
                     <code>Accept: application/json, text/event-stream</code> instead:</p>
                  <pre>curl -X POST %2$s \\
                    -H 'Content-Type: application/json' \\
                    -H 'Accept: application/json, text/event-stream' \\
                    -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{
                         "protocolVersion":"2025-06-18","capabilities":{},
                         "clientInfo":{"name":"curl","version":"1.0"}}}'</pre>

                  <h2>REST API</h2>
                  <ul>
                    <li><a href="/swagger-ui.html">Swagger UI</a></li>
                    <li><a href="/v3/api-docs">OpenAPI spec</a></li>
                    <li><a href="/actuator/health">Health</a></li>
                  </ul>
                </body>
                </html>
                """
                .formatted(
                        protocol,
                        streamableEndpoint,
                        sseEndpoint,
                        sseMessageEndpoint,
                        badge("STREAMABLE"),
                        badge("SSE"),
                        badge("STATELESS"));
    }

    private String badge(String candidate) {
        return candidate.equals(protocol)
                ? "<span class=\"active\">active</span>"
                : "<span class=\"off\">not served</span>";
    }
}
