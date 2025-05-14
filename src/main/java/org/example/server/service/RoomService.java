package org.example.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.RoomDto;
import org.example.server.model.Room;
import org.example.server.model.User;
import org.example.server.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<RoomDto> getRoomsForUser(Pageable pageable) {
        User user = userService.getCurrentUser();
        return roomRepository.findByUsersId(user.getId(), pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<RoomDto> searchRooms(String query, Pageable pageable) {
        return roomRepository.findRoomByNameContainsIgnoreCaseAndIsPrivateFalse(query, pageable)
                .map(this::convertToDto);
    }

    @Transactional
    public RoomDto createRoom(RoomDto roomDto) {
        User creator = userService.getCurrentUser();
        Room newRoom = Room.builder()
                .name(roomDto.getName())
                .isPrivate(roomDto.isPrivate())
                .build();

        newRoom.addUser(creator);
        Room savedRoom = roomRepository.save(newRoom);
        return convertToDto(savedRoom);
    }

    @Transactional
    public void joinRoom(UUID roomId, List<String> usernames) {
        Room room = roomRepository.findWithUsersById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        List<User> users = userService.findByUsernames(usernames);

        users.forEach(user -> {
            if (!room.containsUser(user)) {
                room.addUser(user);
            }
        });
        roomRepository.save(room);
    }

    @Transactional
    public void leaveRoom(UUID roomId) {
        User user = userService.getCurrentUser();
        Room room = roomRepository.findWithUsersById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!room.containsUser(user)) {
            throw new IllegalStateException("User is not a member of this room");
        }

        room.removeUser(user);
    }

    private RoomDto convertToDto(Room room) {
        return RoomDto.builder()
                .id(room.getId())
                .name(room.getName())
                .isPrivate(room.isPrivate())
                .createdAt(room.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Room getRoomById(UUID id) {
        return roomRepository.findWithUsersById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }
}