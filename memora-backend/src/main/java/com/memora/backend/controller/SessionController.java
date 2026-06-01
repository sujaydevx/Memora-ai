package com.memora.backend.controller;

import com.memora.backend.dto.response.ContentResponse;
import com.memora.backend.dto.response.SessionResponse;
import com.memora.backend.dto.response.StartSessionResponse;
import com.memora.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    private UUID getCurrentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/start")
    public ResponseEntity<StartSessionResponse> start(Authentication authentication) {
        return ResponseEntity.ok(sessionService.startSession(getCurrentUserId(authentication)));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<SessionResponse> end(@PathVariable UUID id,
                                               Authentication authentication) {
        return ResponseEntity.ok(sessionService.endSession(id, getCurrentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getSessions(Authentication authentication) {
        return ResponseEntity.ok(sessionService.getSessions(getCurrentUserId(authentication)));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<List<ContentResponse>> getContent(@PathVariable UUID id,
                                                            Authentication authentication) {
        return ResponseEntity.ok(sessionService.getSessionContent(id, getCurrentUserId(authentication)));
    }
}