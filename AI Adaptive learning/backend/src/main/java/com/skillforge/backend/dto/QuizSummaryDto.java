package com.skillforge.backend.dto;

public record QuizSummaryDto(
        Long id,
        Long courseId,
        String title,
        boolean generatedByAI
) {
}
