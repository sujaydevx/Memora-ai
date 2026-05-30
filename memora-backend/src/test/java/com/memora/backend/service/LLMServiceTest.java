package com.memora.backend.service;

import com.memora.backend.dto.TopicDetectionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LLMServiceTest {

    @Autowired
    private LLMService llmService;

    @Test
    void detectTopics_withValidContent_returnsTopics() {
        TopicDetectionResult result = llmService.detectTopics(
                "Machine learning is a subset of artificial intelligence that enables systems to learn from data"
        );
        assertThat(result).isNotNull();
        assertThat(result.primaryTopic()).isNotBlank();
        System.out.println("Primary topic: " + result.primaryTopic());
        System.out.println("Subtopics: " + result.subtopics());
    }

    @Test
    void detectTopics_withNullContent_returnsUncategorised() {
        TopicDetectionResult result = llmService.detectTopics(null);
        assertThat(result.primaryTopic()).isEqualTo("Uncategorised");
        assertThat(result.subtopics()).isEmpty();
    }
}