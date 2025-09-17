package org.borrowbook.borrowbookbackend.exception;

public class MissingFieldException extends RuntimeException {
    public MissingFieldException(String message) {
        super(message);
    }
}
