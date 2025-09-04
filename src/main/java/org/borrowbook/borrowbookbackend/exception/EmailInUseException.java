package org.borrowbook.borrowbookbackend.exception;

public class EmailInUseException extends RuntimeException {
    public EmailInUseException(String message) {
        super(message);
    }
    public EmailInUseException(String message, Throwable e) {super(message, e);}
}
