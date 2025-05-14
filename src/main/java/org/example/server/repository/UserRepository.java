package org.example.server.repository;

import org.example.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findUserByUsernameContainingIgnoreCaseAndIdNot(String username, UUID id);

    @Query("SELECT u FROM User u WHERE u.username IN :usernames")
    List<User> findUserByUsernamesCaseSensitive(@Param("usernames") List<String> usernames);
}
