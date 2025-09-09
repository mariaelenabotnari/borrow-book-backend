package org.borrowbook.borrowbookbackend.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.*;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.exception.*;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.borrowbook.borrowbookbackend.util.Generator;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final CookieService cookieService;

    private final Generator generator;
    private final CodeVerificationService codeVerificationService;
    private final RateLimiterService rateLimiterService;

    @Transactional
    public SessionResponse registerAndSendCode(RegisterRequest request) {
        this.checkRateLimit("register", request.getEmail());

        if (repository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameInUseException("Username is already in use");
        }
        var existingUser = repository.findByEmailAndActivatedTrue(request.getEmail()).orElse(null);

        if (existingUser != null) {
            throw new EmailInUseException("Email is already in use. Please sign in!");
        }

        User user = new User(request.getUsername(), request.getEmail(), passwordEncoder.encode(request.getPassword()));

        repository.save(user);
        return this.sendCode(user, true);
    }

    @Transactional
    public SessionResponse loginAndSendCode(AuthenticationRequest request) {
        this.checkRateLimit("login", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        return this.sendCode(user, false);
    }

    @Transactional
    public void verifyCode(VerifyCodeRequest request, HttpServletResponse response) {
        VerificationSession session = codeVerificationService.verifyCode(request.getSessionId(), request.getCode());

        if (session == null) {
            throw new InvalidCodeException("Invalid verification code");
        }

        User user = repository.findByUsername(session.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setActivated(true);
        repository.save(user);

        repository.findAllByEmail(session.getEmail()).stream()
                .filter(u -> !u.getUsername().equals(session.getUsername()) && !u.isActivated())
                .forEach(repository::delete);

        String jwtToken = jwtService.generateToken(user);
        response.addHeader("Set-Cookie", cookieService.createJwtCookie(jwtToken).toString());

        rateLimiterService.deleteRateLimit(session.getEmail(), "register");
        rateLimiterService.deleteRateLimit(session.getUsername(), "login");
    }


    private SessionResponse sendCode(User user, boolean isNew) {
        String code = generator.generateOTP();
        VerificationSession verificationSession = new VerificationSession(user.getEmail(), user.getUsername(), code, 0);
        String sessionId = generator.generateSessionId();
        codeVerificationService.storeSession(sessionId, verificationSession);
        if (isNew) {
            codeVerificationService.addSessionToEmail(user.getEmail(), sessionId);
        }
        emailService.sendVerificationCode(user.getEmail(), code);
        return new SessionResponse(sessionId);
    }

    private void checkRateLimit(String prefix, String identifier) {
        long retryAfter = rateLimiterService.checkRateLimit(prefix, identifier, 5, 15 * 60);
        if (retryAfter > 0) {
            throw new RateLimitException("Too many login attempts. Try again in " + retryAfter + " seconds.");
        }
    }
}