package com.skillforge.backend.service;

import com.skillforge.backend.dto.QuizAttemptRequest;
import com.skillforge.backend.dto.QuizAttemptResponse;
import com.skillforge.backend.exception.BadRequestException;
import com.skillforge.backend.exception.ResourceNotFoundException;
import com.skillforge.backend.model.AppUser;
import com.skillforge.backend.model.Question;
import com.skillforge.backend.model.Quiz;
import com.skillforge.backend.model.QuizAttempt;
import com.skillforge.backend.repository.QuestionRepository;
import com.skillforge.backend.repository.QuizAttemptRepository;
import com.skillforge.backend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizAttemptApiService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public QuizAttemptResponse submit(QuizAttemptRequest request, AppUser student) {
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));

        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        if (questions.isEmpty()) {
            throw new BadRequestException("Quiz has no questions.");
        }

        int correct = 0;
        for (Question question : questions) {
            String answer = request.answers().get(question.getId());
            if (answer != null && answer.equalsIgnoreCase(question.getCorrectAnswer())) {
                correct++;
            }
        }

        int percentage = Math.round((correct * 100.0f) / questions.size());

        QuizAttempt saved = quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .score(percentage)
                .total(100)
                .attemptedAt(LocalDateTime.now())
                .build());

        return new QuizAttemptResponse(
                saved.getId(),
                quiz.getId(),
                student.getId(),
                percentage,
                saved.getAttemptedAt(),
                mapLevel(percentage)
        );
    }

    public List<QuizAttemptResponse> attemptsByStudent(Long studentId, AppUser caller) {
        if (!caller.getId().equals(studentId)) {
            throw new BadRequestException("Students can access only their own attempts.");
        }

        return quizAttemptRepository.findByStudentIdOrderByAttemptedAtDesc(studentId).stream()
                .map(a -> new QuizAttemptResponse(
                        a.getId(),
                        a.getQuiz().getId(),
                        a.getStudent().getId(),
                        a.getScore(),
                        a.getAttemptedAt(),
                        mapLevel(a.getScore())
                ))
                .toList();
    }

    private String mapLevel(int score) {
        if (score < 40) {
            return "Beginner";
        }
        if (score <= 70) {
            return "Intermediate";
        }
        return "Advanced";
    }
}
