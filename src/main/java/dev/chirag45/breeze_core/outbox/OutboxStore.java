package dev.chirag45.breeze_core.outbox;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.chirag45.breeze_core.dto.translation.ProfileSnapshot;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OutboxStore {

    private final JdbcTemplate jdbc;

    public OutboxStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void requestInitialProfileSync(UUID userId) {
        jdbc.update("""
                INSERT INTO outbox_events (id, event_type, aggregate_id, payload)
                VALUES (?, 'USER_PROFILE_SYNC_REQUESTED', ?, jsonb_build_object('requestId', ?::text))
                ON CONFLICT (event_type, aggregate_id) DO NOTHING
                """, UuidCreator.getTimeOrderedEpoch(), userId, MDC.get("requestId"));
    }

    @Transactional
    public Optional<Claim> claim(String claimToken, int leaseSeconds, int maxAttempts) {
        return jdbc.query("""
                WITH candidate AS (
                    SELECT id FROM outbox_events
                    WHERE (status IN ('PENDING', 'FAILED') AND next_attempt_at <= now() AND attempts < ?)
                       OR (status = 'PROCESSING' AND locked_until < now())
                    ORDER BY created_at, id
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                )
                UPDATE outbox_events AS event
                SET status = 'PROCESSING', attempts = event.attempts + 1,
                    locked_by = ?, locked_until = now() + (? * interval '1 second'), updated_at = now()
                FROM candidate WHERE event.id = candidate.id
                RETURNING event.id, event.aggregate_id, event.attempts,
                    event.payload ->> 'requestId' AS request_id
                """, (rs, rowNum) -> new Claim(
                rs.getObject("id", UUID.class),
                rs.getObject("aggregate_id", UUID.class),
                rs.getInt("attempts"), claimToken, rs.getString("request_id")
        ), maxAttempts, claimToken, leaseSeconds).stream().findFirst();
    }

    @Transactional
    public boolean complete(Claim claim, ProfileSnapshot profile) {
        int updated = jdbc.update("""
                UPDATE outbox_events SET status = 'COMPLETED', locked_by = NULL, locked_until = NULL,
                    last_error = NULL, completed_at = now(), updated_at = now()
                WHERE id = ? AND status = 'PROCESSING' AND locked_by = ?
                """, claim.eventId(), claim.claimToken());
        if (updated == 0) {
            return false;
        }
        jdbc.update("""
                UPDATE users SET email = ?, display_name = ?, avatar_url = ?,
                    profile_sync_status = 'SYNCED', profile_synced_at = now(), updated_at = now()
                WHERE id = ?
                """, profile.email(), profile.displayName(), profile.avatarUrl(), claim.userId());
        return true;
    }

    @Transactional
    public boolean fail(Claim claim, String errorCode, int maxAttempts, int retryDelaySeconds) {
        boolean exhausted = claim.attempts() >= maxAttempts;
        int updated = jdbc.update("""
                UPDATE outbox_events SET status = ?, locked_by = NULL, locked_until = NULL,
                    last_error = ?, next_attempt_at = now() + (? * interval '1 second'), updated_at = now()
                WHERE id = ? AND status = 'PROCESSING' AND locked_by = ?
                """, exhausted ? "DEAD" : "FAILED", errorCode, retryDelaySeconds,
                claim.eventId(), claim.claimToken());
        if (updated == 0) {
            return false;
        }
        if (exhausted) {
            jdbc.update("""
                    UPDATE users SET profile_sync_status = 'FAILED', updated_at = now()
                    WHERE id = ? AND profile_sync_status <> 'SYNCED'
                    """, claim.userId());
        }
        return true;
    }

    public record Claim(UUID eventId, UUID userId, int attempts, String claimToken, String requestId) { }

}
