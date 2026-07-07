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
import java.util.List;

@Service
@ConditionalOnProperty(name = "rate-limiter.strategy", havingValue = "fixed-window")
@Log4j2
public class FixedWindowRateLimiterService extends RateLimiter {
    private static final String RATE_LIMITER_KEY = "FixedWindowRateLimiterKey-";

    private final RedisScript<List> redisScript;
    private final StringRedisTemplate redisTemplate;

    public FixedWindowRateLimiterService(
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

        List<Long> result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(userKey),
                String.valueOf(windowDurationsSeconds)
        );

        boolean isAllowed = result.get(0) < maxRequestsPerWindow;
        Long retryAfter = result.get(1);

        log.info("Count: {}", result.get(0));

        return new RateLimitAttemptDto(isAllowed, retryAfter);
    }

}
