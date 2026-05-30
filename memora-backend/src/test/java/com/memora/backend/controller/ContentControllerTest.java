package com.memora.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.backend.dto.request.SaveContentRequest;
import com.memora.backend.dto.response.ContentResponse;
import com.memora.backend.dto.response.ResurfaceResponse;
import com.memora.backend.entity.ContentType;
import com.memora.backend.exception.ContentNotFoundException;
import com.memora.backend.exception.DuplicateContentException;
import com.memora.backend.exception.MemoraServiceException;
import com.memora.backend.service.ContentService;
import com.memora.backend.service.ResurfaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
public class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContentService contentService;

    @MockBean
    private ResurfaceService resurfaceService;

    private SaveContentRequest validRequest() {
        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.TEXT);
        request.setRawContent("Machine learning is a subset of artificial intelligence");
        request.setPageTitle("ML Overview");
        return request;
    }

    private ContentResponse mockContentResponse() {
        return ContentResponse.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .type(ContentType.TEXT)
                .preview("Machine learning is a subset")
                .pageTitle("ML Overview")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void postSave_validBody_returns201() throws Exception {
        when(contentService.saveContent(any(), any())).thenReturn(mockContentResponse());

        mockMvc.perform(post("/api/v1/content/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("TEXT"));
    }

    @Test
    void postSave_duplicate_returns409() throws Exception {
        when(contentService.saveContent(any(), any()))
                .thenThrow(new DuplicateContentException("You already saved this content."));

        mockMvc.perform(post("/api/v1/content/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void postSave_blankRawContentTextType_returns400() throws Exception {
        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.TEXT);
        request.setRawContent("");

        mockMvc.perform(post("/api/v1/content/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postSave_llmDown_returns503() throws Exception {
        when(contentService.saveContent(any(), any()))
                .thenThrow(new MemoraServiceException("LLM service timeout after 5 seconds"));

        mockMvc.perform(post("/api/v1/content/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.retryable").value(true));
    }

    @Test
    void getAll_returns200() throws Exception {
        when(contentService.getAllContent(any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(mockContentResponse())));

        mockMvc.perform(get("/api/v1/content/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void delete_valid_returns204() throws Exception {
        doNothing().when(contentService).deleteContent(any(), any());

        mockMvc.perform(delete("/api/v1/content/" + UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new ContentNotFoundException(UUID.randomUUID()))
                .when(contentService).deleteContent(any(), any());

        mockMvc.perform(delete("/api/v1/content/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void resurface_validContext_returns200() throws Exception {
        ResurfaceResponse response = ResurfaceResponse.builder()
                .id(UUID.randomUUID())
                .preview("Machine learning content")
                .similarityScore(0.95)
                .createdAt(OffsetDateTime.now())
                .build();

        when(resurfaceService.resurface(any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/content/resurface")
                        .param("context", "neural networks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void resurface_blankContext_returns200EmptyList() throws Exception {
        when(resurfaceService.resurface(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/content/resurface")
                        .param("context", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}