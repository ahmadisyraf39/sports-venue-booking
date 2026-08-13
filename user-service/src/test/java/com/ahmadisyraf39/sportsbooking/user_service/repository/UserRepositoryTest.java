package com.ahmadisyraf39.sportsbooking.user_service.repository;

import com.ahmadisyraf39.sportsbooking.user_service.entity.Role;
import com.ahmadisyraf39.sportsbooking.user_service.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndRetrieveUserByEmail() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedpassword");
        user.setName("Test User");
        user.setRole(Role.USER);

        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
        assertThat(found.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("doesnotexist@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPassword("hashedpassword");
        user.setName("Another User");
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exists@example.com");

        assertThat(exists).isTrue();
    }
}