package com.skillforge.backend.dto;

import com.skillforge.backend.model.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateQuizRequest(
        @NotBlank String title,
        @NotBlank String topic,
        @NotNull Difficulty difficulty,
        @Min(3) @Max(20) int questionCount
) {
}
