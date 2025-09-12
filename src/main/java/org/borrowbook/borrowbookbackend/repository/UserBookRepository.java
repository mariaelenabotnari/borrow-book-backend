package org.borrowbook.borrowbookbackend.repository;

import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    List<UserBook> findByOwner_Username(String username);
    Optional<UserBook> findByIdAndOwner_Username(Integer id, String username);
}
