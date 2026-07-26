package com.skillforge.backend.controller;

import com.skillforge.backend.dto.CourseDto;
import com.skillforge.backend.dto.CourseRequest;
import com.skillforge.backend.service.CourseService;
import com.skillforge.backend.service.SecurityContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final SecurityContextService securityContextService;

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseDto> create(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.create(request, securityContextService.currentUser()));
    }

    @GetMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<CourseDto>> list() {
        return ResponseEntity.ok(courseService.list());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseDto> update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.update(id, request, securityContextService.currentUser()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id, securityContextService.currentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<CourseDto>> allCourses() {
        return ResponseEntity.ok(courseService.list());
    }
}
