package org.borrowbook.borrowbookbackend.exception;

public class MaxOtpAttemptsExceededException extends RuntimeException {
    public MaxOtpAttemptsExceededException(String message) {
        super(message);
    }
}
