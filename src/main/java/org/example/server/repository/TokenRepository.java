package org.example.server.repository;

import org.example.server.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    List<Token> findAllByUser_Id(UUID userId);

    Optional<Token> findByToken(String token);
}
