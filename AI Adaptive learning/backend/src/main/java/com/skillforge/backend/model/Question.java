package com.skillforge.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String options;

    @Column(name = "optiona", nullable = false)
    private String optionA;

    @Column(name = "optionb", nullable = false)
    private String optionB;

    @Column(name = "optionc", nullable = false)
    private String optionC;

    @Column(name = "optiond", nullable = false)
    private String optionD;

    @Column(name = "question_type", nullable = false)
    private String questionType;

    @Column(nullable = false)
    private String correctAnswer;
}
