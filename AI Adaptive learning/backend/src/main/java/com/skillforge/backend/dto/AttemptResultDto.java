package com.skillforge.backend.dto;

import java.time.LocalDateTime;

public record AttemptResultDto(
        Long attemptId,
        int score,
        int total,
        double percentage,
        LocalDateTime attemptedAt,
        String nextRecommendedDifficulty
) {
}
