package com.memora.backend.service;

import com.memora.backend.dto.SimilarityResult;
import com.memora.backend.dto.response.ResurfaceResponse;
import com.memora.backend.entity.ContentItem;
import com.memora.backend.entity.Topic;
import com.memora.backend.repository.ContentItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResurfaceService {

    private final EmbeddingService embeddingService;
    private final ContentItemRepository contentItemRepository;

    public List<ResurfaceResponse> resurface(String context, UUID userId) {
        if (context == null || context.isBlank() || context.length() < 5) {
            return List.of();
        }

        try {
            List<SimilarityResult> similarityResults = embeddingService.searchSimilar(
                    context, userId.toString(), 5
            );

            if (similarityResults.isEmpty()) return List.of();

            List<UUID> contentItemIds = similarityResults.stream()
                    .map(r -> UUID.fromString(r.contentItemId()))
                    .collect(Collectors.toList());

            List<ContentItem> items = contentItemRepository.findAllById(contentItemIds);

            // Security check — only return items belonging to this user
            Map<UUID, Double> scoreMap = similarityResults.stream()
                    .collect(Collectors.toMap(
                            r -> UUID.fromString(r.contentItemId()),
                            SimilarityResult::similarityScore
                    ));

            return items.stream()
                    .filter(item -> item.getUserId().equals(userId))
                    .map(item -> buildResurfaceResponse(item, scoreMap.getOrDefault(item.getId(), 0.0)))
                    .sorted(Comparator.comparingDouble(ResurfaceResponse::getSimilarityScore).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Resurfacing failed, returning empty list: {}", e.getMessage());
            return List.of();
        }
    }

    private ResurfaceResponse buildResurfaceResponse(ContentItem item, double score) {
        String preview = item.getRawContent() != null
                ? item.getRawContent().substring(0, Math.min(200, item.getRawContent().length()))
                : null;

        String primaryTopicName = null;
        if (item.getTopics() != null && !item.getTopics().isEmpty()) {
            primaryTopicName = item.getTopics().stream()
                    .filter(t -> t.getParentTopicId() == null)
                    .findFirst()
                    .map(Topic::getName)
                    .orElse(item.getTopics().get(0).getName());
        }

        return ResurfaceResponse.builder()
                .id(item.getId())
                .preview(preview)
                .primaryTopicName(primaryTopicName)
                .sourceTitle(item.getPageTitle())
                .sourceUrl(item.getSourceUrl())
                .similarityScore(score)
                .createdAt(item.getCreatedAt())
                .build();
    }
}