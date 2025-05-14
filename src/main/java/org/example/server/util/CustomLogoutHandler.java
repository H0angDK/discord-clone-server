package org.example.server.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.server.service.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenService tokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String header = request.getHeader("Authorization");
        String token;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring("Bearer ".length());
            tokenService.getTokenByValue(token).ifPresent(value -> tokenService.revokeAllUserTokens(value.getUser()));
        }
    }
}
