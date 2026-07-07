package com.damian.leaderboardapi.dto;

public record RateLimitAttemptDto(boolean isAllowed, Long retryAfter) {
}
