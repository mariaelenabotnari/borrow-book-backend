package org.borrowbook.borrowbookbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.AuthenticationRequest;
import org.borrowbook.borrowbookbackend.model.dto.AuthenticationResponse;
import org.borrowbook.borrowbookbackend.model.dto.RegisterRequest;
import org.borrowbook.borrowbookbackend.model.dto.VerifyCodeRequest;
import org.borrowbook.borrowbookbackend.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.borrowbook.borrowbookbackend.service.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final EmailService emailService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        service.registerAndSendCode(request);
    }

    @PostMapping("/login")
    public void login(@Valid @RequestBody AuthenticationRequest request) {
        service.loginAndSendCode(request);
    }

    @PostMapping("/verify-code")
    public AuthenticationResponse verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        return service.verifyCode(request);
    }
}
