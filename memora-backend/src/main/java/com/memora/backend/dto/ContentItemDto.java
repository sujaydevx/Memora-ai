package com.memora.backend.dto;

import lombok.Data;

@Data
public class ContentItemDto {
    private String type;
    private String rawContent;
    private String sourceUrl;
    private String pageTitle;
    private String mimeType;
}