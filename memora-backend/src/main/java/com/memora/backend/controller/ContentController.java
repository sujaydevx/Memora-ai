package com.memora.backend.controller;

import com.memora.backend.dto.request.SaveContentRequest;
import com.memora.backend.dto.response.ContentResponse;
import com.memora.backend.dto.response.ResurfaceResponse;
import com.memora.backend.service.ContentService;
import com.memora.backend.service.ResurfaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final ResurfaceService resurfaceService;

    private UUID getCurrentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/save")
    public ResponseEntity<ContentResponse> save(@RequestBody @Valid SaveContentRequest request,
                                                Authentication authentication) {
        return ResponseEntity.status(201).body(contentService.saveContent(request, getCurrentUserId(authentication)));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ContentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        return ResponseEntity.ok(contentService.getAllContent(getCurrentUserId(authentication), page, size).getContent());
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID contentId,
                                       Authentication authentication) {
        contentService.deleteContent(contentId, getCurrentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resurface")
    public ResponseEntity<List<ResurfaceResponse>> resurface(@RequestParam String context,
                                                             Authentication authentication) {
        return ResponseEntity.ok(resurfaceService.resurface(context, getCurrentUserId(authentication)));
    }
}