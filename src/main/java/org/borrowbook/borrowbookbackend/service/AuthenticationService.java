package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.dto.AuthenticationRequest;
import org.borrowbook.borrowbookbackend.dto.AuthenticationResponse;
import org.borrowbook.borrowbookbackend.dto.RegisterRequest;
import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.entities.User;
import org.borrowbook.borrowbookbackend.exception.EmailInUseException;
import org.borrowbook.borrowbookbackend.exception.UsernameInUseException;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration verificationCodeTTL = Duration.ofHours(1);

    public void registerAndSendCode(RegisterRequest request) {
        repository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                    if (user.isActivated()) {
                        throw new UsernameInUseException("Username is already in use");
                    }
                });

        repository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    if (user.isActivated()) {
                        throw new EmailInUseException("Email is already in use");
                    }
                });

        var existingUser = repository.findByEmail(request.getEmail())
                .orElse(null);
        User userToSave;
        if (existingUser != null && !existingUser.isActivated()) {
            existingUser.setUsername(request.getUsername());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            userToSave = existingUser;
        }
        else if (existingUser != null && existingUser.isActivated()) {
            throw new EmailInUseException("Email is already in use");
        }
        else {
            userToSave = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.USER)
                    .activated(false)
                    .build();
        }

        repository.save(userToSave);

        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        try {
            redisTemplate.opsForValue().set(
                    "verification:" + userToSave.getUsername(),
                    code,
                    verificationCodeTTL
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to save code in Redis", e);
        }

        try {
            emailService.sendVerificationCode(userToSave.getEmail(), code);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }

    public void loginAndSendCode(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        var user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        redisTemplate.opsForValue().set(
                "verification:" + user.getUsername(),
                code,
                verificationCodeTTL
        );

        emailService.sendVerificationCode(user.getEmail(), code);
    }

    public AuthenticationResponse verifyCode(String username, String code) {
        String storedCode = (String) redisTemplate.opsForValue().get("verification:" + username);

        if (storedCode != null && storedCode.equals(code)) {
            var user = repository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setActivated(true);
            repository.save(user);

            repository.findByEmail(user.getEmail())
                    .stream()
                    .filter(u -> !u.getUsername().equals(user.getUsername()) && !u.isActivated())
                    .forEach(repository::delete);

            var jwtToken = jwtService.generateToken(user);

            redisTemplate.delete("verification:" + username);

            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        }
        throw new RuntimeException("Invalid verification code");
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
                        request.getUsername(),
                        request.getPassword())
        );

        var user = repository.findByUsername(request.getUsername())
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
