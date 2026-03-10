package edu.robertob.ayd2_p1_backend.core.config.web;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    private final SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    void customOpenAPI_returnsOpenAPIWithTitle() {
        OpenAPI api = swaggerConfig.customOpenAPI();

        assertNotNull(api);
        assertNotNull(api.getInfo());
        assertEquals("REST API - Project 1-B AyD2", api.getInfo().getTitle());
    }

    @Test
    void publicApi_returnsGroupedOpenApi() {
        GroupedOpenApi api = swaggerConfig.publicApi();

        assertNotNull(api);
        assertEquals("public", api.getGroup());
    }
}
