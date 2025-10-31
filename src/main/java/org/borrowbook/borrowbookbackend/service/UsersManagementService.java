package org.borrowbook.borrowbookbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.model.dto.PaginatedRequestDTO;
import org.borrowbook.borrowbookbackend.model.dto.PaginatedResultDTO;
import org.borrowbook.borrowbookbackend.model.dto.UserDTO;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UsersManagementService {
    private final UserRepository userRepository;

    public PaginatedResultDTO<UserDTO> adminGetUsers(
            User admin,
            PaginatedRequestDTO request) {

        if (admin.getRole() != Role.ADMIN)
            throw new SecurityException("Only admins can perform this action.");

        Pageable pageable = PageRequest.of(
                request.getPageIndex() - 1,
                request.getPageSize(),
                Sort.by("username").ascending()
        );

        Page<User> usersPage = userRepository.findByRole(Role.USER, pageable);

        List<UserDTO> users = usersPage.getContent()
                .stream()
                .map(UserDTO::new)
                .toList();

        return new PaginatedResultDTO<>(
                request.getPageIndex(),
                request.getPageSize(),
                usersPage.getTotalElements(),
                users
        );
    }

    public UserDTO adminFindUser(User admin, String username) {
        if (admin.getRole() != Role.ADMIN)
            throw new SecurityException("Only admins can perform this action.");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        return new UserDTO(user);
    }

    @Transactional
    public void adminDeleteUser(User admin, String username) {
        if (admin.getRole() != Role.ADMIN)
            throw new SecurityException("Only admins can perform this action.");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        if (user.getRole() == Role.ADMIN)
            throw new SecurityException("Cannot delete another admin user.");

        userRepository.delete(user);
    }
}
