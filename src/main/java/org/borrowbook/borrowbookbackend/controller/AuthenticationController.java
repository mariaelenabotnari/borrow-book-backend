package org.borrowbook.borrowbookbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.dto.*;
import org.borrowbook.borrowbookbackend.service.EmailService;
import org.borrowbook.borrowbookbackend.service.OAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.borrowbook.borrowbookbackend.service.AuthenticationService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final OAuthService oAuthService;
    private final EmailService emailService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        service.registerAndSendCode(request);
        return "Verification code sent to email.";
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthenticationRequest request) {
        service.loginAndSendCode(request);
        return ResponseEntity.ok("Verification code sent to email.");
    }

    @PostMapping("/verify-code")
    public AuthenticationResponse verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        return service.verifyCode(request.getEmail(), request.getCode());
    }

    @PostMapping("/oauth-register")
    public String oauthRegister(@Valid @RequestBody OAuthRegisterRequest request) {
        oAuthService.registerAndSendCode(request);
        return "Verification code sent to email.";
    }
}
