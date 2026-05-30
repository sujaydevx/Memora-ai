package com.memora.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "topic_clusters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicCluster {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToMany
    @JoinTable(
            name = "topic_cluster_topics",
            joinColumns = @JoinColumn(name = "topic_cluster_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private List<Topic> topics;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}