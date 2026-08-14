package com.ahmadisyraf39.sportsbooking.user_service.controller;

import com.ahmadisyraf39.sportsbooking.user_service.dto.request.LoginRequest;
import com.ahmadisyraf39.sportsbooking.user_service.dto.request.RegisterRequest;
import com.ahmadisyraf39.sportsbooking.user_service.dto.response.AuthResponse;
import com.ahmadisyraf39.sportsbooking.user_service.dto.response.UserResponse;
import com.ahmadisyraf39.sportsbooking.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}