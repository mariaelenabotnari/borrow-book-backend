package org.borrowbook.borrowbookbackend.auth;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.borrowbook.borrowbookbackend.auth.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest request
    ) {
        service.registerAndSendCode(request);
        return ResponseEntity.ok("Verification code sent to email.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ) {
        service.loginAndSendCode(request);
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<AuthenticationResponse> verifyCode(
            @RequestParam String username,
            @RequestParam String code
    ) {
        return ResponseEntity.ok(service.verifyCode(username, code));
    }
}
