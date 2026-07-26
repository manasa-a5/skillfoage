package com.skillforge.backend.service;

import com.skillforge.backend.dto.*;
import com.skillforge.backend.exception.ResourceNotFoundException;
import com.skillforge.backend.model.*;
import com.skillforge.backend.repository.CourseRepository;
import com.skillforge.backend.repository.QuizAttemptRepository;
import com.skillforge.backend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseRepository courseRepository;
    private final OpenAiQuizService openAiQuizService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public QuizDto generateQuiz(GenerateQuizRequest request, AppUser instructor) {
        List<Map<String, String>> generated = openAiQuizService.generateQuestions(request);

        Course course = courseRepository.findByInstructorId(instructor.getId())
                .stream()
                .findFirst()
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .title("Default Course")
                        .difficultyLevel(request.difficulty().name())
                        .instructor(instructor)
                        .build()));

        Quiz quiz = Quiz.builder()
                .title(request.title())
                .topic(request.topic())
                .difficulty(request.difficulty())
                .course(course)
                .generatedByAI(true)
                .generatedByAiLegacy(true)
                .createdBy(instructor)
                .createdAt(LocalDateTime.now())
                .build();

        Quiz savedQuiz = quizRepository.saveAndFlush(quiz);
        mirrorParentQuizRow(savedQuiz);

        generated.forEach(item -> {
            QuizQuestion question = QuizQuestion.builder()
                    .quiz(savedQuiz)
                    .prompt(item.getOrDefault("prompt", ""))
                    .optionA(item.getOrDefault("optionA", ""))
                    .optionB(item.getOrDefault("optionB", ""))
                    .optionC(item.getOrDefault("optionC", ""))
                    .optionD(item.getOrDefault("optionD", ""))
                    .correctOption(item.getOrDefault("correctOption", "A").trim().toUpperCase())
                    .build();
            savedQuiz.getQuestions().add(question);
        });

        Quiz saved = quizRepository.saveAndFlush(savedQuiz);
        return toDto(saved);
    }

    private void mirrorParentQuizRow(Quiz quiz) {
        // Legacy DBs may have FKs pointing to either quiz(id) or quizzes(id).
        mirrorIntoTable("quiz", quiz);
        mirrorIntoTable("quizzes", quiz);
    }

    private void mirrorIntoTable(String tableName, Quiz quiz) {
        String sql = "INSERT IGNORE INTO " + tableName +
                " (id, title, topic_id, course_id, generated_by_ai, generated_byai, topic, difficulty, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(
                    sql,
                    quiz.getId(),
                    quiz.getTitle(),
                    quiz.getTopicId(),
                    quiz.getCourse() == null ? null : quiz.getCourse().getId(),
                    Boolean.TRUE.equals(quiz.getGeneratedByAI()) ? 1 : 0,
                    Boolean.TRUE.equals(quiz.getGeneratedByAiLegacy()) ? 1 : 0,
                    quiz.getTopic(),
                    quiz.getDifficulty() == null ? null : quiz.getDifficulty().name(),
                    quiz.getCreatedBy() == null ? null : quiz.getCreatedBy().getId(),
                    java.sql.Timestamp.valueOf(quiz.getCreatedAt())
            );
        } catch (Exception ex) {
            log.debug("Skipping quiz table mirror for {}: {}", tableName, ex.getMessage());
        }
    }

    public List<QuizDto> listAllQuizzes() {
        return quizRepository.findAll().stream()
                .sorted(Comparator.comparing(Quiz::getCreatedAt).reversed())
                .map(this::toDto)
                .toList();
    }

    public QuizDto getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));
        return toDto(quiz);
    }

    public AttemptResultDto submitAttempt(Long quizId, SubmitAttemptRequest request, AppUser student) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));

        int total = quiz.getQuestions().size();
        int score = 0;

        for (QuizQuestion question : quiz.getQuestions()) {
            String userAnswer = request.answers().get(question.getId());
            if (userAnswer != null && userAnswer.equalsIgnoreCase(question.getCorrectOption())) {
                score++;
            }
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .score(score)
                .total(total)
                .attemptedAt(LocalDateTime.now())
                .build();

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        String recommendation = recommendDifficulty(student.getId()).name();

        double percent = total == 0 ? 0 : (score * 100.0 / total);
        return new AttemptResultDto(saved.getId(), score, total, percent, saved.getAttemptedAt(), recommendation);
    }

    public Difficulty recommendDifficulty(Long studentId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentIdOrderByAttemptedAtDesc(studentId);
        if (attempts.isEmpty()) {
            return Difficulty.EASY;
        }

        double avg = attempts.stream()
                .limit(5)
                .mapToDouble(a -> a.getTotal() == 0 ? 0 : (a.getScore() * 100.0 / a.getTotal()))
                .average()
                .orElse(0);

        if (avg >= 80) {
            return Difficulty.HARD;
        }
        if (avg >= 55) {
            return Difficulty.MEDIUM;
        }
        return Difficulty.EASY;
    }

    public List<AttemptResultDto> studentAttempts(Long studentId) {
        return quizAttemptRepository.findByStudentIdOrderByAttemptedAtDesc(studentId).stream()
                .map(a -> new AttemptResultDto(
                        a.getId(),
                        a.getScore(),
                        a.getTotal(),
                        a.getTotal() == 0 ? 0 : (a.getScore() * 100.0 / a.getTotal()),
                        a.getAttemptedAt(),
                        recommendDifficulty(studentId).name()
                ))
                .toList();
    }

    private QuizDto toDto(Quiz quiz) {
        return new QuizDto(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTopic(),
                quiz.getDifficulty(),
                quiz.getCreatedBy().getName(),
                quiz.getCreatedAt(),
                quiz.getQuestions().stream()
                        .map(q -> new QuizQuestionDto(
                                q.getId(),
                                q.getPrompt(),
                                q.getOptionA(),
                                q.getOptionB(),
                                q.getOptionC(),
                                q.getOptionD()
                        ))
                        .toList()
        );
    }
}
