package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.BorrowedBookDTO;
import org.borrowbook.borrowbookbackend.model.dto.CollectionBookDTO;
import org.borrowbook.borrowbookbackend.model.dto.UserDTO;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController{
    private final BookService bookService;


    @GetMapping("/me")
    public UserDTO me(@AuthenticationPrincipal User user){
        return new UserDTO(user);
    }

    @GetMapping("/books")
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

}
