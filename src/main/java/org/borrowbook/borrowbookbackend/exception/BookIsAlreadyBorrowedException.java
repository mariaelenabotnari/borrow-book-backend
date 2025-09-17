package org.borrowbook.borrowbookbackend.exception;

public class BookIsAlreadyBorrowedException extends RuntimeException {
  public BookIsAlreadyBorrowedException(String message) {
    super(message);
  }
  public BookIsAlreadyBorrowedException(String message, Throwable cause) {
    super(message, cause);
  }
}