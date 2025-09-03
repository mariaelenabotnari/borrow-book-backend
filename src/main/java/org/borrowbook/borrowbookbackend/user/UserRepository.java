package org.borrowbook.borrowbookbackend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // custom method that automatically generates SQL query to find users based on username
    Optional<User> findByUsername(String username);
}
