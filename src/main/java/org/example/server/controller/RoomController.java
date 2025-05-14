package org.example.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.RoomDto;
import org.example.server.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/users")
    private ResponseEntity<Page<RoomDto>> getRoomForUsers(Pageable pageable) {
        return ResponseEntity.ok(roomService.getRoomsForUser(pageable));
    }


    @GetMapping
    private ResponseEntity<Page<RoomDto>> getRooms(@RequestParam(required = false) String query, Pageable pageable) {
        log.info("Getting rooms");
        return ResponseEntity.ok(roomService.searchRooms(query, pageable));
    }

    @PostMapping
    private ResponseEntity<RoomDto> createRoom(@RequestBody RoomDto roomDto) {
        return ResponseEntity.ok(roomService.createRoom(roomDto));
    }

    @PutMapping("/leave/{roomId}")
    private ResponseEntity<Void> leaveRoom(@PathVariable UUID roomId) {
        roomService.leaveRoom(roomId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/add-users")
    private ResponseEntity<Void> addUsersToRoom(@RequestBody RoomDto roomDto) {
        log.info("Adding users to room");
        log.info("RoomDto: {}", roomDto);
        roomService.joinRoom(roomDto.getId(), roomDto.getUsers());
        return ResponseEntity.ok().build();
    }


}
