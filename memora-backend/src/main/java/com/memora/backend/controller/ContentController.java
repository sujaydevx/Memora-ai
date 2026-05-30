package com.memora.backend.controller;

import com.memora.backend.dto.request.SaveContentRequest;
import com.memora.backend.dto.response.ContentResponse;
import com.memora.backend.dto.response.ResurfaceResponse;
import com.memora.backend.service.ContentService;
import com.memora.backend.service.ResurfaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final ContentService contentService;
    private final ResurfaceService resurfaceService;

    @PostMapping("/save")
    public ResponseEntity<ContentResponse> save(@RequestBody @Valid SaveContentRequest request) {
        ContentResponse response = contentService.saveContent(request, TEST_USER_ID);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ContentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(contentService.getAllContent(TEST_USER_ID, page, size).getContent());
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID contentId) {
        contentService.deleteContent(contentId, TEST_USER_ID);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resurface")
    public ResponseEntity<List<ResurfaceResponse>> resurface(@RequestParam String context) {
        return ResponseEntity.ok(resurfaceService.resurface(context, TEST_USER_ID));
    }
}