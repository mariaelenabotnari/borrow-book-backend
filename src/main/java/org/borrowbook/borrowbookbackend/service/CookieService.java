package org.borrowbook.borrowbookbackend.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.config.properties.CookieProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {

    @Value("${app.cookie.domain}")
    private String cookieDomain;

    private final CookieProperties  cookieProperties;

    public String extractAccessTokenFromRequest(HttpServletRequest request) {
        return getTokenFromCookies(request.getCookies(), cookieProperties.getAccessToken().getName());
    }

    public String extractRefreshTokenFromRequest(HttpServletRequest request) {
        return getTokenFromCookies(request.getCookies(), cookieProperties.getRefreshToken().getName());
    }

    public void setAuthTokensInCookies(String accessToken, String refreshToken, HttpServletResponse response) {
        ResponseCookie accessTokenCookie = createAccessTokenCookie(accessToken);
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    public ResponseCookie createAccessTokenCookie(String token) {
        return createTokenCookie(
                cookieProperties.getAccessToken().getName(), token, cookieProperties.getAccessToken().getMaxAgeSeconds());
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return createTokenCookie(
                cookieProperties.getRefreshToken().getName(), token, cookieProperties.getRefreshToken().getMaxAgeSeconds());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        System.out.println("Clearing cookies...");
        clearCookie(cookieProperties.getAccessToken().getName(), response);
        clearCookie(cookieProperties.getRefreshToken().getName(), response);
        System.out.println("Cookies cleared: " + cookieProperties.getAccessToken().getName() + ", " + cookieProperties.getRefreshToken().getName());
    }

    public void clearJwtCookie(HttpServletResponse response) {
        clearCookie(cookieProperties.getAccessToken().getName(), response);
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
                .sameSite("None")
                .domain(cookieDomain)
                .maxAge(maxAge)
                .path("/")
                .build();
    }

    private void clearCookie(String cookieName, HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .domain(cookieDomain)
                .maxAge(0)
                .path("/")
                .build();

        response.addHeader("Set-Cookie", clearCookie.toString());
    }
}