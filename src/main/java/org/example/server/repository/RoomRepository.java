package org.example.server.repository;

import org.example.server.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    Optional<Room> findByName(String name);


    Page<Room> findByUsersId(UUID usersId, Pageable pageable);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = "users")
    Optional<Room> findWithUsersById(UUID id);

    Page<Room> findRoomByNameContainsIgnoreCaseAndIsPrivateFalse(String name, Pageable pageable);
}
