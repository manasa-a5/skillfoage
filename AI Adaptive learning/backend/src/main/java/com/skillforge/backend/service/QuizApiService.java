package com.skillforge.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillforge.backend.dto.GenerateQuizAiRequest;
import com.skillforge.backend.dto.QuestionDto;
import com.skillforge.backend.dto.QuizGenerationRequest;
import com.skillforge.backend.dto.QuizGenerationResponse;
import com.skillforge.backend.dto.QuizSummaryDto;
import com.skillforge.backend.exception.BadRequestException;
import com.skillforge.backend.exception.ResourceNotFoundException;
import com.skillforge.backend.model.*;
import com.skillforge.backend.repository.QuestionRepository;
import com.skillforge.backend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizApiService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OpenAiQuizService openAiQuizService;
    private final CourseService courseService;
    private final ObjectMapper objectMapper;

        public QuizGenerationResponse generateForInstructor(QuizGenerationRequest request, AppUser instructor) {
        String normalizedDifficulty = request.difficulty() == null ? "MEDIUM" : request.difficulty().trim().toUpperCase();
        log.info("Generating questions for instructorId={}, title={}, topic={}, difficulty={}, questionCount={}",
            instructor.getId(), request.title(), request.topic(), normalizedDifficulty, request.questionCount());

        List<Map<String, String>> generated = openAiQuizService.generateQuestionsFromTopic(
            request.topic(),
            normalizedDifficulty,
            request.questionCount()
        );

        if (generated == null || generated.isEmpty()) {
            throw new BadRequestException("Question generation failed: no questions produced for topic.");
        }

        return new QuizGenerationResponse(
            request.title(),
            request.topic(),
            normalizedDifficulty,
            request.questionCount(),
            generated
        );
        }

    @Transactional
    public QuizSummaryDto generate(GenerateQuizAiRequest request, AppUser instructor) {
        Course course = courseService.getById(request.courseId());
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new BadRequestException("Course does not belong to instructor.");
        }

        // STEP 1: Generate questions FIRST (before any database writes)
        List<Map<String, String>> generated = openAiQuizService.generateQuestionsFromTopic(
                request.topic(),
                course.getDifficultyLevel(),
                request.questionCount()
        );

        // STEP 2: Validate generation succeeded before touching database
        if (generated == null || generated.isEmpty()) {
            throw new BadRequestException("Question generation failed: no questions produced for topic.");
        }

        // STEP 3: Build and validate all question objects (still no DB writes)
        List<Question> questionsToSave = generated.stream().map(item -> {
            String optionA = item.getOrDefault("optionA", "").trim();
            String optionB = item.getOrDefault("optionB", "").trim();
            String optionC = item.getOrDefault("optionC", "").trim();
            String optionD = item.getOrDefault("optionD", "").trim();
            String prompt = item.getOrDefault("prompt", "").trim();

            if (prompt.isEmpty()) {
                throw new BadRequestException("Generated question has empty prompt.");
            }

            Map<String, String> options = new LinkedHashMap<>();
            options.put("A", optionA);
            options.put("B", optionB);
            options.put("C", optionC);
            options.put("D", optionD);

            return Question.builder()
                    .questionText(prompt)
                    .text(prompt)
                    .options(toJson(options))
                    .optionA(optionA)
                    .optionB(optionB)
                    .optionC(optionC)
                    .optionD(optionD)
                    .questionType("MCQ")
                    .correctAnswer(item.getOrDefault("correctOption", "A").toUpperCase())
                    .build();
        }).toList();

        // STEP 4: Create quiz object (quiz field in questions will be set during save)
        Quiz quiz = Quiz.builder()
                .topicId(1L)  // Ensure topicId is always set (FK constraint requires this)
                .title(request.title())
                .topic(request.topic())
                .difficulty(mapDifficulty(course.getDifficultyLevel()))
                .generatedByAI(true)
                .generatedByAiLegacy(true)
                .createdBy(instructor)
                .createdAt(LocalDateTime.now())
                .course(course)
                .build();

        // STEP 5: Save quiz atomically
        Quiz savedQuiz = quizRepository.saveAndFlush(quiz);
        log.info("✓ Quiz saved successfully with ID: {} and topicId: {}", savedQuiz.getId(), savedQuiz.getTopicId());
        log.info("Quiz details - CourseID: {}, Instructor: {}", savedQuiz.getCourse().getId(), savedQuiz.getCreatedBy().getId());

        // STEP 6: Link questions to saved quiz and save all questions atomically
        // If this fails, entire transaction rolls back (quiz never persisted)
        log.info("Linking {} questions to quiz {}", questionsToSave.size(), savedQuiz.getId());
        questionsToSave.forEach(q -> {
            q.setQuiz(savedQuiz);
            log.debug("Question prompt: {} -> Quiz: {}", q.getText().substring(0, Math.min(30, q.getText().length())), savedQuiz.getId());
        });
        questionRepository.saveAllAndFlush(questionsToSave);
        log.info("✓ All {} questions saved successfully for quiz {}", questionsToSave.size(), savedQuiz.getId());

        return new QuizSummaryDto(savedQuiz.getId(), course.getId(), savedQuiz.getTitle(), true);
    }

    public List<QuizSummaryDto> quizzesByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId).stream()
                .filter(q -> questionRepository.countByQuizId(q.getId()) > 0 || !q.getQuestions().isEmpty())
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .map(q -> new QuizSummaryDto(q.getId(), q.getCourse() == null ? null : q.getCourse().getId(), q.getTitle(), Boolean.TRUE.equals(q.getGeneratedByAI())))
                .toList();
    }

    public List<QuestionDto> questionsByQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));

        List<QuestionDto> newModelQuestions = questionRepository.findByQuizId(quizId).stream().map(q -> new QuestionDto(
            q.getId(),
            q.getQuiz().getId(),
            q.getQuestionText(),
            parseOptionsOrLegacy(q),
            q.getCorrectAnswer()
        )).toList();

        if (!newModelQuestions.isEmpty()) {
            return newModelQuestions;
        }

        return quiz.getQuestions().stream().map(q -> new QuestionDto(
            q.getId(),
            quiz.getId(),
            q.getPrompt(),
            Map.of(
                "A", q.getOptionA(),
                "B", q.getOptionB(),
                "C", q.getOptionC(),
                "D", q.getOptionD()
            ),
            q.getCorrectOption()
        )).toList();
    }

    private Difficulty mapDifficulty(String difficultyLevel) {
        String value = difficultyLevel == null ? "" : difficultyLevel.trim().toUpperCase();
        return switch (value) {
            case "ADVANCED", "HARD" -> Difficulty.HARD;
            case "INTERMEDIATE", "MEDIUM" -> Difficulty.MEDIUM;
            default -> Difficulty.EASY;
        };
    }

    private String toJson(Map<String, String> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (Exception ex) {
            throw new BadRequestException("Unable to serialize question options.");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseOptions(String optionsJson) {
        try {
            return objectMapper.readValue(optionsJson, Map.class);
        } catch (Exception ex) {
            throw new BadRequestException("Unable to parse stored question options.");
        }
    }

    private Map<String, String> parseOptionsOrLegacy(Question question) {
        if (question.getOptions() != null && !question.getOptions().isBlank()) {
            try {
                return parseOptions(question.getOptions());
            } catch (Exception ignored) {
                // Fallback below for legacy rows with option columns.
            }
        }

        return Map.of(
                "A", question.getOptionA() == null ? "" : question.getOptionA(),
                "B", question.getOptionB() == null ? "" : question.getOptionB(),
                "C", question.getOptionC() == null ? "" : question.getOptionC(),
                "D", question.getOptionD() == null ? "" : question.getOptionD()
        );
    }
}
