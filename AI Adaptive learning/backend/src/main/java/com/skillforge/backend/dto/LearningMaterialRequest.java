package com.skillforge.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LearningMaterialRequest(
        Long courseId,
        @NotBlank String subject,
        @NotBlank String title,
        String description,
        String youtubeUrl,
        String pdfUrl
) {
}
