package com.memora.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.backend.dto.TopicDetectionResult;
import com.memora.backend.exception.MemoraServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${memora.llm.base-url}")
    private String baseUrl;

    @Value("${memora.llm.api-key}")
    private String apiKey;

    @Value("${memora.llm.model}")
    private String model;

    public TopicDetectionResult detectTopics(String content) {
        if (content == null || content.isBlank()) {
            return new TopicDetectionResult("Uncategorised", List.of());
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> systemMessage = Map.of(
                    "role", "system",
                    "content", "You are a topic detection assistant. Given a piece of text, identify the primary topic and up to 4 subtopics. Always respond with valid JSON only, no explanation, no markdown. Format: {\"primaryTopic\": \"string\", \"subtopics\": [\"string\", \"string\"]}"
            );

            Map<String, Object> userMessage = Map.of(
                    "role", "user",
                    "content", content
            );

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(systemMessage, userMessage)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", entity, String.class
            );

            log.info("Groq response: {}", response.getBody());

            JsonNode root = objectMapper.readTree(response.getBody());
            String messageContent = root.path("choices").get(0).path("message").path("content").asText();

            return objectMapper.readValue(messageContent, TopicDetectionResult.class);

        } catch (ResourceAccessException e) {
            throw new MemoraServiceException("LLM service timeout after 5 seconds", e);
        } catch (Exception e) {
            throw new MemoraServiceException("LLM topic detection failed: " + e.getMessage(), e);
        }
    }

    public String generateSessionHeading(String summary) {
        if (summary == null || summary.isBlank()) {
            return "Study Session";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> systemMessage = Map.of(
                    "role", "system",
                    "content", "You are a study assistant. Given these topics and content titles, generate a short 5-7 word heading that summarises what was studied. Return only the heading text, nothing else."
            );

            Map<String, Object> userMessage = Map.of(
                    "role", "user",
                    "content", summary
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(systemMessage, userMessage));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", entity, String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText().trim();

        } catch (Exception e) {
            log.warn("Failed to generate session heading: {}", e.getMessage());
            return "Study Session";
        }
    }
}