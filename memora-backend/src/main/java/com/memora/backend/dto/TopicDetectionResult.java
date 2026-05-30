package com.memora.backend.dto;

import java.util.List;

public record TopicDetectionResult(String primaryTopic, List<String> subtopics) {
}