package com.damian.leaderboardapi.controller;

import com.damian.leaderboardapi.dto.UpdateScoreDto;
import com.damian.leaderboardapi.service.LeaderboardService;
import com.damian.leaderboardapi.service.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final RateLimiter rateLimiterService;

    @PostMapping("/score")
    public ResponseEntity<String> updateScore(@RequestBody UpdateScoreDto updateScoreDto) {
        if (!rateLimiterService.isAllowed(updateScoreDto.userId())) {
            return ResponseEntity.status(429).build();
        }

        leaderboardService.addScore(updateScoreDto.userId(), updateScoreDto.score());
        return ResponseEntity.ok("updated");
    }

    @GetMapping("/leaderboard")
    public Set<ZSetOperations.TypedTuple<String>> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }
}
