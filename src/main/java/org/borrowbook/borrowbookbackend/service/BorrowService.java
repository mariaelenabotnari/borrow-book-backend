package org.borrowbook.borrowbookbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.exception.BookIsAlreadyBorrowedException;
import org.borrowbook.borrowbookbackend.exception.CantBorrowYourOwnBookException;
import org.borrowbook.borrowbookbackend.exception.MissingFieldException;
import org.borrowbook.borrowbookbackend.exception.PendingBorrowRequestExistsException;
import org.borrowbook.borrowbookbackend.model.dto.BorrowRequestDTO;
import org.borrowbook.borrowbookbackend.model.dto.BorrowRequestResponseDTO;
import org.borrowbook.borrowbookbackend.model.dto.PaginatedResultDTO;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

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
        String username =  user.getUsername();
        Optional<BorrowRequest> existingRequest = borrowRequestRepository
                .findByBorrowerUsernameAndUserBookIdAndStatus(
                        username, userBookId, BookRequestStatus.PENDING);

        if (existingRequest.isPresent()) {
            throw new PendingBorrowRequestExistsException(
                    "You already have a pending borrow request for this book.");
        }

        UserBook userBook = userBookRepository.findById(Long.valueOf(userBookId))
                .orElseThrow(() -> new EntityNotFoundException("UserBook not found with id: " + userBookId));

        if (userBook.getStatus() == BookStatus.BORROWED) {
            throw new BookIsAlreadyBorrowedException("This book is already borrowed by another user.");
        }

        if (userBook.getOwner().getId().equals(user.getId())) {
            throw new CantBorrowYourOwnBookException("You cannot borrow your own book.");
        }

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

        borrowRequest.setStatus(BookRequestStatus.ACCEPTED);
        borrowRequest.setBorrowedAt(LocalDate.now());

        UserBook userBook = borrowRequest.getUserBook();
        userBook.setStatus(BookStatus.BORROWED);
        userBookRepository.save(userBook);

        List<BorrowRequest> otherPendingRequests = borrowRequestRepository
                .findByUserBookIdAndStatusAndIdNot(userBook.getId(), BookRequestStatus.PENDING, borrowRequestId);

        for (BorrowRequest otherRequest : otherPendingRequests) {
            otherRequest.setStatus(BookRequestStatus.REJECTED);
        }
        borrowRequestRepository.saveAll(otherPendingRequests);

        borrowRequestRepository.save(borrowRequest);
    }

    @Transactional
    public void rejectBorrowRequest(User owner, Integer borrowRequestId) {
        BorrowRequest borrowRequest = getBorrowRequestForOwner(owner, borrowRequestId);
        borrowRequest.setStatus(BookRequestStatus.REJECTED);
        borrowRequestRepository.save(borrowRequest);
    }

    private BorrowRequest getBorrowRequestForOwner(User owner, Integer borrowRequestId) {
        return borrowRequestRepository.findByIdAndUserBookOwnerUsername(borrowRequestId, owner.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Borrow request not found or you're not the owner"));
    }

    public PaginatedResultDTO<BorrowRequestResponseDTO> getIncomingRequests(User owner, Integer size, Integer page) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BorrowRequestResponseDTO> incomingPage = borrowRequestRepository
                .findByUserBookOwnerUsernameAndStatus(owner.getUsername(), BookRequestStatus.PENDING, pageable)
                .map(BorrowRequestResponseDTO::new);;
        return new PaginatedResultDTO<>(incomingPage);
    }

    public List<BorrowRequestResponseDTO> getOutgoingRequests(User borrower) {
        List<BorrowRequest> requests = borrowRequestRepository
                .findByBorrowerUsernameAndStatus(borrower.getUsername(), BookRequestStatus.PENDING);
        return requests.stream()
                .map(BorrowRequestResponseDTO::new)
                .collect(Collectors.toList());
    }
}
