package org.example.server.config;

import lombok.RequiredArgsConstructor;
import org.example.server.controller.CallingHandler;
import org.example.server.controller.ChatHandler;
import org.example.server.util.AuthHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatHandler chatHandler;
    private final CallingHandler callingHandler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/chat")
                .setAllowedOrigins("*")
                .addInterceptors(authHandshakeInterceptor);

        registry.addHandler(callingHandler, "/call")
                .setAllowedOrigins("*")
                .addInterceptors(authHandshakeInterceptor);
    }
}
