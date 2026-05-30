package com.memora.backend.entity;

import com.memora.backend.converter.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "content_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "content_type")
    private ContentType type;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "raw_content", columnDefinition = "TEXT")
    private String rawContent;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(name = "page_title")
    private String pageTitle;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "minio_key")
    private String minioKey;

    @Column(nullable = false)
    private String checksum;

    @Column(name = "session_id")
    private UUID sessionId;

    @ManyToOne
    @JoinColumn(name = "session_id", insertable = false, updatable = false)
    private StudySession session;

    @ManyToMany
    @JoinTable(
            name = "content_item_topics",
            joinColumns = @JoinColumn(name = "content_item_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private List<Topic> topics;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}