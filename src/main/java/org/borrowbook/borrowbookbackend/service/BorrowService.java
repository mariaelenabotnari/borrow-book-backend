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

        BorrowRequest borrowRequest = new BorrowRequest(
                userBook,
                user,
                borrowRequestDTO.getLocation(),
                borrowRequestDTO.getMeetingTime()
        );

        borrowRequestRepository.save(borrowRequest);
    }
}
