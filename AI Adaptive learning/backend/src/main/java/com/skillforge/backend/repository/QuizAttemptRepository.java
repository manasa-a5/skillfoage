package com.skillforge.backend.repository;

import com.skillforge.backend.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByStudentIdOrderByAttemptedAtDesc(Long studentId);
}
