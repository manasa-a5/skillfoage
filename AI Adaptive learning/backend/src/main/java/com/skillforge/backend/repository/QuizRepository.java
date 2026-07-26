package com.skillforge.backend.repository;

import com.skillforge.backend.model.Difficulty;
import com.skillforge.backend.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByDifficulty(Difficulty difficulty);
    List<Quiz> findByCourseId(Long courseId);
}
