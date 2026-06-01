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
public class SessionResponse {
    private UUID id;
    private String heading;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private int contentCount;
    private OffsetDateTime createdAt;
}