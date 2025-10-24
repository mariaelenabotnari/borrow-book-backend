package org.borrowbook.borrowbookbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.exception.BookIsAlreadyBorrowedException;
import org.borrowbook.borrowbookbackend.exception.CantBorrowYourOwnBookException;
import org.borrowbook.borrowbookbackend.exception.NotFoundException;
import org.borrowbook.borrowbookbackend.exception.PendingBorrowRequestExistsException;
import org.borrowbook.borrowbookbackend.model.dto.*;
import org.borrowbook.borrowbookbackend.model.entity.Book;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;
import org.borrowbook.borrowbookbackend.repository.BorrowRequestRepository;
import org.borrowbook.borrowbookbackend.repository.UserBookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BorrowService {
    private final BorrowRequestRepository borrowRequestRepository;
    private final UserBookRepository userBookRepository;

    public void saveBorrowRequest(User user, BorrowRequestDTO borrowRequestDTO, Integer userBookId) {
        String username = user.getUsername();
        Optional<BorrowRequest> existingRequest = borrowRequestRepository
                .findByBorrowerUsernameAndUserBookIdAndStatus(
                        username, userBookId, BookRequestStatus.PENDING);

        if (existingRequest.isPresent())
            throw new PendingBorrowRequestExistsException(
                    "You already have a pending borrow request for this book.");

        UserBook userBook = userBookRepository.findById(Long.valueOf(userBookId))
                .orElseThrow(() -> new EntityNotFoundException("UserBook not found with id: " + userBookId));

        if (userBook.getStatus() == BookStatus.BORROWED)
            throw new BookIsAlreadyBorrowedException("This book is already borrowed by another user.");

        if (userBook.getOwner().getId().equals(user.getId()))
            throw new CantBorrowYourOwnBookException("You cannot borrow your own book.");

        BorrowRequest borrowRequest = new BorrowRequest(
                userBook,
                user,
                BookRequestStatus.PENDING,
                LocalDateTime.now(),
                borrowRequestDTO.getMeetingTime(),
                borrowRequestDTO.getDueDate(),
                borrowRequestDTO.getLocation()
        );
        borrowRequestRepository.save(borrowRequest);
    }

    @Transactional
    public void acceptBorrowRequest(User owner, Integer borrowRequestId) {
        BorrowRequest borrowRequest = getBorrowRequestForOwner(owner, borrowRequestId);
        processAccept(borrowRequest);
    }

    @Transactional
    public void rejectBorrowRequest(User owner, Integer borrowRequestId) {
        BorrowRequest borrowRequest = getBorrowRequestForOwner(owner, borrowRequestId);
        processReject(borrowRequest);
    }

    private BorrowRequest getBorrowRequestForOwner(User owner, Integer borrowRequestId) {
        return borrowRequestRepository.findByIdAndUserBookOwnerUsername(borrowRequestId, owner.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Borrow request not found or you're not the owner"));
    }

    public PaginatedResultDTO<BorrowRequestResponseDTO> getIncomingRequests(User owner, Integer size, Integer page) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BorrowRequestResponseDTO> incomingPage = borrowRequestRepository
                .findByUserBookOwnerUsernameAndStatus(owner.getUsername(), BookRequestStatus.PENDING, pageable)
                .map(BorrowRequestResponseDTO::new);
        return new PaginatedResultDTO<>(incomingPage);
    }

    public List<BorrowRequestResponseDTO> getOutgoingRequests(User borrower) {
        List<BorrowRequest> requests = borrowRequestRepository
                .findByBorrowerUsernameAndStatus(borrower.getUsername(), BookRequestStatus.PENDING);
        return requests.stream()
                .map(BorrowRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    public PaginatedResultDTO<AdminBorrowRequestDTO> adminGetAllBorrowRequestsPaginated(
            User admin,
            PaginatedRequestDTO request,
            BookRequestStatus status) {
        if (admin.getRole() != Role.ADMIN)
            throw new SecurityException("Only admins can perform this action.");

        Pageable pageable = PageRequest.of(
                request.getPageIndex() - 1,
                request.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Page<BorrowRequest> borrowRequests = (status != null) ?
                borrowRequestRepository.findByStatus(status, pageable) :
                borrowRequestRepository.findAll(pageable);

        List<AdminBorrowRequestDTO> items = borrowRequests.getContent().stream().map(br -> {
            String bookTitle = br.getUserBook().getBook().getTitle();
            String borrower = br.getBorrower().getUsername();
            String lender = br.getUserBook().getOwner().getUsername();
            String statusStr = br.getStatus().name();
            String requestDate = br.getCreatedAt().toString();
            String responseDate = (br.getBorrowedAt() != null) ? br.getBorrowedAt().toString() : null;
            return new AdminBorrowRequestDTO(
                    br.getId(),
                    bookTitle,
                    borrower,
                    lender,
                    statusStr,
                    requestDate,
                    responseDate
            );
        }).collect(Collectors.toList());

        return new PaginatedResultDTO<>(
                request.getPageIndex(),
                request.getPageSize(),
                borrowRequests.getTotalElements(),
                items
        );
    }

    public void adminAcceptBorrowRequest(User admin, Integer borrowRequestId) {
        if (admin.getRole() != Role.ADMIN)
            throw new SecurityException("Only admins can perform this action.");

        BorrowRequest borrowRequest = borrowRequestRepository.findById(Long.valueOf(borrowRequestId))
                .orElseThrow(() -> new NotFoundException("Borrow request not found"));
        processAccept(borrowRequest);
    }

    public void adminRejectBorrowRequest(User admin, Integer borrowRequestId) {
        if (admin.getRole() != Role.ADMIN)
            throw new SecurityException("Only admins can perform this action.");

        BorrowRequest borrowRequest = borrowRequestRepository.findById(Long.valueOf(borrowRequestId))
                .orElseThrow(() -> new NotFoundException("Borrow request not found"));
        processReject(borrowRequest);
    }

    private void processAccept(BorrowRequest borrowRequest) {
        borrowRequest.setStatus(BookRequestStatus.ACCEPTED);
        borrowRequest.setBorrowedAt(LocalDate.now());

        UserBook userBook = borrowRequest.getUserBook();
        userBook.setStatus(BookStatus.BORROWED);
        userBookRepository.save(userBook);

        List<BorrowRequest> otherPendingRequests = borrowRequestRepository
                .findByUserBookIdAndStatusAndIdNot(userBook.getId(), BookRequestStatus.PENDING, borrowRequest.getId());

        for (BorrowRequest otherRequest : otherPendingRequests)
            otherRequest.setStatus(BookRequestStatus.REJECTED);

        borrowRequestRepository.saveAll(otherPendingRequests);

        borrowRequestRepository.save(borrowRequest);
    }

    private void processReject(BorrowRequest borrowRequest) {
        borrowRequest.setStatus(BookRequestStatus.REJECTED);
        borrowRequestRepository.save(borrowRequest);
    }

}
