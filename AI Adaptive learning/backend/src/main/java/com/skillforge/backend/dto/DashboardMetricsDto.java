package com.skillforge.backend.dto;

public record DashboardMetricsDto(
        long totalStudents,
        long totalQuizzes,
        long totalAttempts,
        double averageScorePercent
) {
}
