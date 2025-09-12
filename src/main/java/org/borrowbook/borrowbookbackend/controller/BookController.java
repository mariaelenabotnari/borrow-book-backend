package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.*;
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

    @GetMapping
    public List<CollectionBooks> getBooksForUser(@AuthenticationPrincipal User authPrincipal) {
        String username = authPrincipal.getUsername();
        List<CollectionBooks> books = bookService.fetchBooksUser(username);

        return books;
    }

    @GetMapping("/borrowed")
    public List<BorrowedBooks> borrowBook(@AuthenticationPrincipal User authPrincipal) {
        String username = authPrincipal.getUsername();
        List<BorrowedBooks> borrowedBooks = bookService.fetchBorrowedBooks(username);

        return borrowedBooks;
    }

    @DeleteMapping("/{userBookId}")
    public ResponseEntity<Void> deleteBook(@AuthenticationPrincipal User authPrincipal, @PathVariable int userBookId) {
        String username = authPrincipal.getUsername();
        bookService.deleteBook(username, userBookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/google")
    public List<BookSearchDTO> fetchBooksWithGoogle(@RequestParam String q){
        return bookService.fetchBooksWithGoogle(q);
    }

    @PostMapping
    public ResponseEntity<AddBookResponse> addBook(
            @RequestBody AddBookRequest request, @AuthenticationPrincipal User user)
    {
        AddBookResponse response = bookService.addBookToUser(request.getGoogleBookId(), request.getStatus(), user);
        return ResponseEntity.ok(response);
    }
}
