package org.borrowbook.borrowbookbackend.exception.handler;


import lombok.extern.log4j.Log4j2;
import org.borrowbook.borrowbookbackend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        String errorMessage = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ExceptionResult globalException = new ExceptionResult(
                errorMessage,
                badRequest,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );

        return new ResponseEntity<>(globalException, badRequest);
    }

    @ExceptionHandler(exception={
            EmailInUseException.class,
            UsernameInUseException.class,
            InvalidCodeException.class,
            EmailServiceException.class,
            RateLimitException.class
    })
    public ResponseEntity<Object> handleBadRequestException(RuntimeException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ExceptionResult globalException = new ExceptionResult(
                ex.getMessage(),
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.NOT_FOUND;

        ExceptionResult globalException = new ExceptionResult(
                ex.getMessage(),
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleUnauthorizedException(RuntimeException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ExceptionResult globalException = new ExceptionResult(
                ex.getMessage(),
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        log.error("Exception: {} | Message: {} | Cause: {}",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex.getCause());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ExceptionResult globalException = new ExceptionResult(
                "Internal Server Error",
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }
}

