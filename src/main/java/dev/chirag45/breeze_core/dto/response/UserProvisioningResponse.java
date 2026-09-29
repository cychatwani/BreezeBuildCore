package dev.chirag45.breeze_core.dto.response;

import dev.chirag45.breeze_core.entities.UserEntity;

import java.util.UUID;

public record UserProvisioningResponse(
        UUID userId,
        String clerkUserId
) {

    public static UserProvisioningResponse from(UserEntity user) {
        return new UserProvisioningResponse(
                user.getId(),
                user.getClerkUserId()
        );
    }
}
