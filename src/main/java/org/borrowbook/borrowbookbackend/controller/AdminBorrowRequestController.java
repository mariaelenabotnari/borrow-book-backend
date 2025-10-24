package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.AdminBorrowRequestDTO;
import org.borrowbook.borrowbookbackend.model.dto.PaginatedRequestDTO;
import org.borrowbook.borrowbookbackend.model.dto.PaginatedResultDTO;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;
import org.borrowbook.borrowbookbackend.service.BorrowService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/admin/borrow-requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBorrowRequestController {
    private final BorrowService borrowService;

    @GetMapping
    public PaginatedResultDTO<AdminBorrowRequestDTO> getAllBorrowRequests(
            @AuthenticationPrincipal User admin,
            PaginatedRequestDTO request,
            @RequestParam(required = false)BookRequestStatus status) {
        return borrowService.adminGetAllBorrowRequestsPaginated(admin, request, status);
    }

    @PostMapping("/accept/{id}")
    public void adminAcceptBorrowRequest(
            @AuthenticationPrincipal User admin,
            @PathVariable Integer id) {
        borrowService.adminAcceptBorrowRequest(admin, id);
    }

    @PostMapping("/reject/{id}")
    public void adminRejectBorrowRequest(
            @AuthenticationPrincipal User admin,
            @PathVariable Integer id) {
        borrowService.adminRejectBorrowRequest(admin, id);
    }
}
