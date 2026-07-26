package com.skillforge.backend.controller;

import com.skillforge.backend.dto.QuestionDto;
import com.skillforge.backend.service.QuizApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuizApiService quizApiService;

    @GetMapping("/{quizId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<QuestionDto>> byQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizApiService.questionsByQuiz(quizId));
    }
}
