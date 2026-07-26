package com.skillforge.backend.controller;

import com.skillforge.backend.dto.AttemptResultDto;
import com.skillforge.backend.dto.LearningMaterialDto;
import com.skillforge.backend.dto.QuizDto;
import com.skillforge.backend.dto.SubmitAttemptRequest;
import com.skillforge.backend.service.AnalyticsService;
import com.skillforge.backend.service.LearningMaterialService;
import com.skillforge.backend.service.QuizService;
import com.skillforge.backend.service.SecurityContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final QuizService quizService;
    private final LearningMaterialService learningMaterialService;
    private final SecurityContextService securityContextService;
    private final AnalyticsService analyticsService;

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizDto>> quizzes() {
        return ResponseEntity.ok(quizService.listAllQuizzes());
    }

    @GetMapping("/materials")
    public ResponseEntity<List<LearningMaterialDto>> materials() {
        return ResponseEntity.ok(learningMaterialService.listForStudents());
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizDto> quiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuiz(quizId));
    }

    @PostMapping("/quizzes/{quizId}/submit")
    public ResponseEntity<AttemptResultDto> submit(@PathVariable Long quizId,
                                                    @Valid @RequestBody SubmitAttemptRequest request) {
        return ResponseEntity.ok(quizService.submitAttempt(quizId, request, securityContextService.currentUser()));
    }

    @GetMapping("/attempts")
    public ResponseEntity<List<AttemptResultDto>> attempts() {
        Long studentId = securityContextService.currentUser().getId();
        return ResponseEntity.ok(quizService.studentAttempts(studentId));
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> analytics() {
        Long studentId = securityContextService.currentUser().getId();
        return ResponseEntity.ok(analyticsService.studentMetrics(studentId));
    }
}
