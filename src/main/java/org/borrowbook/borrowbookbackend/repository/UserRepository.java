package org.borrowbook.borrowbookbackend.repository;

import org.borrowbook.borrowbookbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // custom methods that automatically generate SQL queries to find users based on username/email
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
