package com.memora.backend.entity;

import com.memora.backend.converter.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "compiled_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompiledDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String title;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "compiled_text", columnDefinition = "TEXT")
    private String compiledText;

    @Column(name = "minio_key")
    private String minioKey;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}