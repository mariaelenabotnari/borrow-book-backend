package org.borrowbook.borrowbookbackend.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int ACCESS_TOKEN_MAX_AGE = 15 * 60; // 15 minutes
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    public String getJwtFromCookies(Cookie[] cookies) {
        return getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_NAME);
    }

    public void clearJwtCookie(HttpServletResponse response) {
        ResponseCookie clearCookie = createClearCookie(ACCESS_TOKEN_COOKIE_NAME);
        response.addHeader("Set-Cookie", clearCookie.toString());
    }

    public ResponseCookie createAccessTokenCookie(String token) {
        return createTokenCookie(ACCESS_TOKEN_COOKIE_NAME, token, ACCESS_TOKEN_MAX_AGE);
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return createTokenCookie(REFRESH_TOKEN_COOKIE_NAME, token, REFRESH_TOKEN_MAX_AGE);
    }

    public String extractRefreshTokenFromRequest(HttpServletRequest request) {
        return getTokenFromCookies(request.getCookies(), REFRESH_TOKEN_COOKIE_NAME);
    }

    private String getTokenFromCookies(Cookie[] cookies, String cookieName) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private ResponseCookie createTokenCookie(String name, String token, int maxAge) {
        return ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(maxAge)
                .path("/")
                .build();
    }

    private ResponseCookie createClearCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)
                .path("/")
                .build();
    }
}