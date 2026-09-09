package com.example.mcp.server;

public record RequestIdentity(String tenantId, String userId) {
    public static RequestIdentity demoIdentity() {
        // Replace with verified credentials propagated from the MCP client.
        return new RequestIdentity("tenant-a", "mcp-demo-user");
    }
}
