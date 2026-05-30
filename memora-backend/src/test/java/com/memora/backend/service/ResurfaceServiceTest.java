package com.memora.backend.service;

import com.memora.backend.dto.AuthRequest;
import com.memora.backend.dto.request.SaveContentRequest;
import com.memora.backend.dto.response.ResurfaceResponse;
import com.memora.backend.entity.ContentType;
import com.memora.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ResurfaceServiceTest {

    @Autowired
    private ResurfaceService resurfaceService;

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
    void resurface_withShortContext_returnsEmpty() {
        List<ResurfaceResponse> results = resurfaceService.resurface("hi", UUID.randomUUID());
        assertThat(results).isEmpty();
    }

    @Test
    void resurface_withValidContext_returnsResults() {
        UUID userId = createTestUser();

        SaveContentRequest ml1 = new SaveContentRequest();
        ml1.setType(ContentType.TEXT);
        ml1.setRawContent("Machine learning algorithms learn patterns from data using neural networks and deep learning");
        ml1.setPageTitle("ML Overview");
        contentService.saveContent(ml1, userId);

        SaveContentRequest ml2 = new SaveContentRequest();
        ml2.setType(ContentType.TEXT);
        ml2.setRawContent("Neural networks are inspired by biological neurons and are used in deep learning");
        ml2.setPageTitle("Neural Networks");
        contentService.saveContent(ml2, userId);

        SaveContentRequest cyber = new SaveContentRequest();
        cyber.setType(ContentType.TEXT);
        cyber.setRawContent("Cybersecurity involves protecting systems from digital attacks and unauthorized access");
        cyber.setPageTitle("Cybersecurity");
        contentService.saveContent(cyber, userId);

        List<ResurfaceResponse> results = resurfaceService.resurface("neural networks deep learning", userId);

        System.out.println("Resurface results: " + results);
        assertThat(results).isNotEmpty();
    }
}