package com.damian.leaderboardapi.service.ratelimiter;

import com.damian.leaderboardapi.dto.RateLimitAttemptDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "rate-limiter.strategy", havingValue = "fixed-window")
public class FixedWindowRateLimiterService extends RateLimiter {
    private static final String RATE_LIMITER_KEY = "FixedWindowRateLimiterKey-";

    private final RedisTemplate<String, String> redisTemplate;

    public FixedWindowRateLimiterService(
            @Value("${rate-limiter.window-duration-seconds}") Long windowDurationSeconds,
            @Value("${rate-limiter.max-requests-per-window}") Long maxRequestsPerWindow,
            RedisTemplate<String, String> redisTemplate
    ) {
        super(windowDurationSeconds, maxRequestsPerWindow);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitAttemptDto attempt(String origin) {
        String userKey = RATE_LIMITER_KEY + origin;

        Long value = valueOps().increment(userKey);

        if (value == 1) {
            valueOps().getAndExpire(userKey, Duration.ofSeconds(windowDurationsSeconds));
        }

        Long pttl = redisTemplate.getExpire(userKey, TimeUnit.SECONDS);

        return new RateLimitAttemptDto(value <= maxRequestsPerWindow, pttl);
    }

    private ValueOperations<String, String> valueOps() {
        return redisTemplate.opsForValue();
    }
}
