package dev.chirag45.breeze_core.dto.response;

import dev.chirag45.breeze_core.entities.ProjectEntity;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID ownerUserId,
        String name,
        String description,
        Instant workSpaceInitializedOn,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProjectResponse from(ProjectEntity project) {
        return new ProjectResponse(
                project.getId(),
                project.getOwner().getId(),
                project.getName(),
                project.getDescription(),
                project.getWorkSpaceInitializedOn(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
