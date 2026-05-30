package com.memora.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_topic_id")
    private UUID parentTopicId;

    @ManyToOne
    @JoinColumn(name = "parent_topic_id", insertable = false, updatable = false)
    private Topic parentTopic;

    @Column(name = "embedding_ref")
    private String embeddingRef;

    @ManyToMany(mappedBy = "topics")
    private List<ContentItem> contentItems;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}