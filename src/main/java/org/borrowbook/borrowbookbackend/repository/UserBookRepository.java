package org.borrowbook.borrowbookbackend.repository;

import org.borrowbook.borrowbookbackend.model.entity.User;
import org.borrowbook.borrowbookbackend.model.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    List<UserBook> findByOwnerId(Integer ownerId);
}
