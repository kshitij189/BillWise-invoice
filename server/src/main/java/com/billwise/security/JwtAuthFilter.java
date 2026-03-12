package com.billwise.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/users") ||
               path.startsWith("/public/") ||
               path.equals("/send-pdf") ||
               path.equals("/create-pdf") ||
               path.equals("/fetch-pdf");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\":\"No token provided\"}");
            response.setContentType("application/json");
            return;
        }

        String token = authHeader.substring(7);
        String userId = null;

        if (jwtUtil.isGoogleToken(token)) {
            // Google token - decode without verification
            Map<String, Object> payload = jwtUtil.decodeGoogleToken(token);
            if (payload != null) {
                userId = (String) payload.get("sub");
            }
        } else {
            // Custom JWT token - verify with secret
            Claims claims = jwtUtil.validateToken(token);
            if (claims != null) {
                userId = claims.get("id", String.class);
            }
        }

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\":\"Invalid or expired token\"}");
            response.setContentType("application/json");
            return;
        }

        // Set userId as request attribute for controllers to use
        request.setAttribute("userId", userId);

        // Set authentication in security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
