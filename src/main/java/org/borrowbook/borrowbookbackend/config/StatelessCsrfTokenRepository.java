package org.borrowbook.borrowbookbackend.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Custom CSRF Token Repository that preserves tokens across requests in stateless applications.
 * Unlike the default CookieCsrfTokenRepository, this doesn't regenerate tokens unnecessarily.
 */
@Slf4j
public class StatelessCsrfTokenRepository implements CsrfTokenRepository {

    static final String DEFAULT_CSRF_COOKIE_NAME = "XSRF-TOKEN";
    static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";
    static final String DEFAULT_CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;
    private String headerName = DEFAULT_CSRF_HEADER_NAME;
    private String cookieName = DEFAULT_CSRF_COOKIE_NAME;
    private boolean cookieHttpOnly = false;
    private String cookiePath;
    private String cookieDomain;
    private Boolean secure;
    private int cookieMaxAge = -1;
    private SecureRandom secureRandom = new SecureRandom();

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return new DefaultCsrfToken(this.headerName, this.parameterName, createNewToken());
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            String path = request.getRequestURI();

            // Case 1: Logout → really clear cookie
            if (path != null && path.contains("/logout")) {
                Cookie cookie = new Cookie(this.cookieName, "");
                cookie.setPath(StringUtils.hasLength(this.cookiePath) ? this.cookiePath : this.getRequestContext(request));
                cookie.setMaxAge(0); // expire now
                cookie.setHttpOnly(this.cookieHttpOnly);
                if (StringUtils.hasLength(this.cookieDomain)) {
                    cookie.setDomain(this.cookieDomain);
                }
                response.addCookie(cookie);
                return;
            }

            // Case 2: "Accidental clear" (e.g. login/session fixation) → ROTATE instead of clear
            CsrfToken newToken = generateToken(request);
            token = newToken; // replace with new one
        }

        // Save / update cookie with the (new) token
        String tokenValue = token.getToken();
        Cookie cookie = new Cookie(this.cookieName, tokenValue);
        cookie.setSecure((this.secure != null) ? this.secure : request.isSecure());
        cookie.setPath(StringUtils.hasLength(this.cookiePath) ? this.cookiePath : this.getRequestContext(request));
        cookie.setMaxAge(this.cookieMaxAge);
        cookie.setHttpOnly(this.cookieHttpOnly);

        if (StringUtils.hasLength(this.cookieDomain)) {
            cookie.setDomain(this.cookieDomain);
        }

        response.addCookie(cookie);
    }


    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        Cookie cookie = getCookie(request);
        if (cookie == null) {
            return null;
        }

        String token = cookie.getValue();
        if (!StringUtils.hasLength(token)) {
            return null;
        }

        return new DefaultCsrfToken(this.headerName, this.parameterName, token);
    }

    /**
     * This method is key - it loads the existing token if present, otherwise generates a new one.
     * This prevents unnecessary token regeneration.
     */
    public DeferredCsrfToken loadDeferredToken(HttpServletRequest request, HttpServletResponse response) {
        return new RepositoryDeferredCsrfToken(this, request, response);
    }

    private Cookie getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (this.cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private String getRequestContext(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return (contextPath.length() > 0) ? contextPath : "/";
    }

    private String createNewToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Custom deferred token that preserves existing tokens
     */
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
            if (this.csrfToken != null) {
                return;
            }

            // First, try to load existing token
            this.csrfToken = this.tokenRepository.loadToken(this.request);
            this.missingToken = (this.csrfToken == null);

            // Only generate and save new token if one doesn't exist
            if (this.missingToken) {
                this.csrfToken = this.tokenRepository.generateToken(this.request);
                this.tokenRepository.saveToken(this.csrfToken, this.request, this.response);
            }
        }
    }

    // Setter methods for configuration
    public void setParameterName(String parameterName) {
        Assert.notNull(parameterName, "parameterName cannot be null");
        this.parameterName = parameterName;
    }

    public void setHeaderName(String headerName) {
        Assert.notNull(headerName, "headerName cannot be null");
        this.headerName = headerName;
    }

    public void setCookieName(String cookieName) {
        Assert.notNull(cookieName, "cookieName cannot be null");
        this.cookieName = cookieName;
    }

    public void setCookieHttpOnly(boolean cookieHttpOnly) {
        this.cookieHttpOnly = cookieHttpOnly;
    }

    public void setCookiePath(String path) {
        this.cookiePath = path;
    }

    public void setCookieDomain(String domain) {
        this.cookieDomain = domain;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public void setCookieMaxAge(int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    /**
     * Factory method to create with HttpOnly set to false (for JavaScript access)
     */
    public static StatelessCsrfTokenRepository withHttpOnlyFalse() {
        StatelessCsrfTokenRepository result = new StatelessCsrfTokenRepository();
        result.setCookieHttpOnly(false);
        return result;
    }
}