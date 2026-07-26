package com.skillforge.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateQuizAiRequest(
        @NotNull Long courseId,
        @NotBlank String topic,
        @NotBlank String title,
        @Min(3) @Max(20) int questionCount
) {
}
