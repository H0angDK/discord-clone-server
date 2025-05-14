package org.example.server.controller;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.AuthDto;
import org.example.server.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<AuthDto.Response> signIn(@RequestBody AuthDto.SignInRequest request) {
        return ResponseEntity.ok(authService.signIn(request));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthDto.Response> signUp(@RequestBody AuthDto.SignUpRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDto.Response> refresh(@RequestBody AuthDto.RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
