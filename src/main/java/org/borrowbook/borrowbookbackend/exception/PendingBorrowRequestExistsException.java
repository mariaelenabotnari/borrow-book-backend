package org.borrowbook.borrowbookbackend.exception;

public class PendingBorrowRequestExistsException extends RuntimeException {
    public PendingBorrowRequestExistsException(String message) {
        super(message);
    }
    public PendingBorrowRequestExistsException(String message, Throwable cause) {super(message, cause);}
}
