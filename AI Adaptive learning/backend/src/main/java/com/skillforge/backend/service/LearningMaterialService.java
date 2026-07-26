package com.skillforge.backend.service;

import com.skillforge.backend.dto.LearningMaterialDto;
import com.skillforge.backend.dto.LearningMaterialRequest;
import com.skillforge.backend.exception.BadRequestException;
import com.skillforge.backend.exception.ResourceNotFoundException;
import com.skillforge.backend.model.AppUser;
import com.skillforge.backend.model.Course;
import com.skillforge.backend.model.LearningMaterial;
import com.skillforge.backend.repository.CourseRepository;
import com.skillforge.backend.repository.LearningMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningMaterialService {

    private final LearningMaterialRepository learningMaterialRepository;
    private final CourseRepository courseRepository;

    public LearningMaterialDto create(LearningMaterialRequest request, AppUser instructor) {
        Course course = resolveCourse(request.courseId(), instructor);
        validateResources(request.youtubeUrl(), request.pdfUrl());

        LearningMaterial saved = learningMaterialRepository.save(LearningMaterial.builder()
                .course(course)
                .instructor(instructor)
                .subject(request.subject().trim())
                .title(request.title().trim())
                .description(request.description() == null ? "" : request.description().trim())
            .youtubeUrl(normalizeUrl(request.youtubeUrl()))
            .pdfUrl(normalizeUrl(request.pdfUrl()))
                .createdAt(LocalDateTime.now())
                .build());

        return toDto(saved);
    }

    public List<LearningMaterialDto> listForInstructor(AppUser instructor) {
        return learningMaterialRepository.findByInstructorIdOrderByCreatedAtDesc(instructor.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<LearningMaterialDto> listForStudents() {
        return learningMaterialRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    public void delete(Long materialId, AppUser instructor) {
        LearningMaterial material = learningMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found."));

        if (!material.getInstructor().getId().equals(instructor.getId())) {
            throw new BadRequestException("You can delete only your own materials.");
        }

        learningMaterialRepository.delete(material);
    }

    private Course resolveCourse(Long requestedCourseId, AppUser instructor) {
        if (requestedCourseId != null) {
            Course course = courseRepository.findById(requestedCourseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
            return course;
        }

        return courseRepository.findByInstructorId(instructor.getId())
                .stream()
                .findFirst()
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .title("General Course")
                        .difficultyLevel("MEDIUM")
                        .instructor(instructor)
                        .build()));
    }

    private void validateResources(String youtubeUrl, String pdfUrl) {
        String yt = normalizeUrl(youtubeUrl);
        String pdf = normalizeUrl(pdfUrl);

        if ((yt == null || yt.isBlank()) && (pdf == null || pdf.isBlank())) {
            throw new BadRequestException("Provide at least one URL: YouTube or PDF.");
        }

        if (yt != null) {
            String lower = yt.toLowerCase();
            if (!(lower.contains("youtube.com") || lower.contains("youtu.be"))) {
                throw new BadRequestException("Please provide a valid YouTube URL.");
            }
        }

        if (pdf != null) {
            String lower = pdf.toLowerCase();
            if (!lower.contains(".pdf")) {
                throw new BadRequestException("Please provide a valid PDF URL.");
            }
        }
    }

    private String normalizeUrl(String url) {
        if (url == null) {
            return null;
        }
        String trimmed = url.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private LearningMaterialDto toDto(LearningMaterial material) {
        return new LearningMaterialDto(
                material.getId(),
                material.getCourse().getId(),
                material.getCourse().getTitle(),
                material.getSubject(),
                material.getTitle(),
                material.getDescription(),
                material.getYoutubeUrl(),
                material.getPdfUrl(),
                material.getInstructor().getName(),
                material.getCreatedAt()
        );
    }
}
