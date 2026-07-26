package com.skillforge.backend.dto;

import java.time.LocalDateTime;

public record LearningMaterialDto(
        Long id,
        Long courseId,
        String courseTitle,
        String subject,
        String title,
        String description,
        String youtubeUrl,
        String pdfUrl,
        String instructorName,
        LocalDateTime createdAt
) {
}
