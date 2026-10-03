package dev.chirag45.breeze_core.repository;

import dev.chirag45.breeze_core.entities.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    List<ProjectEntity> findAllByOwner_ClerkUserIdOrderByCreatedAtDesc(String clerkUserId);

    Optional<ProjectEntity> findByIdAndOwner_ClerkUserId(UUID id, String clerkUserId);
}
