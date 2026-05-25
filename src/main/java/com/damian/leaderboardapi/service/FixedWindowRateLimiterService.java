package com.damian.leaderboardapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class FixedWindowRateLimiterService implements RateLimiter{
    private static final String RATE_LIMITER_KEY = "FixedWindowRateLimiterKey-";

    private final Long windowDurationsSeconds;
    private final Long maxRequestsPerWindow;
    private final RedisTemplate<String, String> redisTemplate;

    public FixedWindowRateLimiterService(
            @Value("${rate-limiter.window-duration-seconds}") Long windowDurationSeconds,
            @Value("${rate-limiter.max-requests-per-window}") Long maxRequestsPerWindow,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.windowDurationsSeconds = windowDurationSeconds;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(String origin) {
        String userKey = RATE_LIMITER_KEY + origin;

        Long value = valueOps().increment(userKey);

        if (value == 1) {
            valueOps().getAndExpire(userKey, Duration.ofSeconds(windowDurationsSeconds));
        }

        return value <= maxRequestsPerWindow;
    }

    private ValueOperations<String, String> valueOps() {
        return redisTemplate.opsForValue();
    }
}
