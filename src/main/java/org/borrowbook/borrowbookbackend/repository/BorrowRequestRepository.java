package org.borrowbook.borrowbookbackend.repository;

import org.borrowbook.borrowbookbackend.model.entity.BorrowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByBorrowerIdAndStatus(Integer borrowerId, String status);
}
