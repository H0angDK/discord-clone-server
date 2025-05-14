package org.example.server.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

public class AuthDto {
    @Data
    @Builder
    public static class SignInRequest {
        private String username;
        private String password;
    }

    @Data
    @Builder
    public static class SignUpRequest {
        private String username;
        private String password;
        private String confirmPassword;
    }

    @Data
    @Builder
    public static class Response {
        private String accessToken;
        private String refreshToken;
        private UUID userId;
        private String username;
        private long expiresIn;
    }

    @Data
    public static class RefreshRequest {
        private String refreshToken;
    }
}