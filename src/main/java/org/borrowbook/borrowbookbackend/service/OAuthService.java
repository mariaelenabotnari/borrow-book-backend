package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.exception.EmailInUseException;
import org.borrowbook.borrowbookbackend.exception.NotFoundException;
import org.borrowbook.borrowbookbackend.exception.RateLimitException;
import org.borrowbook.borrowbookbackend.exception.UsernameInUseException;
import org.borrowbook.borrowbookbackend.model.dto.AuthenticationRequest;
import org.borrowbook.borrowbookbackend.model.dto.SessionResponse;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public SessionResponse activateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isActivated()) {
            throw new EmailInUseException("Email is already in use");
        }
        user.setActivated(true);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new SessionResponse(token);
    }

    public SessionResponse login(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with that email"));

        if (user.getGoogleId() == null || !user.isActivated()) {
            throw new NotFoundException("User not registered with Google");
        }

        String token = jwtService.generateToken(user);
        return new SessionResponse(token);
    }
}


