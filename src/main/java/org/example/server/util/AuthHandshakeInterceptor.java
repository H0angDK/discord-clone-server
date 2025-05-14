package org.example.server.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.service.RoomService;
import org.example.server.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final UserService userService;
    private final RoomService roomService;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {


        var map = extractQuery(request);

        if (!validateRequiredParams(map, "userId", "roomId")) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        var userId = UUID.fromString(map.get("userId"));
        var roomId = UUID.fromString(map.get("roomId"));

        var user = userService.findById(userId);
        var room = roomService.getRoomById(roomId);

        if (user.isPresent() && room != null
//                && room.containsUser(user.get())
        ) {
            attributes.put("user", user.get());
            attributes.put("room", room);
            return true;
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }

    private boolean validateRequiredParams(Map<String, String> params, String... keys) {
        return Arrays.stream(keys).allMatch(key -> params.containsKey(key) && !params.get(key).isEmpty());
    }

    private Map<String, String> extractQuery(ServerHttpRequest request) {
        Map<String, String> queryMap = new HashMap<>();
        String query = request.getURI().getQuery();

        if (query == null || query.isEmpty()) {
            return queryMap;
        }

        Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .filter(pair -> pair.length == 2)
                .forEach(pair -> queryMap.put(pair[0], pair[1]));

        return queryMap;
    }
}
