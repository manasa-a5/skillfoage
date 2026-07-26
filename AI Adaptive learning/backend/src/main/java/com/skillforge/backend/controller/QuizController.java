package com.skillforge.backend.controller;

import com.skillforge.backend.dto.QuestionDto;
import com.skillforge.backend.dto.GenerateQuizRequest;
import com.skillforge.backend.dto.QuizGenerationRequest;
import com.skillforge.backend.dto.QuizGenerationResponse;
import com.skillforge.backend.dto.QuizSummaryDto;
import com.skillforge.backend.dto.QuizDto;
import com.skillforge.backend.exception.BadRequestException;
import com.skillforge.backend.model.AppUser;
import com.skillforge.backend.model.Difficulty;
import com.skillforge.backend.service.QuizApiService;
import com.skillforge.backend.service.QuizService;
import com.skillforge.backend.service.SecurityContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QuizController {

    private final QuizApiService quizApiService;
    private final QuizService quizService;
    private final SecurityContextService securityContextService;

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<QuizGenerationResponse> generate(@Valid @RequestBody QuizGenerationRequest request) {
        AppUser user = securityContextService.currentUser();
        log.info("Quiz generation request: title={}, topic={}, difficulty={}, questionCount={}",
                request.title(), request.topic(), request.difficulty(), request.questionCount());
        log.info("Authenticated user: email={}, role={}", user.getEmail(), user.getRole());

        try {
            Difficulty difficulty;
            try {
            difficulty = Difficulty.valueOf(request.difficulty().trim().toUpperCase());
            } catch (Exception ex) {
            throw new BadRequestException("Invalid difficulty. Allowed values: EASY, MEDIUM, HARD");
            }

            QuizDto savedQuiz = quizService.generateQuiz(
                new GenerateQuizRequest(
                    request.title(),
                    request.topic(),
                    difficulty,
                    request.questionCount()
                ),
                user
            );

            return ResponseEntity.ok(new QuizGenerationResponse(
                savedQuiz.title(),
                savedQuiz.topic(),
                savedQuiz.difficulty().name(),
                savedQuiz.questions().size(),
                savedQuiz.questions().stream().map(q -> java.util.Map.of(
                    "prompt", q.prompt(),
                    "optionA", q.optionA(),
                    "optionB", q.optionB(),
                    "optionC", q.optionC(),
                    "optionD", q.optionD()
                )).toList()
            ));
        } catch (Exception ex) {
            log.error("Quiz generation failed for user {}: {}", user.getEmail(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<QuizSummaryDto>> byCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizApiService.quizzesByCourse(courseId));
    }

    @GetMapping("/{quizId}/questions")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<QuestionDto>> questionsByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizApiService.questionsByQuiz(quizId));
    }
}
