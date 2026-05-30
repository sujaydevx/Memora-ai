package com.memora.backend.repository;

import com.memora.backend.entity.ExamTopicCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExamTopicCoverageRepository extends JpaRepository<ExamTopicCoverage, UUID> {
    List<ExamTopicCoverage> findByExamProfileId(UUID examProfileId);
    List<ExamTopicCoverage> findByTopicId(UUID topicId);
}