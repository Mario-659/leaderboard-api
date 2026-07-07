package com.damian.leaderboardapi.service.ratelimiter.nonatomic;

import com.damian.leaderboardapi.dto.RateLimitAttemptDto;
import com.damian.leaderboardapi.service.ratelimiter.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@ConditionalOnProperty(name = "rate-limiter.strategy", havingValue = "sliding-window-log-non-atomic")
public class SlidingWindowLogRateLimiterNonAtomicService extends RateLimiter {
    private static final String RATE_LIMITER_KEY = "FixedWindowRateLimiterKey-";

    private final RedisTemplate<String, String> redisTemplate;

    public SlidingWindowLogRateLimiterNonAtomicService(
            @Value("${rate-limiter.window-duration-seconds}") Long windowDurationSeconds,
            @Value("${rate-limiter.max-requests-per-window}") Long maxRequestsPerWindow,
            RedisTemplate<String, String> redisTemplate
    ) {
        super(windowDurationSeconds, maxRequestsPerWindow);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitAttemptDto attempt(String origin) {
        String key = RATE_LIMITER_KEY + origin;

        Long now = System.currentTimeMillis();
        Long bottomBoundary = now - (windowDurationsSeconds * 1000);
        String member = now + ":" + Math.random();

        zSetOps().removeRangeByScore(key, 0, bottomBoundary);

        Long size = zSetOps().size(key);

        if (size >= maxRequestsPerWindow) {
            return new RateLimitAttemptDto(false, getRetryAfterSeconds(key, now / 1000, windowDurationsSeconds));
        }

        zSetOps().add(key, member, now);

        return new RateLimitAttemptDto(true, 0L);
    }

    private Long getRetryAfterSeconds(String key, long nowSec, long windowSec) {
        Set<ZSetOperations.TypedTuple<String>> oldest =
                zSetOps().rangeWithScores(key, 0, 0);
        if (oldest == null || oldest.isEmpty()) {
            return 0L;
        }
        double oldestScore = oldest.iterator().next().getScore();
        long retryAt = (long) (oldestScore + windowSec * 1000) / 1000;
        return Math.max(0L, retryAt - nowSec);
    }

    private ZSetOperations<String, String> zSetOps() {
        return redisTemplate.opsForZSet();
    }
}
