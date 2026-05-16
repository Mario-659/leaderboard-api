package com.damian.leaderboardapi.controller;

import com.damian.leaderboardapi.dto.UpdateScoreDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Score {

    @PostMapping("/score")
    public String updateScore(UpdateScoreDto updateScoreDto) {
        return "updated";
    }
}
