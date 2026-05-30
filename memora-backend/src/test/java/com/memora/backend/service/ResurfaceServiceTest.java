package com.memora.backend.service;

import com.memora.backend.dto.SimilarityResult;
import com.memora.backend.dto.response.ResurfaceResponse;
import com.memora.backend.entity.ContentItem;
import com.memora.backend.repository.ContentItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResurfaceServiceTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private ContentItemRepository contentItemRepository;

    @InjectMocks
    private ResurfaceService resurfaceService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void resurface_happyPath() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(embeddingService.searchSimilar(any(), any(), anyInt())).thenReturn(List.of(
                new SimilarityResult(id1.toString(), 0.95),
                new SimilarityResult(id2.toString(), 0.80)
        ));

        ContentItem item1 = ContentItem.builder()
                .id(id1).userId(userId)
                .rawContent("Machine learning content")
                .createdAt(OffsetDateTime.now()).build();

        ContentItem item2 = ContentItem.builder()
                .id(id2).userId(userId)
                .rawContent("Neural networks content")
                .createdAt(OffsetDateTime.now()).build();

        when(contentItemRepository.findAllById(any())).thenReturn(List.of(item1, item2));

        List<ResurfaceResponse> results = resurfaceService.resurface("machine learning", userId);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getSimilarityScore()).isGreaterThan(results.get(1).getSimilarityScore());
    }

    @Test
    void resurface_embeddingServiceDown_returnsEmptyList() {
        when(embeddingService.searchSimilar(any(), any(), anyInt()))
                .thenThrow(new RuntimeException("Service down"));

        List<ResurfaceResponse> results = resurfaceService.resurface("machine learning", userId);

        assertThat(results).isEmpty();
    }

    @Test
    void resurface_blankContext_returnsEmptyListImmediately() {
        List<ResurfaceResponse> results = resurfaceService.resurface("", userId);

        assertThat(results).isEmpty();
        verify(embeddingService, never()).searchSimilar(any(), any(), anyInt());
    }

    @Test
    void resurface_userIsolation_filtersWrongUser() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID wrongUserId = UUID.randomUUID();

        when(embeddingService.searchSimilar(any(), any(), anyInt())).thenReturn(List.of(
                new SimilarityResult(id1.toString(), 0.95),
                new SimilarityResult(id2.toString(), 0.80)
        ));

        ContentItem item1 = ContentItem.builder()
                .id(id1).userId(userId)
                .rawContent("Correct user content")
                .createdAt(OffsetDateTime.now()).build();

        ContentItem item2 = ContentItem.builder()
                .id(id2).userId(wrongUserId)
                .rawContent("Wrong user content")
                .createdAt(OffsetDateTime.now()).build();

        when(contentItemRepository.findAllById(any())).thenReturn(List.of(item1, item2));

        List<ResurfaceResponse> results = resurfaceService.resurface("machine learning", userId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(id1);
    }
}