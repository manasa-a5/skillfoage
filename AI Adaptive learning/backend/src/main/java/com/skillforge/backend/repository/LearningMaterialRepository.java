package com.skillforge.backend.repository;

import com.skillforge.backend.model.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {
    List<LearningMaterial> findByInstructorIdOrderByCreatedAtDesc(Long instructorId);
    List<LearningMaterial> findAllByOrderByCreatedAtDesc();
}
