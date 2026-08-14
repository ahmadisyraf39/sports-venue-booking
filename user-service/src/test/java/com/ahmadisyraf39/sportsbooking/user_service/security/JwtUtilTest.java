package com.ahmadisyraf39.sportsbooking.user_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-must-be-long-enough-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void shouldGenerateValidToken() {
        String token = jwtUtil.generateToken("test@example.com");

        assertThat(token).isNotBlank();
    }

    @Test
    void shouldExtractCorrectEmailFromToken() {
        String token = jwtUtil.generateToken("test@example.com");

        String extractedEmail = jwtUtil.extractEmail(token);

        assertThat(extractedEmail).isEqualTo("test@example.com");
    }

    @Test
    void shouldValidateTokenForCorrectEmail() {
        String token = jwtUtil.generateToken("test@example.com");

        boolean isValid = jwtUtil.isTokenValid(token, "test@example.com");

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectTokenForWrongEmail() {
        String token = jwtUtil.generateToken("test@example.com");

        boolean isValid = jwtUtil.isTokenValid(token, "wrong@example.com");

        assertThat(isValid).isFalse();
    }
}
