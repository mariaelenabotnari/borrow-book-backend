package org.borrowbook.borrowbookbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.AuthenticationRequest;
import org.borrowbook.borrowbookbackend.model.dto.RegisterRequest;
import org.borrowbook.borrowbookbackend.model.dto.SessionResponse;
import org.borrowbook.borrowbookbackend.model.dto.VerifyCodeRequest;
import org.borrowbook.borrowbookbackend.service.AuthenticationService;
import org.borrowbook.borrowbookbackend.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.registerAndSendCode(request);
    }

    @PostMapping("/login")
    public SessionResponse login(@Valid @RequestBody AuthenticationRequest request, HttpServletResponse response) {
        return service.loginAndSendCode(request, response);
    }

    @PostMapping("/verify-code")
    public void verifyCode(@Valid @RequestBody VerifyCodeRequest request, HttpServletResponse response) {
         service.verifyCode(request, response);
    }

    @GetMapping("/get")
    public void get(){}

    @PostMapping("/refreshtoken")
    public ResponseEntity<Void> refreshToken(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        refreshTokenService.refreshAccessToken(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        service.logout(request, response);
        return ResponseEntity.ok("Logged out successfully");
    }
}
