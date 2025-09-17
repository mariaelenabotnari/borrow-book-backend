package org.borrowbook.borrowbookbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.exception.PendingBorrowRequestExistsException;
import org.borrowbook.borrowbookbackend.model.dto.BorrowRequestDTO;
import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;
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
        BorrowRequest borrowRequest = new BorrowRequest();

        User borrower = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        UserBook userBook = userBookRepository.findById(Long.valueOf(userBookId))
                .orElseThrow(() -> new EntityNotFoundException("UserBook not found with id: " + userBookId));

        borrowRequest.setUserBook(userBook);
        borrowRequest.setBorrower(borrower);
        borrowRequest.setStatus(BookRequestStatus.PENDING);
        borrowRequest.setCreated_at(LocalDate.now());
        borrowRequest.setMeeting_time(borrowRequestDTO.getMeeting_time());
        borrowRequest.setLocation(borrowRequestDTO.getLocation());
        borrowRequestRepository.save(borrowRequest);
    }
}
