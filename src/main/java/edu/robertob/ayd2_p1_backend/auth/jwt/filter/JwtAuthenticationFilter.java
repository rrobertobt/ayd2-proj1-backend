package edu.robertob.ayd2_p1_backend.auth.jwt.filter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import edu.robertob.ayd2_p1_backend.auth.jwt.utils.JwtTokenInspector;
import edu.robertob.ayd2_p1_backend.core.enums.PublicEndpointsEnum;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenInspector jwtTokenInspector;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Saltar validación si es endpoint público
        if (isPublicEndpoint(request)) {
            System.out.println("Endpoint público detectado, saltando autenticación JWT: " + request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> tokenOptional = extractTokenFromHeader(request);

        if (tokenOptional.isPresent()) {

            try {
                String token = tokenOptional.get();
                Optional<UserDetails> userDetailsOptional = validateToken(token);
                if (userDetailsOptional.isPresent()) {
                    authenticateUser(userDetailsOptional.get(), token, request);
                }

            } catch (InvalidTokenException ex) {
                log.warn(ex.getMessage());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write(new ObjectMapper().writeValueAsString(
                        new ErrorDTO("Token inválido o expirado")));
                return; // ← stop the chain; response is already committed
            }

        }
        filterChain.doFilter(request, response);
    }


    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        AntPathMatcher matcher = new AntPathMatcher();

        for (PublicEndpointsEnum endpointsEnum : PublicEndpointsEnum.values()) {
            if (!matcher.match(endpointsEnum.getPath(), requestPath)) {
                continue;
            }
            // path matches — also check method if the enum entry specifies one
            if (endpointsEnum.getMethod() == null
                    || endpointsEnum.getMethod().name().equalsIgnoreCase(requestMethod)) {
                return true;
            }
        }
        return false;
    }


    private Optional<String> extractTokenFromHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7));
    }


    private Optional<UserDetails> validateToken(String jwt) {

        String username = jwtTokenInspector.extractUsername(jwt);
        String userType = jwtTokenInspector.extractUserType(jwt);

        // Validar si el token ya ha sido autenticado
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return Optional.empty();
        }

        // creamos el usuario Spring para que sea cargado en el contexto
        User user = new User(username, "", List.of(new SimpleGrantedAuthority(userType)));

        if (jwtTokenInspector.isTokenValid(jwt)) {
            log.info("Usuario autenticado exitosamente: {}", username);
            return Optional.of(user);
        }

        log.warn("Token JWT inválido para el usuario: {}", username);
        return Optional.empty();

    }


    private void authenticateUser(UserDetails userDetails, String token, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, token, userDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
