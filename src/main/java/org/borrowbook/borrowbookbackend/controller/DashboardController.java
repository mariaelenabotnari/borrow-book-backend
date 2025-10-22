package org.borrowbook.borrowbookbackend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.*;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.service.BookService;
import org.borrowbook.borrowbookbackend.service.BorrowService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/dashboard")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class DashboardController {
    private final BorrowService borrowService;

    @GetMapping("/borrow-requests")
    public PaginatedResultDTO<AdminBorrowRequestDTO> getAllBorrowRequests(PaginatedRequestDTO request) {
        return borrowService.getAllBorrowRequestsPaginated(request);
    }

}
