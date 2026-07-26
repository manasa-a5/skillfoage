package com.skillforge.backend.dto;

import java.util.List;
import java.util.Map;

public record QuizGenerationResponse(
        String title,
        String topic,
        String difficulty,
        int questionCount,
        List<Map<String, String>> questions
) {
}
