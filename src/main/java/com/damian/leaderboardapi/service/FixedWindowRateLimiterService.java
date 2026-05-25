package com.damian.leaderboardapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FixedWindowRateLimiterService implements RateLimiter{
    private static final String RATE_LIMITER_KEY = "FixedWindowRateLimiterKey-";
    private static final Long WINDOW_DURATION_SECONDS = 60L;
    private static final Long MAX_REQUESTS_PER_WINDOW = 10L;

    private final RedisTemplate<String, String> redisTemplate;

    private ValueOperations<String, String> valueOps() {
        return redisTemplate.opsForValue();
    }

    @Override
    public boolean isAllowed(String origin) {
        String userKey = RATE_LIMITER_KEY + origin;

        Long value = valueOps().increment(userKey);

        if (value == 1) {
            valueOps().getAndExpire(userKey, Duration.ofSeconds(WINDOW_DURATION_SECONDS));
        }

        return value <= MAX_REQUESTS_PER_WINDOW;
    }
}
