package org.borrowbook.borrowbookbackend.service;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public ResponseCookie createJwtCookie(String jwtToken) {
        return ResponseCookie.from("access_token", jwtToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(JwtService.JWT_EXPIRATION_MS/1000)
                .sameSite("Strict")
                .build();
    }
}
