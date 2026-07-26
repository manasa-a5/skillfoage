package com.skillforge.backend.service;

import com.skillforge.backend.dto.DashboardMetricsDto;
import com.skillforge.backend.model.QuizAttempt;
import com.skillforge.backend.repository.QuizAttemptRepository;
import com.skillforge.backend.repository.QuizRepository;
import com.skillforge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public DashboardMetricsDto instructorMetrics() {
        List<QuizAttempt> attempts = quizAttemptRepository.findAll();
        double avg = attempts.stream()
                .mapToDouble(a -> a.getTotal() == 0 ? 0 : (a.getScore() * 100.0 / a.getTotal()))
                .average()
                .orElse(0);

        return new DashboardMetricsDto(
                userRepository.findAll().stream().filter(u -> u.getRole().name().equals("STUDENT")).count(),
                quizRepository.count(),
                attempts.size(),
                avg
        );
    }

    public Map<String, Object> studentMetrics(Long studentId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentIdOrderByAttemptedAtDesc(studentId);
        double avg = attempts.stream()
                .mapToDouble(a -> a.getTotal() == 0 ? 0 : (a.getScore() * 100.0 / a.getTotal()))
                .average()
                .orElse(0);

        return Map.of(
                "attemptCount", attempts.size(),
                "averageScorePercent", avg,
                "adaptiveRecommendation", attempts.isEmpty() ? "EASY" :
                        (avg >= 80 ? "HARD" : (avg >= 55 ? "MEDIUM" : "EASY"))
        );
    }
}
