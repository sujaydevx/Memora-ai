package com.memora.backend.service;

import com.memora.backend.dto.response.ContentResponse;
import com.memora.backend.dto.response.SessionResponse;
import com.memora.backend.dto.response.StartSessionResponse;
import com.memora.backend.entity.ContentItem;
import com.memora.backend.entity.StudySession;
import com.memora.backend.entity.Topic;
import com.memora.backend.exception.ContentNotFoundException;
import com.memora.backend.repository.ContentItemRepository;
import com.memora.backend.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final StudySessionRepository studySessionRepository;
    private final ContentItemRepository contentItemRepository;
    private final LLMService llmService;
    private final ContentService contentService;

    public StartSessionResponse startSession(UUID userId) {
        return studySessionRepository.findByUserIdAndEndedAtIsNull(userId)
                .map(existing -> StartSessionResponse.builder()
                        .sessionId(existing.getId())
                        .startedAt(existing.getStartedAt())
                        .build())
                .orElseGet(() -> {
                    StudySession session = StudySession.builder()
                            .userId(userId)
                            .build();
                    StudySession saved = studySessionRepository.save(session);
                    return StartSessionResponse.builder()
                            .sessionId(saved.getId())
                            .startedAt(saved.getStartedAt())
                            .build();
                });
    }

    public SessionResponse endSession(UUID sessionId, UUID userId) {
        StudySession session = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ContentNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new ContentNotFoundException(sessionId);
        }

        session.setEndedAt(OffsetDateTime.now());

        List<ContentItem> items = contentItemRepository.findAllBySessionId(sessionId);

        String heading;
        if (items.isEmpty()) {
            heading = "Empty session";
        } else {
            String summary = items.stream()
                    .map(item -> {
                        String title = item.getPageTitle() != null ? item.getPageTitle() : "";
                        String preview = item.getRawContent() != null
                                ? item.getRawContent().substring(0, Math.min(100, item.getRawContent().length()))
                                : "";
                        return title + " " + preview;
                    })
                    .collect(Collectors.joining(", "));
            heading = llmService.generateSessionHeading(summary);
        }

        session.setHeading(heading);
        StudySession saved = studySessionRepository.save(session);

        return buildSessionResponse(saved, items.size());
    }

    public List<SessionResponse> getSessions(UUID userId) {
        return studySessionRepository.findAllByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(session -> {
                    int count = contentItemRepository.findAllBySessionId(session.getId()).size();
                    return buildSessionResponse(session, count);
                })
                .collect(Collectors.toList());
    }

    public List<ContentResponse> getSessionContent(UUID sessionId, UUID userId) {
        StudySession session = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ContentNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new ContentNotFoundException(sessionId);
        }

        return contentItemRepository.findAllBySessionId(sessionId)
                .stream()
                .map(item -> contentService.buildContentResponsePublic(item))
                .collect(Collectors.toList());
    }

    private SessionResponse buildSessionResponse(StudySession session, int contentCount) {
        return SessionResponse.builder()
                .id(session.getId())
                .heading(session.getHeading())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .contentCount(contentCount)
                .createdAt(session.getCreatedAt())
                .build();
    }
}