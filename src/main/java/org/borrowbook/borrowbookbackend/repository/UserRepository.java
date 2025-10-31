package org.borrowbook.borrowbookbackend.repository;

import org.borrowbook.borrowbookbackend.Role;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByEmail(String email);
    Optional<User> findByEmailAndActivatedTrue(String email);
    Page<User> findByRole(Role role, Pageable pageable);
}
