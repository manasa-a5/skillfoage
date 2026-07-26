package com.skillforge.backend.dto;

import java.util.Map;

public record QuestionDto(
        Long id,
        Long quizId,
        String questionText,
        Map<String, String> options,
        String correctAnswer
) {
}
