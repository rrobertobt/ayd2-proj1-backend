package edu.robertob.ayd2_p1_backend.core.config.web;

import edu.robertob.ayd2_p1_backend.auth.jwt.filter.JwtAuthenticationFilter;
import edu.robertob.ayd2_p1_backend.core.config.AppProperties;
import edu.robertob.ayd2_p1_backend.core.enums.PublicEndpointsEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final AppProperties appProperties;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthEntryPoint authEntryPoint;

    private final AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Activa CORS
                .authorizeHttpRequests(auth -> {
                    for (PublicEndpointsEnum route : PublicEndpointsEnum.values()) {
                        if (route.getMethod() == null) {
                            auth.requestMatchers(route.getPath()).permitAll();
                        } else {
                            auth.requestMatchers(route.getMethod(), route.getPath())
                                    .permitAll();
                        }
                    }
                    auth.anyRequest().authenticated(); // Resto de rutas protegidas
                })
                // sin sesiones
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 401 – sin autenticación, 403 – autenticado pero sin permisos
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.getOrBuild();
    }


    /**
     * Configuración de CORS personalizada
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // agrega todas las rutas permitidas
        configuration.setAllowedOrigins(List.of(appProperties.getAllowedOrigins()));

        // decimos que operaciones http estan permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "PUT"));

        // decimos que headers estan permitidos
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // aplicamos CORS a todas las rutas del sistema
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptVersion.$2B, 12);
    }

}
