package org.example.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.AuthDto;
import org.example.server.model.Token;
import org.example.server.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthDto.Response signUp(AuthDto.SignUpRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("User already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        var savedUser = userService.save(user);
        var refreshToken = tokenService.generateRefreshToken(savedUser);
        var accessToken = tokenService.generateAccessToken(savedUser);
        return AuthDto
                .Response
                .builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .expiresIn(System.currentTimeMillis() + 1000 * 60 * 60 * 24)
                .build();
    }

    public AuthDto.Response signIn(AuthDto.SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userService.findByUsername(request.getUsername()).orElseThrow();
        tokenService.revokeAllUserTokens(user);
        var refreshToken = tokenService.generateRefreshToken(user);
        var accessToken = tokenService.generateAccessToken(user);
        return AuthDto
                .Response
                .builder()
                .refreshToken(refreshToken.getToken())
                .accessToken(accessToken.getToken())
                .userId(user.getId())
                .username(user.getUsername())
                .expiresIn(System.currentTimeMillis() + 1000 * 60 * 60 * 24)
                .build();

    }

    public AuthDto.Response refresh(AuthDto.RefreshRequest request) {
        var token = tokenService.getTokenByValue(request.getRefreshToken());
        if (token.isEmpty() || token.get().isRevoked()) {
            throw new RuntimeException("Invalid refresh token");
        }

        if (!tokenService.isTokenValid(request.getRefreshToken())) {
            throw new RuntimeException("Token expired");
        }

        if (!token.get().getTokenType().equals(Token.TokenType.REFRESH)) {
            throw new RuntimeException("Token is not a refresh token");
        }

        tokenService.revokeAllUserTokens(token.get().getUser());
        var user = token.get().getUser();
        var newAccessToken = tokenService.generateAccessToken(user);
        var newRefreshToken = tokenService.generateRefreshToken(user);
        return AuthDto
                .Response
                .builder()
                .refreshToken(newRefreshToken.getToken())
                .accessToken(newAccessToken.getToken())
                .expiresIn(System.currentTimeMillis() + 1000 * 60 * 60 * 24)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}
