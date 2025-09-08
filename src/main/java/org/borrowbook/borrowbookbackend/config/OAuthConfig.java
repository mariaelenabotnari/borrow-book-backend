package org.borrowbook.borrowbookbackend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.exception.EmailInUseException;
import org.borrowbook.borrowbookbackend.exception.NotFoundException;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.borrowbook.borrowbookbackend.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthConfig extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${application.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String googleId = oauth2User.getAttribute("sub");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(extractUsername(email));
            newUser.setGoogleId(googleId);
            newUser.setActivated(true);
            newUser.setRole(Role.USER);
            return userRepository.save(newUser);
        });

        if (user.getGoogleId() == null || !user.isActivated()) {
            throw new NotFoundException("No account registered with Google for this email.");
        }

        String token = jwtService.generateToken(user);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth-callback")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    public String extractUsername(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        return email.substring(0, email.indexOf('@'));
    }
}