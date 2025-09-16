package org.borrowbook.borrowbookbackend.config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        if (accessDeniedException instanceof org.springframework.security.web.csrf.CsrfException) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
        }
    }

}
