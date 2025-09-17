package org.borrowbook.borrowbookbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.exception.BookIsAlreadyBorrowedException;
import org.borrowbook.borrowbookbackend.exception.CantBorrowYourOwnBookException;
import org.borrowbook.borrowbookbackend.exception.MissingFieldException;
import org.borrowbook.borrowbookbackend.exception.PendingBorrowRequestExistsException;
import org.borrowbook.borrowbookbackend.model.dto.BorrowRequestDTO;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;
import org.borrowbook.borrowbookbackend.repository.BorrowRequestRepository;
import org.borrowbook.borrowbookbackend.repository.UserBookRepository;
import org.borrowbook.borrowbookbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BorrowService {
    private final BorrowRequestRepository borrowRequestRepository;
    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;

    public void saveBorrowRequest(String username, BorrowRequestDTO borrowRequestDTO, Integer userBookId) {
        Optional<BorrowRequest> existingRequest = borrowRequestRepository
                .findByBorrower_UsernameAndUserBook_IdAndStatus(
                        username, userBookId, BookRequestStatus.PENDING);

        if (existingRequest.isPresent()) {
            throw new PendingBorrowRequestExistsException(
                    "You already have a pending borrow request for this book.");
        }

        User borrower = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        UserBook userBook = userBookRepository.findById(Long.valueOf(userBookId))
                .orElseThrow(() -> new EntityNotFoundException("UserBook not found with id: " + userBookId));

        if (userBook.getStatus() == BookStatus.BORROWED) {
            throw new BookIsAlreadyBorrowedException("This book is already borrowed by another user.");
        }

        if (userBook.getOwner().getId().equals(borrower.getId())) {
            throw new CantBorrowYourOwnBookException("You cannot borrow your own book.");
        }

        if (borrowRequestDTO.getMeeting_time() == null) {
            throw new MissingFieldException("Meeting time is required for a borrow request.");
        }

        if (borrowRequestDTO.getDue_date() == null) {
            throw new MissingFieldException("Due date is required for a borrow request.");
        }

        if (borrowRequestDTO.getLocation() == null || borrowRequestDTO.getLocation().trim().isEmpty()) {
            throw new MissingFieldException("Location is required for a borrow request.");
        }

        BorrowRequest borrowRequest = new BorrowRequest(
                userBook,
                borrower,
                BookRequestStatus.PENDING,
                LocalDate.now(),
                borrowRequestDTO.getMeeting_time(),
                borrowRequestDTO.getDue_date(),
                borrowRequestDTO.getLocation()
        );
        borrowRequestRepository.save(borrowRequest);
    }
}
