package com.memora.backend.controller;

import com.memora.backend.dto.ContentItemDto;
import com.memora.backend.entity.ContentItem;
import com.memora.backend.service.ContentItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentItemController {

    private final ContentItemService contentItemService;

    @PostMapping
    public ResponseEntity<ContentItem> save(@RequestBody ContentItemDto dto,
                                            Authentication authentication) {
        return ResponseEntity.ok(contentItemService.save(dto, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<ContentItem>> getAll(Authentication authentication,
                                                    Pageable pageable) {
        return ResponseEntity.ok(contentItemService.getAll(authentication.getName(), pageable));
    }
}