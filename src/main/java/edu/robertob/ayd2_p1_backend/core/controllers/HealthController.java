package edu.robertob.ayd2_p1_backend.core.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Operation(summary = "Health check", description = "Returns the health status of the application.", responses = {
            @ApiResponse(responseCode = "200", description = "Application is healthy")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

}
