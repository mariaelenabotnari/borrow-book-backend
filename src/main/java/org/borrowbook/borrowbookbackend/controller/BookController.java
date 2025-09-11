package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.AddBookRequest;
import org.borrowbook.borrowbookbackend.model.dto.AddBookResponse;
import org.borrowbook.borrowbookbackend.model.dto.BookSearchDTO;
import org.borrowbook.borrowbookbackend.model.dto.SessionResponse;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
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

    @GetMapping("/search/google")
    public List<BookSearchDTO> fetchBooksWithGoogle(@RequestParam String q){
        return bookService.fetchBooksWithGoogle(q);
    }

    @PostMapping("/add")
    public ResponseEntity<AddBookResponse> addBook(
            @RequestBody AddBookRequest request, @AuthenticationPrincipal User user)
    {
        AddBookResponse response = bookService.addBookToUser(request.getGoogleBookId(), request.getStatus(), user);
        return ResponseEntity.ok(response);
    }
}
