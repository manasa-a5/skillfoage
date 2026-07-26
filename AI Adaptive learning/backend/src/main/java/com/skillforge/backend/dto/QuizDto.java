package com.skillforge.backend.dto;

import com.skillforge.backend.model.Difficulty;

import java.time.LocalDateTime;
import java.util.List;

public record QuizDto(
        Long id,
        String title,
        String topic,
        Difficulty difficulty,
        String createdBy,
        LocalDateTime createdAt,
        List<QuizQuestionDto> questions
) {
}
