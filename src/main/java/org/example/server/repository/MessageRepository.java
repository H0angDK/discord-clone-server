package org.example.server.repository;

import org.example.server.model.Message;
import org.example.server.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByRoomOrderByCreatedAtAsc(Room room, Pageable pageable);

    Page<Message> findByRoomIdOrderByCreatedAtAsc(UUID roomId, Pageable pageable);
}
