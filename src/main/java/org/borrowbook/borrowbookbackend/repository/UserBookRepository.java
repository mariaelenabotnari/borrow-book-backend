package org.borrowbook.borrowbookbackend.repository;

import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.borrowbook.borrowbookbackend.model.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    List<UserBook> findByOwner_Username(String username);
    Optional<UserBook> findByIdAndOwner_Username(Integer id, String username);

    Page<UserBook> findByBookTitleContainingIgnoreCaseAndStatus(String title, BookStatus status, Pageable pageable);
}
