package com.skillforge.backend.controller;

import com.skillforge.backend.dto.DashboardMetricsDto;
import com.skillforge.backend.dto.GenerateQuizRequest;
import com.skillforge.backend.dto.LearningMaterialDto;
import com.skillforge.backend.dto.LearningMaterialRequest;
import com.skillforge.backend.dto.QuizDto;
import com.skillforge.backend.service.AnalyticsService;
import com.skillforge.backend.service.LearningMaterialService;
import com.skillforge.backend.service.QuizService;
import com.skillforge.backend.service.SecurityContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
public class InstructorController {

    private final QuizService quizService;
    private final LearningMaterialService learningMaterialService;
    private final AnalyticsService analyticsService;
    private final SecurityContextService securityContextService;

    @PostMapping("/quizzes/generate")
    public ResponseEntity<QuizDto> generateQuiz(@Valid @RequestBody GenerateQuizRequest request) {
        return ResponseEntity.ok(quizService.generateQuiz(request, securityContextService.currentUser()));
    }

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizDto>> getQuizzes() {
        return ResponseEntity.ok(quizService.listAllQuizzes());
    }

    @PostMapping("/materials")
    public ResponseEntity<LearningMaterialDto> createMaterial(@Valid @RequestBody LearningMaterialRequest request) {
        return ResponseEntity.ok(learningMaterialService.create(request, securityContextService.currentUser()));
    }

    @GetMapping("/materials")
    public ResponseEntity<List<LearningMaterialDto>> listMaterials() {
        return ResponseEntity.ok(learningMaterialService.listForInstructor(securityContextService.currentUser()));
    }

    @DeleteMapping("/materials/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId) {
        learningMaterialService.delete(materialId, securityContextService.currentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics")
    public ResponseEntity<DashboardMetricsDto> getAnalytics() {
        return ResponseEntity.ok(analyticsService.instructorMetrics());
    }
}
