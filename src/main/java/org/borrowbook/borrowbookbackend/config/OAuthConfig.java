package org.borrowbook.borrowbookbackend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.exception.NotFoundException;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.borrowbook.borrowbookbackend.service.CookieService;
import org.borrowbook.borrowbookbackend.service.JwtService;
import org.borrowbook.borrowbookbackend.service.RefreshTokenPersistenceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuthConfig extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenPersistenceService refreshTokenPersistenceService;

    @Value("${application.frontend.url}")

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String googleId = oauth2User.getAttribute("sub");

        User user = userRepository.findByEmailAndActivatedTrue(email).orElseGet(() -> {
            User newUser = new User(extractUsername(email),email, googleId, true);
            return userRepository.save(newUser);
        });

        if (user.getGoogleId() == null || !user.isActivated()) {
            throw new NotFoundException("No account registered with Google for this email.");
        }

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenPersistenceService.storeRefreshToken(
                user.getEmail(),
                refreshToken,
                Duration.ofDays(7)
        );

        cookieService.setAuthTokensInCookies(accessToken, refreshToken, response);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        String targetUrl = frontendUrl + "/profile";
        setDefaultTargetUrl(targetUrl);
        try {
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            throw new RuntimeException("Redirect failed", e);
        }
    }

    public String extractUsername(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        return email.substring(0, email.indexOf('@'));
    }
}
