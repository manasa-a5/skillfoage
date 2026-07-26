package com.skillforge.backend.dto;

import java.time.LocalDateTime;

public record QuizAttemptResponse(
        Long id,
        Long quizId,
        Long studentId,
        int score,
        LocalDateTime attemptTime,
        String level
) {
}
