package edu.robertob.ayd2_p1_backend.core.config.web;

import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info().title("REST API - Project 1-B AyD2")
                        .version("1.0")
                        .description("Documentación de la API para el Proyecto 1 del curso de Análisis y Diseño de Sistemas 2"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public") // grupo de endpoints públicos
                .pathsToMatch("/api/**") // se define el patrón de las rutas a documentar
                .build();
    }
}
