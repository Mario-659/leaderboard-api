package com.damian.leaderboardapi.service;

public interface RateLimiter {
    boolean isAllowed(String origin);
}
