package com.memora.backend.dto.response;

import com.memora.backend.entity.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {
    private UUID id;
    private UUID userId;
    private ContentType type;
    private String preview;
    private String sourceUrl;
    private String pageTitle;
    private TopicResponse primaryTopic;
    private UUID sessionId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}