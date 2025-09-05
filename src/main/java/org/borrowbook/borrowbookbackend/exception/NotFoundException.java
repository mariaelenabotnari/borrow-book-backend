package org.borrowbook.borrowbookbackend.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {super(message);}
    public NotFoundException(String message, Throwable e) {super(message, e);}
}
