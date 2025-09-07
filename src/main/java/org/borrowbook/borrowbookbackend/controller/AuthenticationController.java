package org.borrowbook.borrowbookbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.*;
import org.borrowbook.borrowbookbackend.service.AuthenticationService;
import org.borrowbook.borrowbookbackend.service.OAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final OAuthService oAuthService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.registerAndSendCode(request);
    }

    @PostMapping("/login")
    public SessionResponse login(@Valid @RequestBody AuthenticationRequest request) {
        return service.loginAndSendCode(request);
    }

    @PostMapping("/verify-code")
    public AuthenticationResponse verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        return service.verifyCode(request);
    }

    @PostMapping("/oauth-register")
    public String oauthRegister(@Valid @RequestBody AuthenticationRequest.OAuthRegisterRequest request) {
        oAuthService.registerAndSendCode(request);
        return "Verification code sent to email.";
    }
}
