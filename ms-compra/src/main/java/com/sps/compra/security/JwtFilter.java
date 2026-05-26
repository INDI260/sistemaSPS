package com.sps.compra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(JwtFilter.class.getName());

    private static final Pattern PAGO_PATTERN = Pattern.compile("^/ws/compra/\\d+/pago$");

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip JWT for POST /ws/compra/{numero}/pago
        if ("POST".equals(method) && PAGO_PATTERN.matcher(path).matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Only enforce JWT for /ws/compra/** paths
        if (!path.startsWith("/ws/compra")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "Token JWT requerido");
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String cedula = claims.get("cedula", String.class);
            String nombre = claims.get("nombre", String.class);
            String correo = claims.get("correo", String.class);

            request.setAttribute("cedula_cliente", cedula);
            request.setAttribute("nombre_cliente", nombre);
            request.setAttribute("correo_cliente", correo);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.warning("JwtFilter: token invalido - " + e.getMessage());
            sendUnauthorized(response, "Token JWT invalido o expirado");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":\"ERROR\",\"data\":null,\"message\":\"" + message + "\",\"timestamp\":\"" +
                        java.time.Instant.now().toString() + "\"}"
        );
    }
}
