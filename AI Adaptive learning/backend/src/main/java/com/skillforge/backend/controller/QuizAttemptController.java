package com.skillforge.backend.controller;

import com.skillforge.backend.dto.QuizAttemptRequest;
import com.skillforge.backend.dto.QuizAttemptResponse;
import com.skillforge.backend.service.QuizAttemptApiService;
import com.skillforge.backend.service.SecurityContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptApiService quizAttemptApiService;
    private final SecurityContextService securityContextService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizAttemptResponse> submit(@Valid @RequestBody QuizAttemptRequest request) {
        return ResponseEntity.ok(quizAttemptApiService.submit(request, securityContextService.currentUser()));
    }

    @GetMapping("/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<QuizAttemptResponse>> byStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(quizAttemptApiService.attemptsByStudent(studentId, securityContextService.currentUser()));
    }
}
