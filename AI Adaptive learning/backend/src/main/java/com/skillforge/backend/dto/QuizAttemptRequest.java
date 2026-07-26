package com.skillforge.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record QuizAttemptRequest(
        @NotNull Long quizId,
        @NotEmpty Map<Long, String> answers
) {
}
