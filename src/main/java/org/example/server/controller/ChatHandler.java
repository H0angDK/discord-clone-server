package org.example.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.MessageDto;
import org.example.server.model.Room;
import org.example.server.model.User;
import org.example.server.service.MessageService;
import org.example.server.service.RoomService;
import org.example.server.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {
    private final MessageService messageService;
    private final RoomService roomService;
    private final UserService userService;

    private final Map<UUID, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        Room room = getRoomData(session);
        sessions.computeIfAbsent(room.getId(), k -> ConcurrentHashMap.newKeySet()).add(session);
        sendMessageHistory(session, room.getId());
    }

    private void sendMessageHistory(WebSocketSession session, UUID roomId) {
        var messages = messageService.getMessageHistory(roomId, null);
        messages.forEach(message -> {
            sendMessage(session, message);
        });
    }

    private void broadcastMessage(UUID roomId, MessageDto message) {
        sessions
                .getOrDefault(roomId, Collections.emptySet())
                .forEach(session -> {
                    if (session.isOpen()) {
                        sendMessage(session, message);
                    }
                });
    }

    private void sendMessage(WebSocketSession session, MessageDto message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Error sending message", e);
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session,
                                     @NonNull TextMessage message) throws Exception {
        Room room = getRoomData(session);
        User user = getCurrentUser(session);
        MessageDto savedMessage = messageService.sendMessage(room, message.getPayload(), user);
        broadcastMessage(room.getId(), savedMessage);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull CloseStatus status) throws Exception {
        Room room = getRoomData(session);
        sessions.getOrDefault(room.getId(), Collections.emptySet()).remove(session);
    }

    private User getCurrentUser(WebSocketSession session) {
        return (User) session.getAttributes().get("user");
    }

    private Room getRoomData(WebSocketSession session) {
        return (Room) session.getAttributes().get("room");
    }
}
