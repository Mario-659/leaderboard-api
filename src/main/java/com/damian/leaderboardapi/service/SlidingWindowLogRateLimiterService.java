package com.damian.leaderboardapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "rate-limiter.strategy", havingValue = "sliding-window-log")
public class SlidingWindowLogRateLimiterService extends RateLimiter {
    private static final String RATE_LIMITER_KEY = "FixedWindowRateLimiterKey-";

    private final RedisTemplate<String, String> redisTemplate;

    public SlidingWindowLogRateLimiterService(
            @Value("${rate-limiter.window-duration-seconds}") Long windowDurationSeconds,
            @Value("${rate-limiter.max-requests-per-window}") Long maxRequestsPerWindow,
            RedisTemplate<String, String> redisTemplate
    ) {
        super(windowDurationSeconds, maxRequestsPerWindow);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(String origin) {
        String key = RATE_LIMITER_KEY + origin;

        Long now = System.currentTimeMillis();
        Long bottomBoundary = now - (windowDurationsSeconds * 1000);
        String member = now + ":" + Math.random();

        zSetOps().removeRangeByScore(key, 0, bottomBoundary);

        Long size = zSetOps().size(key);

        if (size > maxRequestsPerWindow) {
            return false;
        }

        zSetOps().add(key, member, now);

        return true;
    }

    private ZSetOperations<String, String> zSetOps() {
        return redisTemplate.opsForZSet();
    }
}
