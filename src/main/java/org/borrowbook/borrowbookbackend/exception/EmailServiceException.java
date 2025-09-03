package org.borrowbook.borrowbookbackend.exception;

public class EmailServiceException extends RuntimeException {
    public EmailServiceException(String message) {
        super(message);
    }
    public EmailServiceException(String message, Throwable e) {
        super(message, e);
    }
}
