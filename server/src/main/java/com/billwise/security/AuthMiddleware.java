package com.billwise.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthMiddleware {

    /**
     * Extracts the userId from the request attribute set by JwtAuthFilter.
     */
    public String getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? userId.toString() : null;
    }

    /**
     * Checks if the request has a valid authenticated user.
     */
    public boolean isAuthenticated(HttpServletRequest request) {
        return getUserId(request) != null;
    }
}
