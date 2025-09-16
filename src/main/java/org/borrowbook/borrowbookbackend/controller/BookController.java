package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.*;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.service.BookService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/search/google")
    public List<BookSearchDTO> fetchBooksWithGoogle(@RequestParam String q){
        return bookService.fetchBooksWithGoogle(q);
    }

    @PostMapping
    public AddBookResponseDTO addBook(@RequestBody AddBookRequestDTO request, @AuthenticationPrincipal User user) {
        return bookService.addBookToUser(request.getGoogleBookId(), request.getStatus(), user);
    }
}