package com.memora.backend.repository;

import com.memora.backend.entity.ResurfacingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ResurfacingEventRepository extends JpaRepository<ResurfacingEvent, UUID> {
    List<ResurfacingEvent> findAllByUserId(UUID userId);
    List<ResurfacingEvent> findAllByContentItemId(UUID contentItemId);
}