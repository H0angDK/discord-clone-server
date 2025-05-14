package org.example.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.UserDto;
import org.example.server.model.Room;
import org.example.server.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CallingHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentHashMap<UUID, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    private User getUserData(WebSocketSession session) {
        return (User) session.getAttributes().get("user");
    }

    private Room getRoomData(WebSocketSession session) {
        return (Room) session.getAttributes().get("room");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = getUserData(session);
        Room room = getRoomData(session);

        Set<WebSocketSession> roomSessions = rooms.computeIfAbsent(room.getId(), k -> ConcurrentHashMap.newKeySet());
        roomSessions.add(session);
        broadcastUserList(room);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User sender = getUserData(session);
        Room room = getRoomData(session);

        JsonNode json = mapper.readTree(message.getPayload());
        String type = json.get("type").asText();

        switch (type) {
            case "offer" -> handleOffer(sender, room, json);
            case "answer" -> handleAnswer(sender, room, json);
            case "ice-candidate" -> handleIceCandidate(sender, room, json);
            default -> log.warn("Unknown message type: {}", type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        removeSession(session);
    }

    private void handleOffer(User sender, Room room, JsonNode json) {
        SignalMessage offer = new SignalMessage(
                "offer",
                sender.getId(),
                sender.getUsername(),
                UUID.fromString(json.get("targetId").asText()),
                json.get("data")
        );
        sendToUser(room, offer.targetId(), offer);
    }

    private void handleAnswer(User sender, Room room, JsonNode json) {
        SignalMessage answer = new SignalMessage(
                "answer",
                sender.getId(),
                sender.getUsername(),
                UUID.fromString(json.get("targetId").asText()),
                json.get("data")
        );
        sendToUser(room, answer.targetId(), answer);
    }

    private void handleIceCandidate(User sender, Room room, JsonNode json) {
        SignalMessage ice = new SignalMessage(
                "ice-candidate",
                sender.getId(),
                sender.getUsername(),
                UUID.fromString(json.get("targetId").asText()),
                json.get("data")
        );
        sendToUser(room, ice.targetId(), ice);
    }

    private void sendToUser(Room room, UUID targetId, SignalMessage message) {
        getSession(room, targetId).ifPresent(session -> {
            synchronized (session) {
                if (!session.isOpen()) {
                    removeSession(session);
                    return;
                }
                try {
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                } catch (IOException e) {
                    removeSession(session);
                }
            }
        });
    }

    private void broadcastUserList(Room room) {
        List<UserDto> users = getRoomSessions(room).stream()
                .map(this::getUserData)
                .map(user -> new UserDto(user.getId(), user.getUsername()))
                .toList();

        BroadcastMessage userList = new BroadcastMessage("user-list", users);
        broadcast(room, userList);
    }

    private void broadcast(Room room, Object message) {
        getRoomSessions(room).forEach(session -> {
            synchronized (session) {
                if (!session.isOpen()) {
                    removeSession(session);
                    return;
                }
                try {
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                } catch (IOException e) {
                    removeSession(session);
                }
            }
        });
    }

    private Set<WebSocketSession> getRoomSessions(Room room) {
        return rooms.getOrDefault(room.getId(), Collections.emptySet());
    }

    private Optional<WebSocketSession> getSession(Room room, UUID userId) {
        return getRoomSessions(room).stream()
                .filter(s -> getUserData(s).getId().equals(userId))
                .findFirst();
    }

    private void removeSession(WebSocketSession session) {
        try {
            User user = getUserData(session);
            Room room = getRoomData(session);

            Set<WebSocketSession> roomSessions = rooms.get(room.getId());
            if (roomSessions != null) {
                roomSessions.remove(session);
                if (roomSessions.isEmpty()) {
                    rooms.remove(room.getId());
                }
                broadcastUserList(room);
            }
        } catch (Exception e) {
            log.error("Error removing session", e);
        }
    }

    public record SignalMessage(
            String type,
            UUID senderId,
            String senderName,
            UUID targetId,
            Object data
    ) {
    }

    public record BroadcastMessage(String type, Object data) {
    }
}

