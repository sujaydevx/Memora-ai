package com.memora.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "exam_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String name;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "syllabus_text", columnDefinition = "TEXT")
    private String syllabusText;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}