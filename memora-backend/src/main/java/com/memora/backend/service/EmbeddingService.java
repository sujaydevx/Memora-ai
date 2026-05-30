package com.memora.backend.service;

import com.memora.backend.dto.SimilarityResult;
import com.memora.backend.exception.MemoraServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${memora.embedding.base-url}")
    private String baseUrl;

    public String generateAndStoreEmbedding(String contentItemId, String text, String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("contentItemId", contentItemId);
            bodyMap.put("text", text);
            bodyMap.put("userId", userId);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyMap, headers);

            log.info("Sending embed request to: {}/embed body: {}", baseUrl, bodyMap);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/embed", entity, Map.class
            );

            return (String) response.getBody().get("vectorId");

        } catch (Exception e) {
            throw new MemoraServiceException("Embedding service unavailable: " + e.getMessage(), e);
        }
    }

    public List<SimilarityResult> searchSimilar(String queryText, String userId, int topK) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("queryText", queryText);
            bodyMap.put("userId", userId);
            bodyMap.put("topK", topK);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyMap, headers);

            ResponseEntity<List<SimilarityResult>> response = restTemplate.exchange(
                    baseUrl + "/search",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Search failed, returning empty list: {}", e.getMessage());
            return List.of();
        }
    }
}