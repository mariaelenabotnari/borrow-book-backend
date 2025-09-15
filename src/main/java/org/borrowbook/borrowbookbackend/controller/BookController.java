package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.*;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.service.BookService;
import org.springframework.http.HttpStatus;
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
    public List<CollectionBookDTO> getBooksForUser(@AuthenticationPrincipal User authPrincipal) {
        String username = authPrincipal.getUsername();
        return bookService.fetchBooksUser(username);
    }

    @GetMapping("/borrowed")
    public List<BorrowedBookDTO> borrowBook(@AuthenticationPrincipal User authPrincipal) {
        String username = authPrincipal.getUsername();
        return bookService.fetchBorrowedBooks(username);
    }

    @DeleteMapping("/{userBookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@AuthenticationPrincipal User authPrincipal, @PathVariable int userBookId) {
        String username = authPrincipal.getUsername();
        bookService.deleteBook(username, userBookId);
    }

    @GetMapping("/search/google")
    public List<BookSearchDTO> fetchBooksWithGoogle(@RequestParam String q){
        return bookService.fetchBooksWithGoogle(q);
    }

    @PostMapping
    public AddBookResponse addBook(@RequestBody AddBookRequest request, @AuthenticationPrincipal User user) {
        return bookService.addBookToUser(request.getGoogleBookId(), request.getStatus(), user);
    }
}