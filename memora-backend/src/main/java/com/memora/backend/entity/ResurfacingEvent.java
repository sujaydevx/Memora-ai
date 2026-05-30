package com.memora.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "resurfacing_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResurfacingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "content_item_id", nullable = false)
    private UUID contentItemId;

    @Column(name = "triggered_at")
    private OffsetDateTime triggeredAt;

    @Column(name = "context_url", columnDefinition = "TEXT")
    private String contextUrl;

    @PrePersist
    protected void onCreate() {
        triggeredAt = OffsetDateTime.now();
    }
}