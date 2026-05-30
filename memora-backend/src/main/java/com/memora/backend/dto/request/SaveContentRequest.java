package com.memora.backend.dto.request;

import com.memora.backend.entity.ContentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
@ValidContentRequest
public class SaveContentRequest {

    @NotNull(message = "Content type is required")
    private ContentType type;

    @Size(max = 50000)
    private String rawContent;

    @Size(max = 2000)
    private String sourceUrl;

    @Size(max = 500)
    private String pageTitle;

    private UUID sessionId;
    private String mimeType;
    private String minioKey;
}