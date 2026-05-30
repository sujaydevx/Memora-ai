package com.memora.backend.service;

import com.memora.backend.dto.AuthRequest;
import com.memora.backend.dto.request.SaveContentRequest;
import com.memora.backend.dto.response.ContentResponse;
import com.memora.backend.entity.ContentType;
import com.memora.backend.exception.DuplicateContentException;
import com.memora.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ContentServiceTest {

    @Autowired
    private ContentService contentService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private UUID createTestUser() {
        String email = "test_" + UUID.randomUUID() + "@memora.com";
        AuthRequest request = new AuthRequest();
        request.setEmail(email);
        request.setPassword("password123");
        request.setFullName("Test User");
        authService.register(request);
        return userRepository.findByEmailHash(hashEmail(email)).get().getId();
    }

    private String hashEmail(String email) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.toLowerCase().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveContent_returnsContentResponse() {
        UUID userId = createTestUser();

        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.TEXT);
        request.setRawContent("Machine learning is a subset of artificial intelligence that enables computers to learn");
        request.setPageTitle("ML Introduction");
        request.setSourceUrl("https://example.com");

        ContentResponse response = contentService.saveContent(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.getPreview()).isNotBlank();
        assertThat(response.getPrimaryTopic()).isNotNull();
        System.out.println("Preview: " + response.getPreview());
        System.out.println("Primary topic: " + response.getPrimaryTopic().getName());
    }

    @Test
    void saveContent_duplicateThrowsException() {
        UUID userId = createTestUser();

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