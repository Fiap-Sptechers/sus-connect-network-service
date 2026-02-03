package com.fiap.sus.network.modules.user.controller;

import com.fiap.sus.network.modules.user.dto.TokenResponse;
import com.fiap.sus.network.modules.user.dto.LoginRequest;
import com.fiap.sus.network.modules.user.dto.UserRequest;
import com.fiap.sus.network.modules.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
       return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh() {
        return ResponseEntity.ok(authService.refresh());
    }

    @PostMapping("/users")
    public ResponseEntity<Void> create(@RequestBody UserRequest request) {
        authService.createUser(request);
        return ResponseEntity.status(201).build();
    }
}
