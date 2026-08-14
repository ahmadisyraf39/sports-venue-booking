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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    class Register {

        @Test
        void shouldRegisterUser_WhenEmailIsUnique() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("new@example.com");
            request.setPassword("password123");
            request.setName("New User");
            request.setPhone("0123456789");

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            UserResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("new@example.com");
            assertThat(response.getRole()).isEqualTo(Role.USER);
        }

        @Test
        void shouldThrowException_WhenEmailAlreadyExists() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("existing@example.com");

            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    class Login {

        @Test
        void shouldReturnToken_WhenCredentialsAreValid() {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("password123");

            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword("hashed-password");

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
            when(jwtUtil.generateToken("user@example.com")).thenReturn("fake-jwt-token");

            String token = authService.login(request);

            assertThat(token).isEqualTo("fake-jwt-token");
        }

        @Test
        void shouldThrowException_WhenEmailNotFound() {
            LoginRequest request = new LoginRequest();
            request.setEmail("notfound@example.com");
            request.setPassword("password123");

            when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        void shouldThrowException_WhenPasswordIsWrong() {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("wrongpassword");

            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword("hashed-password");

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpassword", "hashed-password")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }
}
