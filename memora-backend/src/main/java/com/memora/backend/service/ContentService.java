package com.memora.backend.service;

import com.memora.backend.dto.TopicDetectionResult;
import com.memora.backend.dto.request.SaveContentRequest;
import com.memora.backend.dto.response.ContentResponse;
import com.memora.backend.dto.response.TopicResponse;
import com.memora.backend.entity.ContentItem;
import com.memora.backend.entity.Topic;
import com.memora.backend.exception.ContentNotFoundException;
import com.memora.backend.exception.DuplicateContentException;
import com.memora.backend.repository.ContentItemRepository;
import com.memora.backend.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    private final ContentItemRepository contentItemRepository;
    private final TopicRepository topicRepository;
    private final LLMService llmService;
    private final EmbeddingService embeddingService;

    public ContentResponse saveContent(SaveContentRequest request, UUID userId) {
        // Step 1 — Compute checksum
        String checksumInput = request.getRawContent() != null
                ? request.getRawContent()
                : request.getMinioKey();
        String checksum = computeChecksum(checksumInput);

        // Step 2 — Duplicate detection
        if (contentItemRepository.findByUserIdAndChecksum(userId, checksum).isPresent()) {
            throw new DuplicateContentException("You already saved this content.");
        }

        // Step 3 — Detect topics
        String textForTopics = request.getRawContent() != null
                ? request.getRawContent()
                : request.getPageTitle();
        TopicDetectionResult topicResult = llmService.detectTopics(textForTopics);

        // Step 4 — Find or create topics
        Topic primaryTopic = findOrCreateTopic(userId, topicResult.primaryTopic(), null);
        List<Topic> allTopics = new ArrayList<>();
        allTopics.add(primaryTopic);
        for (String subtopicName : topicResult.subtopics()) {
            Topic subtopic = findOrCreateTopic(userId, subtopicName, primaryTopic.getId());
            allTopics.add(subtopic);
        }

        // Step 5 — Build and save ContentItem
        ContentItem item = ContentItem.builder()
                .userId(userId)
                .type(request.getType())
                .rawContent(request.getRawContent())
                .sourceUrl(request.getSourceUrl())
                .pageTitle(request.getPageTitle())
                .mimeType(request.getMimeType())
                .minioKey(request.getMinioKey())
                .checksum(checksum)
                .sessionId(request.getSessionId())
                .topics(allTopics)
                .build();

        ContentItem saved = contentItemRepository.save(item);

        // Step 6 — Generate and store embedding
        String textForEmbedding = request.getRawContent() != null
                ? request.getRawContent()
                : request.getPageTitle();
        String vectorId = embeddingService.generateAndStoreEmbedding(
                saved.getId().toString(),
                textForEmbedding,
                userId.toString()
        );
        log.info("Stored embedding with vectorId: {}", vectorId);

        // Step 7 — Build and return response
        return buildContentResponse(saved, primaryTopic);
    }

    public Page<ContentResponse> getAllContent(UUID userId, int page, int size) {
        return contentItemRepository.findAllByUserId(
                userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map(item -> buildContentResponse(item, getPrimaryTopic(item)));
    }

    public void deleteContent(UUID contentItemId, UUID userId) {
        ContentItem item = contentItemRepository.findById(contentItemId)
                .orElseThrow(() -> new ContentNotFoundException(contentItemId));
        if (!item.getUserId().equals(userId)) {
            throw new ContentNotFoundException(contentItemId);
        }
        contentItemRepository.delete(item);
    }

    private Topic findOrCreateTopic(UUID userId, String name, UUID parentTopicId) {
        return topicRepository.findByUserIdAndName(userId, name)
                .orElseGet(() -> {
                    Topic topic = Topic.builder()
                            .userId(userId)
                            .name(name)
                            .parentTopicId(parentTopicId)
                            .build();
                    return topicRepository.save(topic);
                });
    }

    private Topic getPrimaryTopic(ContentItem item) {
        if (item.getTopics() == null || item.getTopics().isEmpty()) return null;
        return item.getTopics().stream()
                .filter(t -> t.getParentTopicId() == null)
                .findFirst()
                .orElse(item.getTopics().get(0));
    }

    private ContentResponse buildContentResponse(ContentItem item, Topic primaryTopic) {
        String preview = item.getRawContent() != null
                ? item.getRawContent().substring(0, Math.min(200, item.getRawContent().length()))
                : null;

        TopicResponse topicResponse = primaryTopic != null ? TopicResponse.builder()
                .id(primaryTopic.getId())
                .name(primaryTopic.getName())
                .parentTopicId(primaryTopic.getParentTopicId())
                .build() : null;

        return ContentResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .type(item.getType())
                .preview(preview)
                .sourceUrl(item.getSourceUrl())
                .pageTitle(item.getPageTitle())
                .primaryTopic(topicResponse)
                .sessionId(item.getSessionId())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private String computeChecksum(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute checksum", e);
        }
    }
}