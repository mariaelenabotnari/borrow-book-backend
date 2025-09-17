package org.borrowbook.borrowbookbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.BorrowRequestDTO;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.service.BookService;
import org.borrowbook.borrowbookbackend.service.BorrowService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/borrow")
public class BorrowRequestController {
    private final BorrowService borrowService;

    @PostMapping("/request/{userBookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void handleBorrowRequest(@AuthenticationPrincipal User authPrincipal, @Valid @RequestBody BorrowRequestDTO borrowRequestDTO, @PathVariable int userBookId) {
        String username = authPrincipal.getUsername();
        borrowService.saveBorrowRequest(username, borrowRequestDTO, userBookId);
    }
}
