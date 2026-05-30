package com.memora.backend.repository;

import com.memora.backend.entity.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface HighlightRepository extends JpaRepository<Highlight, UUID> {
    List<Highlight> findAllByUserId(UUID userId);
    List<Highlight> findAllByContentItemId(UUID contentItemId);
}