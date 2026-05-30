package com.memora.backend.service;

import com.memora.backend.dto.SimilarityResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EmbeddingServiceTest {

    @Autowired
    private EmbeddingService embeddingService;

    @Test
    void generateAndStoreEmbedding_returnsVectorId() {
        String contentItemId = UUID.randomUUID().toString();
        String vectorId = embeddingService.generateAndStoreEmbedding(
                contentItemId,
                "Machine learning is a subset of artificial intelligence",
                "user-001"
        );
        assertThat(vectorId).isNotNull();
        assertThat(vectorId).contains(contentItemId);
        System.out.println("VectorId: " + vectorId);
    }

    @Test
    void searchSimilar_returnsResults() {
        String contentItemId = UUID.randomUUID().toString();
        embeddingService.generateAndStoreEmbedding(
                contentItemId,
                "Deep learning neural networks for image recognition",
                "user-001"
        );

        List<SimilarityResult> results = embeddingService.searchSimilar(
                "artificial intelligence image processing",
                "user-001",
                5
        );
        assertThat(results).isNotEmpty();
        System.out.println("Results: " + results);
    }
}