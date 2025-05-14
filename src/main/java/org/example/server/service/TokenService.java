package org.example.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.model.Token;
import org.example.server.model.User;
import org.example.server.repository.TokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    public Token generateAccessToken(User user) {
        var token = jwtService.generateAccessToken(user);
        return saveToken(user, token, Token.TokenType.ACCESS);
    }

    public Token generateRefreshToken(User user) {
        var token = jwtService.generateRefreshToken(user);
        return saveToken(user, token, Token.TokenType.REFRESH);
    }

    public Token saveToken(User user, String jwt, Token.TokenType tokenType) {
        var token = Token.builder()
                .token(jwt)
                .user(user)
                .isRevoked(false)
                .tokenType(tokenType)
                .build();
        return tokenRepository.save(token);
    }

    public void revokeToken(String token) {
        getTokenByValue(token)
                .ifPresent(t -> {
                    t.setRevoked(true);
                    tokenRepository.save(t);
                });
    }

    public void revokeAllUserTokens(User user) {
        var tokens = tokenRepository.findAllByUser_Id(user.getId());
        if (tokens.isEmpty()) {
            return;
        }

        tokens.forEach(token -> revokeToken(token.getToken()));
    }


    public boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isRevoked())
                .orElse(false);
    }

    public Optional<Token> getTokenByValue(String token) {
        return tokenRepository.findByToken(token);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredTokens() {
        tokenRepository.findAll().forEach(t -> {
            if (jwtService.isTokenExpired(t.getToken()) || !t.isRevoked()) {
                tokenRepository.delete(t);
            }
        });
    }
}
