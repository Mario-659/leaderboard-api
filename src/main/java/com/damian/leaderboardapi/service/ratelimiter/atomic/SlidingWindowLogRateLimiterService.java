package com.damian.leaderboardapi.service.ratelimiter.atomic;

import com.damian.leaderboardapi.dto.RateLimitAttemptDto;
import com.damian.leaderboardapi.service.ratelimiter.RateLimiter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "rate-limiter.strategy", havingValue = "sliding-window-log")
public class SlidingWindowLogRateLimiterService extends RateLimiter {
    private static final String RATE_LIMITER_KEY = "SlidingWindowLogRL-";

    private final RedisScript<List> redisScript;
    private final StringRedisTemplate redisTemplate;

    public SlidingWindowLogRateLimiterService(
            @Value("${rate-limiter.window-duration-seconds}") Long windowDurationSeconds,
            @Value("${rate-limiter.max-requests-per-window}") Long maxRequestsPerWindow,
            RedisScript<List> redisScript,
            StringRedisTemplate redisTemplate
    ) {
        super(windowDurationSeconds, maxRequestsPerWindow);
        this.redisScript = redisScript;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitAttemptDto attempt(String origin) {
        String userKey = RATE_LIMITER_KEY + origin;

        Long now = System.currentTimeMillis();
        String member = now + ":" + UUID.randomUUID();

        List<Long> result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(userKey),
                String.valueOf(maxRequestsPerWindow),
                String.valueOf(windowDurationsSeconds),
                String.valueOf(now),
                member
        );

        boolean isAllowed = result.get(0) == 1;
        Long retryAfter = result.get(2) / 1000;

        return new RateLimitAttemptDto(isAllowed, retryAfter);
    }
}
