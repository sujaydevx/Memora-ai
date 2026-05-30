package com.memora.backend.entity;

import com.memora.backend.converter.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "highlights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Highlight {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "content_item_id", nullable = false)
    private UUID contentItemId;

    @ManyToOne
    @JoinColumn(name = "content_item_id", insertable = false, updatable = false)
    private ContentItem contentItem;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "selected_text", columnDefinition = "TEXT")
    private String selectedText;

    @Column(name = "surrounding_context", columnDefinition = "TEXT")
    private String surroundingContext;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "page_url", columnDefinition = "TEXT")
    private String pageUrl;

    @Column(name = "page_title")
    private String pageTitle;

    @Column(name = "highlight_color")
    private String highlightColor = "yellow";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "position_data", columnDefinition = "jsonb")
    private String positionData;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}