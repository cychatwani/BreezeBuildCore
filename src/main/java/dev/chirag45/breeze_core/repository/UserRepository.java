package dev.chirag45.breeze_core.repository;

import dev.chirag45.breeze_core.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByClerkUserId(String clerkUserId);

    boolean existsByClerkUserId(String clerkUserId);

    @Modifying
    @Query(value = """
            INSERT INTO users (
                id,
                clerk_user_id,
                status,
                profile_sync_status,
                created_at,
                updated_at
            )
            VALUES (
                :id,
                :clerkUserId,
                'ACTIVE',
                'PENDING',
                now(),
                now()
            )
            ON CONFLICT (clerk_user_id)
            DO UPDATE SET updated_at = now()
            """, nativeQuery = true)
    int upsertByClerkUserId(
            @Param("id") UUID id,
            @Param("clerkUserId") String clerkUserId
    );
}
