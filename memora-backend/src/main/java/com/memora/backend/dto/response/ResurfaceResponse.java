package com.memora.backend.dto.response;

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
public class ResurfaceResponse {
    private UUID id;
    private String preview;
    private String primaryTopicName;
    private String sourceTitle;
    private String sourceUrl;
    private double similarityScore;
    private OffsetDateTime createdAt;
}