package org.borrowbook.borrowbookbackend.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.borrowbook.borrowbookbackend.config.properties.CookieProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieServiceTest {

    @Mock
    private CookieProperties cookieProperties;

    @Mock
    private CookieProperties.TokenConfig accessTokenProperties;

    @Mock
    private CookieProperties.TokenConfig refreshTokenProperties;

    @InjectMocks
    private CookieService cookieService;

    private static final String COOKIE_DOMAIN = "localhost";
    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String REFRESH_TOKEN_NAME = "refresh_token";
    private static final int ACCESS_TOKEN_MAX_AGE = 900;  // 15 minutes
    private static final int REFRESH_TOKEN_MAX_AGE = 604800;  // 7 days

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cookieService, "cookieDomain", COOKIE_DOMAIN);
        
        // Setup cookie properties mocks
        lenient().when(cookieProperties.getAccessToken()).thenReturn(accessTokenProperties);
        lenient().when(cookieProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
        lenient().when(accessTokenProperties.getName()).thenReturn(ACCESS_TOKEN_NAME);
        lenient().when(accessTokenProperties.getMaxAgeSeconds()).thenReturn(ACCESS_TOKEN_MAX_AGE);
        lenient().when(refreshTokenProperties.getName()).thenReturn(REFRESH_TOKEN_NAME);
        lenient().when(refreshTokenProperties.getMaxAgeSeconds()).thenReturn(REFRESH_TOKEN_MAX_AGE);
    }

    @Nested
    @DisplayName("extractAccessTokenFromRequest tests")
    class ExtractAccessTokenTests {

        @Test
        @DisplayName("Should extract access token when present in cookies")
        void extractAccessTokenFromRequest_WhenTokenPresent_ShouldReturnToken() {
            // Arrange
            String expectedToken = "test-access-token-12345";
            Cookie[] cookies = {
                    new Cookie(ACCESS_TOKEN_NAME, expectedToken),
                    new Cookie("other_cookie", "other_value")
            };
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getCookies()).thenReturn(cookies);

            // Act
            String result = cookieService.extractAccessTokenFromRequest(request);

            // Assert
            assertEquals(expectedToken, result);
        }

        @Test
        @DisplayName("Should return null when access token not present")
        void extractAccessTokenFromRequest_WhenTokenNotPresent_ShouldReturnNull() {
            // Arrange
            Cookie[] cookies = {
                    new Cookie("other_cookie", "other_value")
            };
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getCookies()).thenReturn(cookies);

            // Act
            String result = cookieService.extractAccessTokenFromRequest(request);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should return null when cookies array is null")
        void extractAccessTokenFromRequest_WhenNoCookies_ShouldReturnNull() {
            // Arrange
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getCookies()).thenReturn(null);

            // Act
            String result = cookieService.extractAccessTokenFromRequest(request);

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("extractRefreshTokenFromRequest tests")
    class ExtractRefreshTokenTests {

        @Test
        @DisplayName("Should extract refresh token when present in cookies")
        void extractRefreshTokenFromRequest_WhenTokenPresent_ShouldReturnToken() {
            // Arrange
            String expectedToken = "test-refresh-token-67890";
            Cookie[] cookies = {
                    new Cookie(REFRESH_TOKEN_NAME, expectedToken),
                    new Cookie("other_cookie", "other_value")
            };
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getCookies()).thenReturn(cookies);

            // Act
            String result = cookieService.extractRefreshTokenFromRequest(request);

            // Assert
            assertEquals(expectedToken, result);
        }

        @Test
        @DisplayName("Should return null when refresh token not present")
        void extractRefreshTokenFromRequest_WhenTokenNotPresent_ShouldReturnNull() {
            // Arrange
            Cookie[] cookies = {
                    new Cookie("other_cookie", "other_value")
            };
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getCookies()).thenReturn(cookies);

            // Act
            String result = cookieService.extractRefreshTokenFromRequest(request);

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("setAuthTokensInCookies tests")
    class SetAuthTokensTests {

        @Test
        @DisplayName("Should set both access and refresh token cookies")
        void setAuthTokensInCookies_ShouldSetBothCookies() {
            // Arrange
            String accessToken = "access-token-value";
            String refreshToken = "refresh-token-value";
            HttpServletResponse response = mock(HttpServletResponse.class);
            List<String> headers = new ArrayList<>();
            doAnswer(invocation -> {
                headers.add(invocation.getArgument(1));
                return null;
            }).when(response).addHeader(eq("Set-Cookie"), anyString());

            // Act
            cookieService.setAuthTokensInCookies(accessToken, refreshToken, response);

            // Assert
            verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
            assertEquals(2, headers.size());
            assertTrue(headers.stream().anyMatch(h -> h.contains(ACCESS_TOKEN_NAME)));
            assertTrue(headers.stream().anyMatch(h -> h.contains(REFRESH_TOKEN_NAME)));
        }
    }

    @Nested
    @DisplayName("createAccessTokenCookie tests")
    class CreateAccessTokenCookieTests {

        @Test
        @DisplayName("Should create access token cookie with correct attributes")
        void createAccessTokenCookie_ShouldHaveCorrectAttributes() {
            // Arrange
            String token = "test-access-token";

            // Act
            ResponseCookie cookie = cookieService.createAccessTokenCookie(token);

            // Assert
            assertEquals(ACCESS_TOKEN_NAME, cookie.getName());
            assertEquals(token, cookie.getValue());
            assertTrue(cookie.isHttpOnly());
            assertTrue(cookie.isSecure());
            assertEquals("Strict", cookie.getSameSite());
            assertEquals(COOKIE_DOMAIN, cookie.getDomain());
            assertEquals(ACCESS_TOKEN_MAX_AGE, cookie.getMaxAge().getSeconds());
            assertEquals("/", cookie.getPath());
        }

        @Test
        @DisplayName("Should handle empty token value")
        void createAccessTokenCookie_WhenEmptyToken_ShouldCreateCookie() {
            // Arrange
            String token = "";

            // Act
            ResponseCookie cookie = cookieService.createAccessTokenCookie(token);

            // Assert
            assertEquals(ACCESS_TOKEN_NAME, cookie.getName());
            assertEquals("", cookie.getValue());
        }
    }

    @Nested
    @DisplayName("createRefreshTokenCookie tests")
    class CreateRefreshTokenCookieTests {

        @Test
        @DisplayName("Should create refresh token cookie with correct attributes")
        void createRefreshTokenCookie_ShouldHaveCorrectAttributes() {
            // Arrange
            String token = "test-refresh-token";

            // Act
            ResponseCookie cookie = cookieService.createRefreshTokenCookie(token);

            // Assert
            assertEquals(REFRESH_TOKEN_NAME, cookie.getName());
            assertEquals(token, cookie.getValue());
            assertTrue(cookie.isHttpOnly());
            assertTrue(cookie.isSecure());
            assertEquals("Strict", cookie.getSameSite());
            assertEquals(COOKIE_DOMAIN, cookie.getDomain());
            assertEquals(REFRESH_TOKEN_MAX_AGE, cookie.getMaxAge().getSeconds());
            assertEquals("/", cookie.getPath());
        }
    }

    @Nested
    @DisplayName("clearAuthCookies tests")
    class ClearAuthCookiesTests {

        @Test
        @DisplayName("Should clear both access and refresh token cookies")
        void clearAuthCookies_ShouldClearBothCookies() {
            // Arrange
            HttpServletResponse response = mock(HttpServletResponse.class);
            ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

            // Act
            cookieService.clearAuthCookies(response);

            // Assert
            verify(response, times(2)).addHeader(eq("Set-Cookie"), headerCaptor.capture());
            
            List<String> capturedHeaders = headerCaptor.getAllValues();
            assertEquals(2, capturedHeaders.size());
            
            // Verify cookies are cleared (max-age=0)
            assertTrue(capturedHeaders.stream().allMatch(h -> h.contains("Max-Age=0")));
            assertTrue(capturedHeaders.stream().anyMatch(h -> h.contains(ACCESS_TOKEN_NAME)));
            assertTrue(capturedHeaders.stream().anyMatch(h -> h.contains(REFRESH_TOKEN_NAME)));
        }
    }

    @Nested
    @DisplayName("clearJwtCookie tests")
    class ClearJwtCookieTests {

        @Test
        @DisplayName("Should clear only access token cookie")
        void clearJwtCookie_ShouldClearOnlyAccessToken() {
            // Arrange
            HttpServletResponse response = mock(HttpServletResponse.class);
            ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

            // Act
            cookieService.clearJwtCookie(response);

            // Assert
            verify(response, times(1)).addHeader(eq("Set-Cookie"), headerCaptor.capture());
            
            String capturedHeader = headerCaptor.getValue();
            assertTrue(capturedHeader.contains(ACCESS_TOKEN_NAME));
            assertTrue(capturedHeader.contains("Max-Age=0"));
        }
    }

    @Nested
    @DisplayName("Cookie security attributes tests")
    class CookieSecurityTests {

        @Test
        @DisplayName("All cookies should be HttpOnly to prevent XSS")
        void allCookies_ShouldBeHttpOnly() {
            // Arrange
            String token = "test-token";

            // Act
            ResponseCookie accessCookie = cookieService.createAccessTokenCookie(token);
            ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(token);

            // Assert
            assertTrue(accessCookie.isHttpOnly(), "Access token cookie should be HttpOnly");
            assertTrue(refreshCookie.isHttpOnly(), "Refresh token cookie should be HttpOnly");
        }

        @Test
        @DisplayName("All cookies should be Secure for HTTPS")
        void allCookies_ShouldBeSecure() {
            // Arrange
            String token = "test-token";

            // Act
            ResponseCookie accessCookie = cookieService.createAccessTokenCookie(token);
            ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(token);

            // Assert
            assertTrue(accessCookie.isSecure(), "Access token cookie should be Secure");
            assertTrue(refreshCookie.isSecure(), "Refresh token cookie should be Secure");
        }

        @Test
        @DisplayName("All cookies should have SameSite=Strict for CSRF protection")
        void allCookies_ShouldHaveSameSiteStrict() {
            // Arrange
            String token = "test-token";

            // Act
            ResponseCookie accessCookie = cookieService.createAccessTokenCookie(token);
            ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(token);

            // Assert
            assertEquals("Strict", accessCookie.getSameSite(), "Access token should have SameSite=Strict");
            assertEquals("Strict", refreshCookie.getSameSite(), "Refresh token should have SameSite=Strict");
        }
    }
}
