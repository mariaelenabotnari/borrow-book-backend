package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.exception.EmailInUseException;
import org.borrowbook.borrowbookbackend.exception.NotFoundException;
import org.borrowbook.borrowbookbackend.exception.RateLimitException;
import org.borrowbook.borrowbookbackend.exception.UsernameInUseException;
import org.borrowbook.borrowbookbackend.model.dto.AuthenticationRequest;
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
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CodeVerificationService codeVerificationService;
    private final RateLimiterService rateLimiterService;
    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;

    public void registerAndSendCode(org.borrowbook.borrowbookbackend.model.dto.AuthenticationRequest.OAuthRegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new UsernameInUseException("Username is already in use");
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    if (user.isActivated())
                        throw new EmailInUseException("Email is already in use");
                });

        var existingUser = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        User user;
        if (existingUser != null && !existingUser.isActivated()) {
            existingUser.setUsername(request.getUsername());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            user = existingUser;
        }
        else {
            user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.USER)
                    .activated(false)
                    .build();
        }

        userRepository.save(user);

        long retryAfter = rateLimiterService.checkRateLimit(
                "register", request.getEmail(), 5, 15 * 60);
        if (retryAfter > 0)
            throw new RateLimitException("Too many register attempts. Try again in " + retryAfter + " seconds.");

//        String code = authenticationService.generateCode();
//        codeVerificationService.storeCode(user.getEmail(), code);
//
//        emailService.sendVerificationCode(user.getEmail(), code);
    }

    public void loginAndSendCode(AuthenticationRequest request) {
        long retryAfter = rateLimiterService.checkRateLimit(
                "login", request.getUsername(), 5, 15 * 60);
        if (retryAfter > 0)
            throw new RateLimitException("Too many login attempts. Try again in " + retryAfter + " seconds.");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

//        String code = authenticationService.generateCode();
//        codeVerificationService.storeCode(user.getEmail(), code);
//
//        emailService.sendVerificationCode(user.getEmail(), code);
    }
}
