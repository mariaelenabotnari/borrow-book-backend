package org.borrowbook.borrowbookbackend.auth;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.config.JwtService;
import org.borrowbook.borrowbookbackend.service.EmailService;
import org.borrowbook.borrowbookbackend.user.Role;
import org.borrowbook.borrowbookbackend.user.User;
import org.borrowbook.borrowbookbackend.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    private final Map<String, String> verificationCodes = new HashMap<>();

    public void registerAndSendCode(RegisterRequest request) {
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        repository.save(user);

        emailService.sendVerificationCode(user.getUsername(), user.getEmail());

    }

    public void loginAndSendCode(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        var user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        emailService.sendVerificationCode(user.getUsername(), user.getEmail());

    }

    public AuthenticationResponse verifyCode(String username, String code) {
        String storedCode = verificationCodes.get(username);

        if (storedCode != null && storedCode.equals(code)) {
            var user = repository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var jwtToken = jwtService.generateToken(user);

            verificationCodes.remove(username);

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
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
