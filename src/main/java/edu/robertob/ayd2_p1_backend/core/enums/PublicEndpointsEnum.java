package edu.robertob.ayd2_p1_backend.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;
@Getter
@AllArgsConstructor
public enum PublicEndpointsEnum {

    HEALTH(HttpMethod.GET, "/api/v1/health"),
    AUTH_LOGIN(HttpMethod.POST, "/api/v1/login"),
    ONBOARDING_SET_PASSWORD(HttpMethod.POST, "/api/v1/users/onboarding/set-password"),
    FORGOT_PASSWORD(HttpMethod.POST, "/api/v1/auth/forgot-password"),
    RESET_PASSWORD(HttpMethod.POST, "/api/v1/auth/reset-password"),
    SWAGGER_UI(null, "/swagger-ui/**"),
    API_DOCS(null, "/v3/api-docs/**"),
    ;

    private final HttpMethod method;
    private final String path;

}
