package com.memora.backend.service;

import com.memora.backend.dto.request.SaveContentRequest;
import com.memora.backend.dto.response.ContentResponse;
import com.memora.backend.entity.ContentType;
import com.memora.backend.exception.DuplicateContentException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ContentServiceTest {

    @Autowired
    private ContentService contentService;

    @Test
    void saveContent_returnsContentResponse() {
        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.TEXT);
        request.setRawContent("Machine learning is a subset of artificial intelligence that enables computers to learn");
        request.setPageTitle("ML Introduction");
        request.setSourceUrl("https://example.com");

        ContentResponse response = contentService.saveContent(request, UUID.randomUUID());

        assertThat(response).isNotNull();
        assertThat(response.getPreview()).isNotBlank();
        assertThat(response.getPrimaryTopic()).isNotNull();
        System.out.println("Preview: " + response.getPreview());
        System.out.println("Primary topic: " + response.getPrimaryTopic().getName());
    }

    @Test
    void saveContent_duplicateThrowsException() {
        UUID userId = UUID.randomUUID();
        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.TEXT);
        request.setRawContent("This is unique content for duplicate test " + userId);
        request.setPageTitle("Test");

        contentService.saveContent(request, userId);

        assertThatThrownBy(() -> contentService.saveContent(request, userId))
                .isInstanceOf(DuplicateContentException.class)
                .hasMessageContaining("already saved");
    }
}