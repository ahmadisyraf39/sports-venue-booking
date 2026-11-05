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

    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);    // safety net: auto-expire
    private static final Duration LOCK_WAIT = Duration.ofSeconds(2);        // how long to keep trying
    private static final Duration RETRY_INTERVAL = Duration.ofMillis(50);

    public boolean acquireLock(Long courtId, LocalDate bookingDate) {
        String lockKey = buildLockKey(courtId, bookingDate);
        long deadline = System.nanoTime() + LOCK_WAIT.toNanos();

        do {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", LOCK_TIMEOUT);
            if (Boolean.TRUE.equals(acquired)) {
                return true;
            }
            try {
                Thread.sleep(RETRY_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        } while (System.nanoTime() < deadline);

        return false;
    }

    public void releaseLock(Long courtId, LocalDate bookingDate) {
        redisTemplate.delete(buildLockKey(courtId, bookingDate));
    }

    // One lock per court per day: overlap checks for the same court/day never run in parallel
    private String buildLockKey(Long courtId, LocalDate bookingDate) {
        return "lock:booking:%d:%s".formatted(courtId, bookingDate);
    }
}
