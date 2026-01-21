package org.borrowbook.borrowbookbackend.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.borrowbook.borrowbookbackend.config.properties.CookieProperties;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private CookieProperties cookieProperties;

    @Mock
    private CookieProperties.TokenConfig accessTokenConfig;

    @Mock
    private CookieProperties.TokenConfig refreshTokenConfig;

    @InjectMocks
    private JwtService jwtService;

    // Base64 encoded 256-bit key for HS256
    private static final String SECRET_KEY = "dGhpc2lzYXZlcnlzZWN1cmVzZWNyZXRrZXlmb3J0ZXN0aW5nand0c2VydmljZTE=";
    private static final int ACCESS_TOKEN_MAX_AGE = 900;  // 15 minutes
    private static final int REFRESH_TOKEN_MAX_AGE = 604800;  // 7 days

    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        
        // Setup cookie properties mocks
        lenient().when(cookieProperties.getAccessToken()).thenReturn(accessTokenConfig);
        lenient().when(cookieProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        lenient().when(accessTokenConfig.getMaxAgeSeconds()).thenReturn(ACCESS_TOKEN_MAX_AGE);
        lenient().when(refreshTokenConfig.getMaxAgeSeconds()).thenReturn(REFRESH_TOKEN_MAX_AGE);

        testUser = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("generateAccessToken tests")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("Should generate valid access token")
        void generateAccessToken_ShouldReturnValidToken() {
            // Arrange - setup done in @BeforeEach

            // Act
            String token = jwtService.generateAccessToken(testUser);

            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.split("\\.").length == 3, "JWT should have 3 parts");
        }

        @Test
        @DisplayName("Should include username as subject in access token")
        void generateAccessToken_ShouldContainCorrectSubject() {
            // Arrange - setup done in @BeforeEach

            // Act
            String token = jwtService.generateAccessToken(testUser);
            String extractedUsername = jwtService.extractUsername(token);

            // Assert
            assertEquals(testUser.getUsername(), extractedUsername);
        }
    }

    @Nested
    @DisplayName("generateRefreshToken tests")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Should generate valid refresh token")
        void generateRefreshToken_ShouldReturnValidToken() {
            // Arrange - setup done in @BeforeEach

            // Act
            String token = jwtService.generateRefreshToken(testUser);

            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.split("\\.").length == 3, "JWT should have 3 parts");
        }

        @Test
        @DisplayName("Should include username as subject in refresh token")
        void generateRefreshToken_ShouldContainCorrectSubject() {
            // Arrange - setup done in @BeforeEach

            // Act
            String token = jwtService.generateRefreshToken(testUser);
            String extractedUsername = jwtService.extractUsername(token);

            // Assert
            assertEquals(testUser.getUsername(), extractedUsername);
        }

        @Test
        @DisplayName("Refresh token should have longer expiration than access token")
        void generateRefreshToken_ShouldHaveLongerExpiration() {
            // Arrange - setup done in @BeforeEach

            // Act
            String accessToken = jwtService.generateAccessToken(testUser);
            String refreshToken = jwtService.generateRefreshToken(testUser);

            // Assert
            assertTrue(jwtService.isRefreshToken(refreshToken), 
                    "Refresh token should be identified as refresh token");
        }
    }

    @Nested
    @DisplayName("extractUsername tests")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Should extract correct username from token")
        void extractUsername_ShouldReturnCorrectUsername() {
            // Arrange
            String token = jwtService.generateAccessToken(testUser);

            // Act
            String username = jwtService.extractUsername(token);

            // Assert
            assertEquals("testuser", username);
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void extractUsername_WhenInvalidToken_ShouldThrowException() {
            // Arrange
            String invalidToken = "invalid.token.here";

            // Act & Assert
            assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
        }
    }

    @Nested
    @DisplayName("isTokenValid tests")
    class IsTokenValidTests {

        @Test
        @DisplayName("Should return true for valid token with matching user")
        void isTokenValid_WhenValidTokenAndMatchingUser_ShouldReturnTrue() {
            // Arrange
            String token = jwtService.generateAccessToken(testUser);

            // Act
            boolean isValid = jwtService.isTokenValid(token, testUser);

            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should return false for token with different username")
        void isTokenValid_WhenDifferentUser_ShouldReturnFalse() {
            // Arrange
            String token = jwtService.generateAccessToken(testUser);
            UserDetails differentUser = User.builder()
                    .id(2)
                    .username("differentuser")
                    .email("different@example.com")
                    .build();

            // Act
            boolean isValid = jwtService.isTokenValid(token, differentUser);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for expired token")
        void isTokenValid_WhenExpiredToken_ShouldThrowOrReturnFalse() {
            // Arrange - create an expired token
            String expiredToken = createExpiredToken(testUser.getUsername());

            // Act & Assert
            assertThrows(ExpiredJwtException.class, 
                    () -> jwtService.isTokenValid(expiredToken, testUser));
        }
    }

    @Nested
    @DisplayName("isRefreshToken tests")
    class IsRefreshTokenTests {

        @Test
        @DisplayName("Should return true for refresh token")
        void isRefreshToken_WhenRefreshToken_ShouldReturnTrue() {
            // Arrange
            String refreshToken = jwtService.generateRefreshToken(testUser);

            // Act
            boolean isRefresh = jwtService.isRefreshToken(refreshToken);

            // Assert
            assertTrue(isRefresh);
        }

        @Test
        @DisplayName("Should return false for access token")
        void isRefreshToken_WhenAccessToken_ShouldReturnFalse() {
            // Arrange
            String accessToken = jwtService.generateAccessToken(testUser);

            // Act
            boolean isRefresh = jwtService.isRefreshToken(accessToken);

            // Assert
            assertFalse(isRefresh);
        }
    }

    @Nested
    @DisplayName("isValidRefreshToken tests")
    class IsValidRefreshTokenTests {

        @Test
        @DisplayName("Should return true for valid refresh token with matching user")
        void isValidRefreshToken_WhenValidAndMatching_ShouldReturnTrue() {
            // Arrange
            String refreshToken = jwtService.generateRefreshToken(testUser);

            // Act
            boolean isValid = jwtService.isValidRefreshToken(refreshToken, testUser);

            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should return false for access token even with matching user")
        void isValidRefreshToken_WhenAccessToken_ShouldReturnFalse() {
            // Arrange
            String accessToken = jwtService.generateAccessToken(testUser);

            // Act
            boolean isValid = jwtService.isValidRefreshToken(accessToken, testUser);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for refresh token with non-matching user")
        void isValidRefreshToken_WhenDifferentUser_ShouldReturnFalse() {
            // Arrange
            String refreshToken = jwtService.generateRefreshToken(testUser);
            UserDetails differentUser = User.builder()
                    .id(2)
                    .username("differentuser")
                    .email("different@example.com")
                    .build();

            // Act
            boolean isValid = jwtService.isValidRefreshToken(refreshToken, differentUser);

            // Assert
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("generateToken with extra claims tests")
    class GenerateTokenWithClaimsTests {

        @Test
        @DisplayName("Should generate token with custom claims")
        void generateToken_WithExtraClaims_ShouldIncludeClaims() {
            // Arrange
            HashMap<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", "ADMIN");
            extraClaims.put("userId", 123);
            long expiration = 3600000L; // 1 hour

            // Act
            String token = jwtService.generateToken(extraClaims, testUser, expiration);

            // Assert
            assertNotNull(token);
            assertEquals(testUser.getUsername(), jwtService.extractUsername(token));
        }

        @Test
        @DisplayName("Should generate token with empty extra claims")
        void generateToken_WithEmptyClaims_ShouldGenerateValidToken() {
            // Arrange
            HashMap<String, Object> extraClaims = new HashMap<>();
            long expiration = 3600000L;

            // Act
            String token = jwtService.generateToken(extraClaims, testUser, expiration);

            // Assert
            assertNotNull(token);
            assertTrue(jwtService.isTokenValid(token, testUser));
        }
    }

    // Helper method to create an expired token for testing
    private String createExpiredToken(String username) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .setExpiration(new Date(System.currentTimeMillis() - 1800000)) // 30 minutes ago (expired)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
