package com.ahmadisyraf39.sportsbooking.booking_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class BookingLockService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10); // Lock expiration time


    public boolean acquireLock(Long courtId, LocalDate bookingDate, LocalTime startTime) {
        String lockKey = buildLockKey(courtId, bookingDate, startTime);
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", LOCK_TIMEOUT);
        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(Long courtId, LocalDate bookingDate, LocalTime startTime) {
        String lockKey = buildLockKey(courtId, bookingDate, startTime);
        redisTemplate.delete(lockKey);
    }

    private String buildLockKey(Long courtId, LocalDate bookingDate, LocalTime startTime) {
        return "lock:booking:%d:%s:%s".formatted(courtId, bookingDate, startTime);
    }
}
