package org.borrowbook.borrowbookbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.*;
import org.borrowbook.borrowbookbackend.service.AuthenticationService;
import org.borrowbook.borrowbookbackend.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

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
}
