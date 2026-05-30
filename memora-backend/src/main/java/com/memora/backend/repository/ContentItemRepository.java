package com.memora.backend.repository;

import com.memora.backend.entity.ContentItem;
import com.memora.backend.entity.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContentItemRepository extends JpaRepository<ContentItem, UUID> {
    Page<ContentItem> findAllByUserId(UUID userId, Pageable pageable);
    Optional<ContentItem> findByUserIdAndChecksum(UUID userId, String checksum);
    List<ContentItem> findAllBySessionId(UUID sessionId);
    List<ContentItem> findAllByUserIdAndType(UUID userId, ContentType type);
}