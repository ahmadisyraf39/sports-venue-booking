package com.ahmadisyraf39.sportsbooking.user_service.service;

import com.ahmadisyraf39.sportsbooking.user_service.dto.request.LoginRequest;
import com.ahmadisyraf39.sportsbooking.user_service.dto.request.RegisterRequest;
import com.ahmadisyraf39.sportsbooking.user_service.dto.response.UserResponse;
import com.ahmadisyraf39.sportsbooking.user_service.entity.Role;
import com.ahmadisyraf39.sportsbooking.user_service.entity.User;
import com.ahmadisyraf39.sportsbooking.user_service.exception.EmailAlreadyExistsException;
import com.ahmadisyraf39.sportsbooking.user_service.exception.InvalidCredentialsException;
import com.ahmadisyraf39.sportsbooking.user_service.repository.UserRepository;
import com.ahmadisyraf39.sportsbooking.user_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return jwtUtil.generateToken(user.getEmail());
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
