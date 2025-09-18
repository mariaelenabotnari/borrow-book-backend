package org.borrowbook.borrowbookbackend.repository;

import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.borrowbook.borrowbookbackend.model.enums.BookRequestStatus;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByBorrowerUsernameAndUserBookStatus(String borrowerUsername, BookStatus bookStatus);

    Optional<BorrowRequest> findByBorrowerUsernameAndUserBookIdAndStatus(
            String username, Integer userBookId, BookRequestStatus status
    );

    Optional<BorrowRequest> findByIdAndUserBookOwnerUsername(Integer id, String ownerUsername);

    List<BorrowRequest> findByUserBookIdAndStatusAndIdNot(
            Integer userBookId, BookRequestStatus status, Integer excludeRequestId
    );

    List<BorrowRequest> findByUserBookOwnerUsernameAndStatus(String ownerUsername, BookRequestStatus status);

    Page<BorrowRequest> findByUserBookOwnerUsernameAndStatus(String ownerUsername, BookRequestStatus status, Pageable pageable);

    List<BorrowRequest> findByBorrowerUsernameAndStatus(String borrowerUsername, BookRequestStatus status);
}

