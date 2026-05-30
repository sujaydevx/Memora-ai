package com.memora.backend.repository;

import com.memora.backend.entity.ExamProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExamProfileRepository extends JpaRepository<ExamProfile, UUID> {
    List<ExamProfile> findAllByUserId(UUID userId);
}