package com.skillforge.backend.dto;

public record QuizQuestionDto(
        Long id,
        String prompt,
        String optionA,
        String optionB,
        String optionC,
        String optionD
) {
}
