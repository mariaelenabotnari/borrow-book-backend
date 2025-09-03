package org.borrowbook.borrowbookbackend.auth;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authentication")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/sendcode")
    public ResponseEntity<String> sendCode() {
        emailService.sendVerificationCode("vremerea0@gmail.com", "200435");
        return ResponseEntity.ok("good");
    }

}
