package dev.chirag45.breeze_core.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.chirag45.breeze_core.enums.ProfileSyncStatus;
import dev.chirag45.breeze_core.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_clerk_user_id",
                columnNames = "clerk_user_id"
        ),
        indexes = @Index(
                name = "idx_users_profile_sync_status",
                columnList = "profile_sync_status"
        )
)
public class UserEntity {

    @Id
    private UUID id;

    @Column(name = "clerk_user_id", nullable = false, updatable = false, length = 255)
    private String clerkUserId;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_sync_status", nullable = false, length = 32)
    private ProfileSyncStatus profileSyncStatus = ProfileSyncStatus.PENDING;

    @Column(name = "profile_synced_at")
    private Instant profileSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntity() {
    }

    public UserEntity(String clerkUserId) {
        this.clerkUserId = clerkUserId;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

}
