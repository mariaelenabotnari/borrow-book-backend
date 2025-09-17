package org.borrowbook.borrowbookbackend.repository;

import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByBorrowerUsernameAndUserBookStatus(String borrower_username, BookStatus bookStatus);
    BorrowRequest findByBorrowerUsername(String username);
    Optional<BorrowRequest> findByBorrower_UsernameAndUserBook_IdAndStatus(String username, Integer userBookId, BookRequestStatus status);
}

