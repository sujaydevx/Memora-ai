package com.memora.backend.repository;

import com.memora.backend.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {
    List<StudySession> findAllByUserIdOrderByStartedAtDesc(UUID userId);
    Optional<StudySession> findByUserIdAndEndedAtIsNull(UUID userId);
}