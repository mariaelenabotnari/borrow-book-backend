package org.borrowbook.borrowbookbackend.exception;

public class InvalidCodeException extends RuntimeException {
    public InvalidCodeException(String message) {
        super(message);
    }
    public InvalidCodeException(String message, Throwable e) {super(message, e);}
}
