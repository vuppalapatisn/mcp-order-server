package com.example.mcp.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiControllerTest {

    private static final String REPLACEMENT_BODY = """
            {"reason":"DAMAGED","idempotencyKey":"api-test-key"}""";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsStatusForKnownOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/ORD-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-1001"))
                .andExpect(jsonPath("$.state").value("SHIPPED"));
    }

    @Test
    void returnsProblemDetailForUnknownOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/ORD-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"));
    }

    @Test
    void createsReplacementAndReplaysSameIdempotencyKey() throws Exception {
        String firstResponse = mockMvc.perform(post("/api/v1/orders/ORD-1001/replacements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REPLACEMENT_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/v1/orders/ORD-1001/replacements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REPLACEMENT_BODY))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(secondResponse).isEqualTo(firstResponse);
    }

    @Test
    void rejectsReplacementWithoutIdempotencyKey() throws Exception {
        mockMvc.perform(post("/api/v1/orders/ORD-1001/replacements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"DAMAGED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishesOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("MCP Order Server API"))
                .andExpect(jsonPath("$.paths['/api/v1/orders/{orderId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/orders/{orderId}/replacements'].post").exists());
    }
}
