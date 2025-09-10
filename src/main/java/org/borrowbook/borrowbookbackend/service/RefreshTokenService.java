package org.borrowbook.borrowbookbackend.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.borrowbook.borrowbookbackend.exception.RefreshTokenException;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private CookieService cookieService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenPersistenceService refreshTokenPersistenceService;

    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.extractRefreshTokenFromRequest(request);
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RefreshTokenException("Refresh token not found");
        }
        
        try {
            String username = jwtService.extractUsername(refreshToken);
            if (username == null) {
                throw new RefreshTokenException("Invalid refresh token");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RefreshTokenException("User not found"));

            if (!jwtService.isValidRefreshToken(refreshToken, user)) {
                throw new RefreshTokenException("Invalid or expired refresh token");
            }

            if (!refreshTokenPersistenceService.isPersistedRefreshToken(user.getEmail(), refreshToken)) {
                throw new RefreshTokenException("Invalid or expired refresh token");
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            ResponseCookie accessTokenCookie = cookieService.createAccessTokenCookie(newAccessToken);
            
            response.addHeader("Set-Cookie", accessTokenCookie.toString());
            
        } catch (Exception e) {
            throw new RefreshTokenException("Failed to refresh token: " + e.getMessage());
        }
    }
}