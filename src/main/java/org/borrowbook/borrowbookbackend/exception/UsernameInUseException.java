package org.borrowbook.borrowbookbackend.exception;

public class UsernameInUseException extends RuntimeException {
    public UsernameInUseException(String message) {
        super(message);
    }
    public UsernameInUseException(String message, Throwable e) {super(message, e);}
}
