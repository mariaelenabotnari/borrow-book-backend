package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.dto.AuthenticationRequest;
import org.borrowbook.borrowbookbackend.dto.AuthenticationResponse;
import org.borrowbook.borrowbookbackend.dto.RegisterRequest;
import org.borrowbook.borrowbookbackend.entities.User;
import org.borrowbook.borrowbookbackend.exception.*;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    private final CodeVerificationService codeVerificationService;
    private final RateLimiterService rateLimiterService;

    public void registerAndSendCode(RegisterRequest request) {
        if (repository.findByUsername(request.getUsername()).isPresent())
            throw new UsernameInUseException("Username is already in use");
        repository.findByEmail(request.getEmail())
            .ifPresent(user -> {
                if (user.isActivated())
                    throw new EmailInUseException("Email is already in use");
            });

        var existingUser = repository.findByEmail(request.getEmail())
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

        repository.save(user);

        long retryAfter = rateLimiterService.checkRateLimit(
                "register", request.getEmail(), 5, 15 * 60);
        if (retryAfter > 0)
            throw new RateLimitException("Too many register attempts. Try again in " + retryAfter + " seconds.");

        String code = generateCode();
        codeVerificationService.storeCode(user.getEmail(), code);

        emailService.sendVerificationCode(user.getEmail(), code);
    }

    public void loginAndSendCode(AuthenticationRequest request) {
        long retryAfter = rateLimiterService.checkRateLimit(
                "login", request.getEmail(), 5, 15 * 60);
        if (retryAfter > 0)
            throw new RateLimitException("Too many login attempts. Try again in " + retryAfter + " seconds.");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String code = generateCode();
        codeVerificationService.storeCode(user.getEmail(), code);

        emailService.sendVerificationCode(user.getEmail(), code);
    }

    public AuthenticationResponse verifyCode(String email, String code) {
        String storedCode = codeVerificationService.getCode(email);

        if (storedCode != null && storedCode.equals(code)) {
            var user = repository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            user.setActivated(true);
            repository.save(user);

            repository.findByEmail(user.getEmail())
                    .stream()
                    .filter(u -> !u.getUsername().equals(user.getUsername()) && !u.isActivated())
                    .forEach(repository::delete);

            var jwtToken = jwtService.generateToken(user);

            codeVerificationService.deleteCode(user.getEmail());
            rateLimiterService.deleteRateLimit(user.getEmail(), "login", "register");

            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        }
        throw new InvalidCodeException("Invalid verification code");
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );

        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        if (!user.isActivated()) {
            throw new RuntimeException("Please verify your email.");
        }

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
