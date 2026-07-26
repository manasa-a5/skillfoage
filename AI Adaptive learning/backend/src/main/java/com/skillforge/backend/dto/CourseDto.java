package com.skillforge.backend.dto;

public record CourseDto(
        Long id,
        String title,
        Long instructorId,
        String difficultyLevel
) {
}
