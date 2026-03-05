package edu.robertob.ayd2_p1_backend.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;
@Getter
@AllArgsConstructor
public enum PublicEndpointsEnum {

    AUTH_LOGIN(HttpMethod.POST, "/api/v1/login"),
    SWAGGER_UI(null, "/swagger-ui/**"),
    API_DOCS(null, "/v3/api-docs/**"),
    ;

    private final HttpMethod method;
    private final String path;

}
