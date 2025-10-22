package org.borrowbook.borrowbookbackend.exception.handler;


import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.borrowbook.borrowbookbackend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
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
            MaxOtpAttemptsExceededException.class,
            EntityNotFoundException.class
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

    @ExceptionHandler(exception = {
            AuthorizationDeniedException.class
    })
    public ResponseEntity<Object> handleAuthorizationDeniedException(RuntimeException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.FORBIDDEN;

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String message = "Invalid request format";

        ExceptionResult globalException = new ExceptionResult(
                message,
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;

        ExceptionResult globalException = new ExceptionResult(
                "Request method not supported: " + ex.getMethod(),
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ExceptionResult globalException = new ExceptionResult(
                "Missing required request parameter: " + ex.getParameterName(),
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Object> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ExceptionResult globalException = new ExceptionResult(
                "Missing required request part: " + ex.getRequestPartName(),
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.NOT_ACCEPTABLE;

        ExceptionResult globalException = new ExceptionResult(
                "Requested media type is not acceptable",
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }


    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupportedException(org.springframework.web.HttpMediaTypeNotSupportedException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;

        ExceptionResult globalException = new ExceptionResult(
                "Unsupported media type: " + ex.getContentType(),
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }

    @ExceptionHandler({
            PendingBorrowRequestExistsException.class,
            BookIsAlreadyBorrowedException.class,
            CantBorrowYourOwnBookException.class
    })
    public ResponseEntity<Object> handleBorrowRequestExceptions(RuntimeException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ExceptionResult globalException = new ExceptionResult(
                ex.getMessage(),
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }

    @ExceptionHandler(MissingFieldException.class)
    public ResponseEntity<Object> handleMissingFieldException(MissingFieldException ex) {
        log.error(ex.getMessage(), ex.getCause());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ExceptionResult globalException = new ExceptionResult(
                ex.getMessage(),
                status,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(globalException, status);
    }
}

