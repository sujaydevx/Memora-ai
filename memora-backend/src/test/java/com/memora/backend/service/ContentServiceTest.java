package com.memora.backend.service;

import com.memora.backend.dto.TopicDetectionResult;
import com.memora.backend.dto.request.SaveContentRequest;
import com.memora.backend.dto.response.ContentResponse;
import com.memora.backend.entity.ContentItem;
import com.memora.backend.entity.ContentType;
import com.memora.backend.entity.Topic;
import com.memora.backend.exception.DuplicateContentException;
import com.memora.backend.exception.MemoraServiceException;
import com.memora.backend.dto.request.ContentRequestValidator;
import com.memora.backend.dto.request.ValidContentRequest;
import com.memora.backend.repository.ContentItemRepository;
import com.memora.backend.repository.TopicRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContentServiceTest {

    @Mock
    private ContentItemRepository contentItemRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private LLMService llmService;
    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private ContentService contentService;

    private Validator validator;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private SaveContentRequest validTextRequest() {
        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.TEXT);
        request.setRawContent("Machine learning enables computers to learn from data");
        request.setPageTitle("ML Overview");
        request.setSourceUrl("https://example.com");
        return request;
    }

    private Topic mockTopic(String name, UUID parentId) {
        Topic topic = Topic.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name(name)
                .parentTopicId(parentId)
                .build();
        return topic;
    }

    private ContentItem mockContentItem(SaveContentRequest request) {
        return ContentItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(request.getType())
                .rawContent(request.getRawContent())
                .pageTitle(request.getPageTitle())
                .sourceUrl(request.getSourceUrl())
                .checksum("fakechecksum")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .topics(List.of(mockTopic("MachineLearning", null)))
                .build();
    }

    @Test
    void saveContent_happyPath() {
        SaveContentRequest request = validTextRequest();
        Topic primaryTopic = mockTopic("MachineLearning", null);
        Topic subtopic = mockTopic("NeuralNetworks", primaryTopic.getId());

        when(contentItemRepository.findByUserIdAndChecksum(any(), any())).thenReturn(Optional.empty());
        when(llmService.detectTopics(any())).thenReturn(
                new TopicDetectionResult("MachineLearning", List.of("NeuralNetworks"))
        );
        when(topicRepository.findByUserIdAndName(any(), eq("MachineLearning"))).thenReturn(Optional.empty());
        when(topicRepository.findByUserIdAndName(any(), eq("NeuralNetworks"))).thenReturn(Optional.empty());
        when(topicRepository.save(any())).thenReturn(primaryTopic).thenReturn(subtopic);
        when(contentItemRepository.save(any())).thenReturn(mockContentItem(request));
        when(embeddingService.generateAndStoreEmbedding(any(), any(), any())).thenReturn("fake-vector-id");

        ContentResponse response = contentService.saveContent(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(ContentType.TEXT);
        assertThat(response.getPrimaryTopic().getName()).isEqualTo("MachineLearning");
    }

    @Test
    void saveContent_duplicateContent_throws409() {
        SaveContentRequest request = validTextRequest();
        when(contentItemRepository.findByUserIdAndChecksum(any(), any()))
                .thenReturn(Optional.of(new ContentItem()));

        assertThatThrownBy(() -> contentService.saveContent(request, userId))
                .isInstanceOf(DuplicateContentException.class);

        verify(llmService, never()).detectTopics(any());
    }

    @Test
    void saveContent_llmTimeout_throwsMemoraServiceException() {
        SaveContentRequest request = validTextRequest();
        when(contentItemRepository.findByUserIdAndChecksum(any(), any())).thenReturn(Optional.empty());
        when(llmService.detectTopics(any())).thenThrow(new MemoraServiceException("LLM service timeout after 5 seconds"));

        assertThatThrownBy(() -> contentService.saveContent(request, userId))
                .isInstanceOf(MemoraServiceException.class);

        verify(contentItemRepository, never()).save(any());
    }

    @Test
    void saveContent_blankRawContent_textType() {
        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.TEXT);
        request.setRawContent("");

        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("rawContent is required for TEXT and NOTE types")
        )).isTrue();
    }

    @Test
    void saveContent_imageType_noMinioKey() {
        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.IMAGE);
        request.setMinioKey(null);

        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void saveContent_sameContentDifferentSourceUrl_deduplicates() {
        SaveContentRequest request1 = validTextRequest();
        request1.setSourceUrl("https://site1.com");

        SaveContentRequest request2 = validTextRequest();
        request2.setSourceUrl("https://site2.com");

        Topic primaryTopic = mockTopic("MachineLearning", null);

        when(contentItemRepository.findByUserIdAndChecksum(any(), any()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new ContentItem()));
        when(llmService.detectTopics(any())).thenReturn(
                new TopicDetectionResult("MachineLearning", List.of())
        );
        when(topicRepository.findByUserIdAndName(any(), any())).thenReturn(Optional.empty());
        when(topicRepository.save(any())).thenReturn(primaryTopic);
        when(contentItemRepository.save(any())).thenReturn(mockContentItem(request1));
        when(embeddingService.generateAndStoreEmbedding(any(), any(), any())).thenReturn("fake-vector-id");

        ContentResponse first = contentService.saveContent(request1, userId);
        assertThat(first).isNotNull();

        assertThatThrownBy(() -> contentService.saveContent(request2, userId))
                .isInstanceOf(DuplicateContentException.class);
    }

    @Test
    void deleteContent_wrongUser_throws404() {
        UUID ownerUserId = UUID.randomUUID();
        UUID wrongUserId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        ContentItem item = ContentItem.builder()
                .id(contentItemId)
                .userId(ownerUserId)
                .build();

        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> contentService.deleteContent(contentItemId, wrongUserId))
                .isInstanceOf(com.memora.backend.exception.ContentNotFoundException.class);

        verify(contentItemRepository, never()).delete(any());
    }

    @Test
    void getAllContent_empty_returnsEmptyPage() {
        when(contentItemRepository.findAllByUserId(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<ContentResponse> result = contentService.getAllContent(userId, 0, 20);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}