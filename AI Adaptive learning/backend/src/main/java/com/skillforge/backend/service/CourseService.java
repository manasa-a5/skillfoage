package com.skillforge.backend.service;

import com.skillforge.backend.dto.CourseDto;
import com.skillforge.backend.dto.CourseRequest;
import com.skillforge.backend.exception.ResourceNotFoundException;
import com.skillforge.backend.model.AppUser;
import com.skillforge.backend.model.Course;
import com.skillforge.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseDto create(CourseRequest request, AppUser instructor) {
        Course saved = courseRepository.save(Course.builder()
                .title(request.title())
                .difficultyLevel(request.difficultyLevel())
                .instructor(instructor)
                .build());
        return toDto(saved);
    }

    public List<CourseDto> list() {
        return courseRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<CourseDto> listForInstructor(AppUser instructor) {
        return courseRepository.findByInstructorId(instructor.getId()).stream().map(this::toDto).toList();
    }

    public CourseDto update(Long id, CourseRequest request, AppUser instructor) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new ResourceNotFoundException("Course not found for instructor.");
        }

        course.setTitle(request.title());
        course.setDifficultyLevel(request.difficultyLevel());
        return toDto(courseRepository.save(course));
    }

    public void delete(Long id, AppUser instructor) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new ResourceNotFoundException("Course not found for instructor.");
        }

        courseRepository.delete(course);
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));
    }

    private CourseDto toDto(Course course) {
        return new CourseDto(course.getId(), course.getTitle(), course.getInstructor().getId(), course.getDifficultyLevel());
    }
}
