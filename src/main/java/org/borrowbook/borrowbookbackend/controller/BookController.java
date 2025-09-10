package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.SessionResponse;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.repository.BookRepository;
import org.borrowbook.borrowbookbackend.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<List<Book>> getBooksForUser(@PathVariable Integer userId) {
        List<Book> books = bookService.fetchBooksUser(userId);

        return ResponseEntity.ok(books);
    }

    @PostMapping("/borrowed/user/{userId}")
    public ResponseEntity<SessionResponse> borrowBook(@PathVariable Integer userId) {
        List<Book> borrowedBooks = bookService.fetchBorrowedBooks(userId);

        return ResponseEntity.ok((SessionResponse) borrowedBooks);
    }
}
