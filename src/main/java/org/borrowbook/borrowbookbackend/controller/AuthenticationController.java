package org.borrowbook.borrowbookbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.AuthenticationRequestDTO;
import org.borrowbook.borrowbookbackend.model.dto.RegisterRequestDTO;
import org.borrowbook.borrowbookbackend.model.dto.SessionResponseDTO;
import org.borrowbook.borrowbookbackend.model.dto.VerifyCodeRequestDTO;
import org.borrowbook.borrowbookbackend.service.AuthenticationService;
import org.borrowbook.borrowbookbackend.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return service.registerAndSendCode(request);
    }

    @PostMapping("/login")
    public SessionResponseDTO login(@Valid @RequestBody AuthenticationRequestDTO request, HttpServletResponse response) {
        return service.loginAndSendCode(request, response);
    }

    @PostMapping("/verify-code")
    public void verifyCode(@Valid @RequestBody VerifyCodeRequestDTO request, HttpServletResponse response) {
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
