package com.damian.leaderboardapi.service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeaderboardService {
    private static final String COLLECTION_KEY = "leaderboard";

    private final RedisTemplate<String, String> redisTemplate;

    private ZSetOperations<String, String> zSetOps() {
        return redisTemplate.opsForZSet();
    }

    public void addScore(String user, Double score){
        zSetOps().add(COLLECTION_KEY, user, score);
    }

    public Set<ZSetOperations.TypedTuple<String>> getLeaderboard() {
        return zSetOps().rangeWithScores(COLLECTION_KEY, 0, -1);
    }
}
