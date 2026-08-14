package com.ahmadisyraf39.sportsbooking.user_service.controller;

import com.ahmadisyraf39.sportsbooking.user_service.config.SecurityConfig;
import com.ahmadisyraf39.sportsbooking.user_service.dto.request.LoginRequest;
import com.ahmadisyraf39.sportsbooking.user_service.dto.request.RegisterRequest;
import com.ahmadisyraf39.sportsbooking.user_service.dto.response.AuthResponse;
import com.ahmadisyraf39.sportsbooking.user_service.dto.response.UserResponse;
import com.ahmadisyraf39.sportsbooking.user_service.entity.Role;
import com.ahmadisyraf39.sportsbooking.user_service.exception.EmailAlreadyExistsException;
import com.ahmadisyraf39.sportsbooking.user_service.exception.InvalidCredentialsException;
import com.ahmadisyraf39.sportsbooking.user_service.security.CustomUserDetailsService;
import com.ahmadisyraf39.sportsbooking.user_service.security.JwtUtil;
import com.ahmadisyraf39.sportsbooking.user_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturn201_WhenRegisterIsSuccessful() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("0123456789");

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .phone("0123456789")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldReturn409_WhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setName("Test User");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already registered: existing@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400_WhenRegisterRequestIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("not-an-email");
        request.setPassword("123");
        request.setName("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200_WhenLoginIsSuccessful() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authService.login(any(LoginRequest.class))).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void shouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}