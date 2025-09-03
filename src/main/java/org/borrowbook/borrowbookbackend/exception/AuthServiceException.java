package org.borrowbook.borrowbookbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthServiceException extends RuntimeException {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage();

        // Decide status code based on error message
        if (message.contains("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        if (message.contains("Username already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message); // 409
        }
        if (message.contains("Email already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message); // 409
        }
        if (message.contains("Invalid email")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message); // 400
        }
        if (message.contains("Password")) { // covers all password-related errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message); // 400
        }

        // Fallback: generic bad request
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
