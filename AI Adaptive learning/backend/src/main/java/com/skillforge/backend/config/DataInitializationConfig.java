package com.skillforge.backend.config;

import com.skillforge.backend.model.*;
import com.skillforge.backend.repository.TopicRepository;
import com.skillforge.backend.repository.CourseRepository;
import com.skillforge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializationConfig {

    private final TopicRepository topicRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Bean
    public ApplicationRunner initializeDefaultData() {
        return args -> {
            log.info("🔧 Starting DataInitializationConfig...");
            // Ensure default topic with ID 1 exists (required for quiz FK constraint)
            if (topicRepository.findById(1L).isEmpty()) {
                log.info("📌 Topic ID 1 not found, creating default initialization chain...");
                // Get or create default course
                Course defaultCourse = courseRepository.findAll().stream().findFirst().orElseGet(() -> {
                    log.info("📌 No course found, creating default instructor...");
                    // Create default course if none exists
                    AppUser defaultInstructor = userRepository.findAll().stream()
                            .filter(u -> u.getRole() == Role.INSTRUCTOR)
                            .findFirst()
                            .orElseGet(() -> {
                                log.info("📌 No instructor found, creating default instructor user...");
                                AppUser instructor = AppUser.builder()
                                        .email("default@skillforge.local")
                                        .name("System")
                                        .username("system")
                                        .password("system")
                                        .legacyPassword("system")
                                        .role(Role.INSTRUCTOR)
                                        .createdAt(java.time.LocalDateTime.now())
                                        .build();
                                AppUser saved = userRepository.save(instructor);
                                log.info("✓ Created default instructor with ID: {}", saved.getId());
                                return saved;
                            });

                    log.info("📌 Creating default course...");
                    Course course = Course.builder()
                            .title("Default Course")
                            .difficultyLevel("Beginner")
                            .instructor(defaultInstructor)
                            .build();
                    Course savedCourse = courseRepository.save(course);
                    log.info("✓ Created default course with ID: {}", savedCourse.getId());
                    return savedCourse;
                });

                log.info("📌 Creating default topic with ID 1...");
                Topic defaultTopic = Topic.builder()
                        .id(1L)
                        .name("Default Topic")
                        .title("Default Topic")
                        .course(defaultCourse)
                        .build();
                Topic savedTopic = topicRepository.save(defaultTopic);
                log.info("✓ Successfully created default topic with ID: {}", savedTopic.getId());
            } else {
                log.info("✓ Topic ID 1 already exists, skipping initialization");
            }
            log.info("✓ DataInitializationConfig completed");
        };
    }
}
