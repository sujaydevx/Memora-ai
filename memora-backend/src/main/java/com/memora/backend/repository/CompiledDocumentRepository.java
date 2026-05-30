package com.memora.backend.repository;

import com.memora.backend.entity.CompiledDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CompiledDocumentRepository extends JpaRepository<CompiledDocument, UUID> {
    List<CompiledDocument> findByUserId(UUID userId);
}