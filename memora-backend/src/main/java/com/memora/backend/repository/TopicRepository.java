package com.memora.backend.repository;

import com.memora.backend.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findAllByUserId(UUID userId);
    Optional<Topic> findByUserIdAndName(UUID userId, String name);
    List<Topic> findAllByUserIdAndParentTopicIdIsNull(UUID userId);
}