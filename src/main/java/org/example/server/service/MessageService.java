package org.example.server.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.MessageDto;
import org.example.server.model.Message;
import org.example.server.model.Room;
import org.example.server.model.User;
import org.example.server.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final UserService userService;

    @Transactional
    public MessageDto sendMessage(Room room, String content, User sender) {
        Message message = Message.builder()
                .content(content)
                .room(room)
                .sender(sender)
                .build();


        var savedMessage = messageRepository.save(message);

        return convertToDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> getMessageHistory(UUID roomId, Pageable pageable) {
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId, pageable)
                .map(this::convertToDto);
    }

    private MessageDto convertToDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSender().getId())
                .roomId(message.getRoom().getId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
