package com.memora.backend.service;

import com.memora.backend.dto.ContentItemDto;
import com.memora.backend.entity.ContentItem;
import com.memora.backend.entity.ContentType;
import com.memora.backend.entity.User;
import com.memora.backend.repository.ContentItemRepository;
import com.memora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentItemService {

    private final ContentItemRepository contentItemRepository;
    private final UserRepository userRepository;

    private String generateChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generating checksum", e);
        }
    }

    public ContentItem save(ContentItemDto dto, String email) {
        String emailHash = hashEmail(email);
        User user = userRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String checksum = generateChecksum(dto.getRawContent() != null ? dto.getRawContent() : dto.getSourceUrl());

        // Duplicate detection
        if (contentItemRepository.findByUserIdAndChecksum(user.getId(), checksum).isPresent()) {
            throw new RuntimeException("Content already saved");
        }

        ContentItem item = ContentItem.builder()
                .userId(user.getId())
                .type(ContentType.valueOf(dto.getType().toUpperCase()))
                .rawContent(dto.getRawContent())
                .sourceUrl(dto.getSourceUrl())
                .pageTitle(dto.getPageTitle())
                .mimeType(dto.getMimeType())
                .checksum(checksum)
                .build();

        return contentItemRepository.save(item);
    }

    public Page<ContentItem> getAll(String email, Pageable pageable) {
        User user = userRepository.findByEmailHash(hashEmail(email))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return contentItemRepository.findAllByUserId(user.getId(), pageable);
    }

    private String hashEmail(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.toLowerCase().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing email", e);
        }
    }
}