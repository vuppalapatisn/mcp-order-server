package com.example.mcp.server;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServerOpenApi(
            @Value("${spring.ai.mcp.server.version:1.0.0}") String version) {

        return new OpenAPI().info(new Info()
                .title("MCP Order Server API")
                .version(version)
                .description("""
                        HTTP view of the order operations this service also exposes as MCP tools \
                        (orders_get_status and orders_create_replacement). Order data is in-memory \
                        demo data scoped to a single demo tenant.""")
                .license(new License().name("Apache-2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
