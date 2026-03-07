package edu.robertob.ayd2_p1_backend.core.enums;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.*;

class PublicEndpointsEnumTest {

    @Test
    void allValues_haveNonNullPath() {
        for (PublicEndpointsEnum entry : PublicEndpointsEnum.values()) {
            assertNotNull(entry.getPath(),
                    "Path should not be null for: " + entry.name());
            assertFalse(entry.getPath().isBlank(),
                    "Path should not be blank for: " + entry.name());
        }
    }

    @Test
    void health_hasGetMethodAndCorrectPath() {
        assertEquals(HttpMethod.GET, PublicEndpointsEnum.HEALTH.getMethod());
        assertEquals("/api/v1/health", PublicEndpointsEnum.HEALTH.getPath());
    }

    @Test
    void authLogin_hasPostMethodAndCorrectPath() {
        assertEquals(HttpMethod.POST, PublicEndpointsEnum.AUTH_LOGIN.getMethod());
        assertEquals("/api/v1/login", PublicEndpointsEnum.AUTH_LOGIN.getPath());
    }

    @Test
    void onboardingSetPassword_hasPostMethodAndCorrectPath() {
        assertEquals(HttpMethod.POST, PublicEndpointsEnum.ONBOARDING_SET_PASSWORD.getMethod());
        assertEquals("/api/v1/users/onboarding/set-password",
                PublicEndpointsEnum.ONBOARDING_SET_PASSWORD.getPath());
    }

    @Test
    void swaggerUi_hasNullMethodAllowingAnyHttpMethod() {
        assertNull(PublicEndpointsEnum.SWAGGER_UI.getMethod());
        assertEquals("/swagger-ui/**", PublicEndpointsEnum.SWAGGER_UI.getPath());
    }

    @Test
    void apiDocs_hasNullMethodAllowingAnyHttpMethod() {
        assertNull(PublicEndpointsEnum.API_DOCS.getMethod());
        assertEquals("/v3/api-docs/**", PublicEndpointsEnum.API_DOCS.getPath());
    }
}
