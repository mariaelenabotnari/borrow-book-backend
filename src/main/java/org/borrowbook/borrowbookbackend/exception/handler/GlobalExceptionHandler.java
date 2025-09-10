package org.borrowbook.borrowbookbackend.exception.handler;


import lombok.extern.log4j.Log4j2;
import org.borrowbook.borrowbookbackend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
            RateLimitException.class,
            MaxOtpAttemptsExceededException.class
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

    @ExceptionHandler(exception = {
            BadCredentialsException.class,
            RefreshTokenException.class
    })
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
        log.error("Exception: {} | Message: {} | Cause: {} | Stacktrace: {}",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex.getCause(),
                ex.getStackTrace());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ExceptionResult globalException = new ExceptionResult(
                "Internal Server Error",
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex) throws NoResourceFoundException {
        String path = ex.getResourcePath();
        if (path.equals("/favicon.ico") || path.startsWith("/.well-known/")) {
            return ResponseEntity.noContent().build(); // 204 instead of 500
        }
        throw ex;
    }
}

