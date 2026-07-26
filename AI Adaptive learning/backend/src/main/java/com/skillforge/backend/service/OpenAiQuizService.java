package com.skillforge.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillforge.backend.dto.GenerateQuizRequest;
import com.skillforge.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiQuizService {

    private final ObjectMapper objectMapper;

    @Value("${app.openai.api-key:}")
    private String apiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String model;

    public List<Map<String, String>> generateQuestions(GenerateQuizRequest request) {
        return generateQuestionsFromTopic(request.topic(), request.difficulty().name(), request.questionCount());
    }

    public List<Map<String, String>> generateQuestionsFromTopic(String topic, String difficultyLevel, int questionCount) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API key is missing. Using local fallback question generator for topic: {}", topic);
            return fallbackQuestions(topic, questionCount);
        }

        String systemPrompt = "You generate quiz questions in strict JSON only.";
        String userPrompt = "Create " + questionCount + " multiple-choice questions for topic: " + topic +
                ", difficulty: " + difficultyLevel +
                ". Return only a JSON array where each object has keys: prompt, optionA, optionB, optionC, optionD, correctOption. " +
                "correctOption must be one of A,B,C,D.";

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.4);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            String normalized = content.trim();
            if (normalized.startsWith("```") ) {
                normalized = normalized.replaceAll("^```json", "").replaceAll("^```", "");
                normalized = normalized.replaceAll("```$", "").trim();
            }

            return objectMapper.readValue(normalized, new TypeReference<>() {
            });
        } catch (Exception ex) {
                log.warn("OpenAI response parsing failed. Using fallback generator. Cause: {}", ex.getMessage());
            return fallbackQuestions(topic, questionCount);
        }
    }

    private List<Map<String, String>> fallbackQuestions(String topic, int questionCount) {
            String cleanTopic = (topic == null || topic.isBlank()) ? "General Knowledge" : topic.trim();

            List<Map<String, String>> bank = new ArrayList<>();
            String lower = cleanTopic.toLowerCase();

            if (lower.contains("join") || lower.contains("sql") || lower.contains("database")) {
                bank.add(mcq(
                    "In SQL, which JOIN returns only matching rows from both tables?",
                    "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN", "A"
                ));
                bank.add(mcq(
                    "Which JOIN keeps all rows from the left table even when there is no match?",
                    "INNER JOIN", "LEFT JOIN", "CROSS JOIN", "SELF JOIN", "B"
                ));
                bank.add(mcq(
                    "Which clause is commonly used with JOIN conditions?",
                    "ON", "ORDER BY", "GROUP BY", "HAVING", "A"
                ));
                bank.add(mcq(
                    "What does a CROSS JOIN produce?",
                    "Only matched records", "Cartesian product", "Distinct values only", "Aggregated rows", "B"
                ));
                bank.add(mcq(
                    "If no related row exists in the right table, LEFT JOIN returns:",
                    "An error", "The left row with NULLs on right columns", "No row at all", "Only right rows", "B"
                ));
                bank.add(mcq(
                    "Which JOIN is often used to find unmatched rows between tables?",
                    "INNER JOIN", "LEFT JOIN with IS NULL filter", "CROSS JOIN", "NATURAL JOIN", "B"
                ));
            } else {
                bank.add(mcq(
                    "Which statement best describes " + cleanTopic + "?",
                    cleanTopic + " involves core principles and practical application",
                    cleanTopic + " is unrelated to problem solving",
                    cleanTopic + " is only theoretical and never used",
                    cleanTopic + " cannot be learned progressively",
                    "A"
                ));
                bank.add(mcq(
                    "What is a good first step when learning " + cleanTopic + "?",
                    "Ignore fundamentals",
                    "Memorize random facts without practice",
                    "Understand basics and solve small examples",
                    "Skip directly to advanced edge cases",
                    "C"
                ));
                bank.add(mcq(
                    "Which approach improves mastery in " + cleanTopic + "?",
                    "Consistent practice with feedback",
                    "Avoid reviewing mistakes",
                    "Use only one example repeatedly",
                    "Never test understanding",
                    "A"
                ));
                bank.add(mcq(
                    "When solving a " + cleanTopic + " problem, what helps most?",
                    "Clarifying inputs and expected output",
                    "Guessing without reading requirements",
                    "Ignoring constraints",
                    "Skipping validation",
                    "A"
                ));
                bank.add(mcq(
                    "What is a common mistake in " + cleanTopic + " learning?",
                    "Practicing progressively",
                    "Documenting key insights",
                    "Rushing without understanding fundamentals",
                    "Asking clarifying questions",
                    "C"
                ));
                bank.add(mcq(
                    "Which habit supports long-term retention of " + cleanTopic + "?",
                    "Spaced revision and applied exercises",
                    "Last-minute cramming only",
                    "Avoiding real examples",
                    "Never revisiting old topics",
                    "A"
                ));
            }

            List<Map<String, String>> out = new ArrayList<>();
            for (int i = 0; i < questionCount; i++) {
                Map<String, String> base = bank.get(i % bank.size());
                out.add(Map.of(
                    "prompt", base.get("prompt"),
                    "optionA", base.get("optionA"),
                    "optionB", base.get("optionB"),
                    "optionC", base.get("optionC"),
                    "optionD", base.get("optionD"),
                    "correctOption", base.get("correctOption")
                ));
            }

            return out;
    }

            private Map<String, String> mcq(String prompt,
                            String optionA,
                            String optionB,
                            String optionC,
                            String optionD,
                            String correctOption) {
            return Map.of(
                "prompt", prompt,
                "optionA", optionA,
                "optionB", optionB,
                "optionC", optionC,
                "optionD", optionD,
                "correctOption", correctOption
            );
            }
}
