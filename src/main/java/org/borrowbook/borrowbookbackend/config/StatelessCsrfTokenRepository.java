package org.borrowbook.borrowbookbackend.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.borrowbook.borrowbookbackend.config.properties.CsrfProperties;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Base64;

public class StatelessCsrfTokenRepository implements CsrfTokenRepository {
    private final CsrfProperties csrfProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public StatelessCsrfTokenRepository(CsrfProperties csrfProperties) {
        this.csrfProperties = csrfProperties;
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return new DefaultCsrfToken(csrfProperties.getHeaderName(), csrfProperties.getParameterName(), createNewToken());
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null)
            token = generateToken(request);

        Cookie cookie = new Cookie(csrfProperties.getCookie().getName(), token.getToken());
        cookie.setSecure(request.isSecure());
        cookie.setPath(getRequestContext(request));
        cookie.setMaxAge(csrfProperties.getCookie().getMaxAge());
        cookie.setHttpOnly(csrfProperties.getCookie().isHttpOnly());

        String domain = csrfProperties.getCookie().getDomain();
        if (domain != null && !domain.isEmpty())
            cookie.setDomain(domain);

        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue())
                .append("; Path=").append(cookie.getPath())
                .append("; Max-Age=").append(cookie.getMaxAge());
        if (cookie.getSecure())
            cookieHeader.append("; Secure");
        if (cookie.isHttpOnly())
            cookieHeader.append("; HttpOnly");
        if (cookie.getDomain() != null)
            cookieHeader.append("; Domain=").append(cookie.getDomain());
        cookieHeader.append("; SameSite=None");

        response.addHeader("Set-Cookie", cookieHeader.toString());
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        Cookie cookie = getCookie(request);
        if (cookie == null)
            return null;

        String token = cookie.getValue();
        if (!StringUtils.hasLength(token))
            return null;

        return new DefaultCsrfToken(csrfProperties.getHeaderName(), csrfProperties.getParameterName(), token);
    }

    public DeferredCsrfToken loadDeferredToken(HttpServletRequest request, HttpServletResponse response) {
        return new RepositoryDeferredCsrfToken(this, request, response);
    }

    private Cookie getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (csrfProperties.getCookie().getName().equals(cookie.getName()))
                    return cookie;

        return null;
    }

    private String getRequestContext(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return (!contextPath.isEmpty()) ? contextPath : "/";
    }

    private String createNewToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static final class RepositoryDeferredCsrfToken implements DeferredCsrfToken {
        private final CsrfTokenRepository tokenRepository;
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private CsrfToken csrfToken;
        private boolean missingToken;

        private RepositoryDeferredCsrfToken(CsrfTokenRepository tokenRepository,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
            this.tokenRepository = tokenRepository;
            this.request = request;
            this.response = response;
        }

        @Override
        public CsrfToken get() {
            init();
            return this.csrfToken;
        }

        @Override
        public boolean isGenerated() {
            init();
            return this.missingToken;
        }

        private void init() {
            if (this.csrfToken != null)
                return;

            this.csrfToken = this.tokenRepository.loadToken(this.request);
            this.missingToken = (this.csrfToken == null);

            if (this.missingToken) {
                this.csrfToken = this.tokenRepository.generateToken(this.request);
                this.tokenRepository.saveToken(this.csrfToken, this.request, this.response);
            }
        }
    }
}