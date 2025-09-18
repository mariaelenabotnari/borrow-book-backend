package org.borrowbook.borrowbookbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.BorrowRequestDTO;
import org.borrowbook.borrowbookbackend.model.dto.BorrowRequestResponseDTO;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.service.BookService;
import org.borrowbook.borrowbookbackend.service.BorrowService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/borrow")
public class BorrowRequestController {
    private final BorrowService borrowService;

    @PostMapping("/request/{userBookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void handleBorrowRequest(
            @AuthenticationPrincipal User authPrincipal,
            @Valid @RequestBody BorrowRequestDTO borrowRequestDTO,
            @PathVariable int userBookId)
    {
        borrowService.saveBorrowRequest(authPrincipal, borrowRequestDTO, userBookId);
    }

    @PutMapping("/accept/{borrowRequestId}")
    public void acceptBorrowRequest(
            @AuthenticationPrincipal User authPrincipal,
            @PathVariable Integer borrowRequestId)
    {
        borrowService.acceptBorrowRequest(authPrincipal, borrowRequestId);
    }

    @PutMapping("/reject/{borrowRequestId}")
    public void rejectBorrowRequest(
            @AuthenticationPrincipal User authPrincipal,
            @PathVariable Integer borrowRequestId)
    {
        borrowService.rejectBorrowRequest(authPrincipal, borrowRequestId);
    }

    @GetMapping("/incoming")
    public List<BorrowRequestResponseDTO> getIncomingRequests(@AuthenticationPrincipal User authPrincipal) {
        return borrowService.getIncomingRequests(authPrincipal);
    }

    @GetMapping("/outgoing")
    public List<BorrowRequestResponseDTO> getOutgoingRequests(@AuthenticationPrincipal User authPrincipal) {
        return borrowService.getOutgoingRequests(authPrincipal);
    }
}
