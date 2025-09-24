package org.borrowbook.borrowbookbackend.filter;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrf = (CsrfToken) request.getAttribute("_csrf");
        if (csrf != null) {
            String cookieValue = csrf.getToken();
            String cookieName = "XSRF-TOKEN";
            String domain = ".borrowbook.me";
            String cookieHeader = cookieName + "=" + cookieValue +
                    "; Path=/" +
                    "; Max-Age=3600" +
                    "; Secure" +
                    "; SameSite=None" +
                    "; Domain=" + domain;
            response.addHeader("Set-Cookie", cookieHeader);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/favicon.ico") ||
                path.startsWith("/.well-known");
    }
}