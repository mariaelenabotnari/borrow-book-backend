package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.PaginatedRequestDTO;
import org.borrowbook.borrowbookbackend.model.dto.PaginatedResultDTO;
import org.borrowbook.borrowbookbackend.model.dto.UserDTO;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.service.UsersManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminManageUsersController {
    private final UsersManagementService usersManagementService;

    @GetMapping
    public PaginatedResultDTO<UserDTO> getAllUsers(
            @AuthenticationPrincipal User admin,
            PaginatedRequestDTO request
    ) {
        return usersManagementService.adminGetUsers(admin, request);
    }
}
