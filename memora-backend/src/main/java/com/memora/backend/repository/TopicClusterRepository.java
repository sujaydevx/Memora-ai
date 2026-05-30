package com.memora.backend.repository;

import com.memora.backend.entity.TopicCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TopicClusterRepository extends JpaRepository<TopicCluster, UUID> {
    List<TopicCluster> findByUserId(UUID userId);
}