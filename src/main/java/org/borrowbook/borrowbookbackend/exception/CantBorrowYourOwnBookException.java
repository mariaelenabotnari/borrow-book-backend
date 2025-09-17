package org.borrowbook.borrowbookbackend.exception;

public class CantBorrowYourOwnBookException extends RuntimeException {
    public CantBorrowYourOwnBookException(String message) {
        super(message);
    }
    public CantBorrowYourOwnBookException(String message, Throwable cause) {
        super(message, cause);
    }
}
