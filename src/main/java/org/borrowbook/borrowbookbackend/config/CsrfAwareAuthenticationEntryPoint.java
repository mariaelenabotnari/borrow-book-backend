package org.borrowbook.borrowbookbackend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CsrfAwareAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        Throwable cause = authException.getCause();
        if (cause instanceof InvalidCsrfTokenException || cause instanceof MissingCsrfTokenException) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid or missing");
            return;
        }
        
        if (request.getHeader("X-XSRF-TOKEN") == null &&
            !request.getRequestURI().startsWith("/api/v1/auth/")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token required");
            return;
        }
        
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
    }
}