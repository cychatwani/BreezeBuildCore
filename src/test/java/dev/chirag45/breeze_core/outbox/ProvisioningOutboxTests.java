package dev.chirag45.breeze_core.outbox;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.chirag45.breeze_core.dto.translation.ProfileSnapshot;
import dev.chirag45.breeze_core.services.UserProvisioningService;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProvisioningOutboxTests {

    @Autowired
    private UserProvisioningService provisioning;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private OutboxStore outbox;

    @Test
    void provisioningAndOutboxInsertAreAtomicAndIdempotent() {
        String clerkId = "test_" + UuidCreator.getTimeOrderedEpoch();
        String requestId = UuidCreator.getTimeOrderedEpoch().toString();

        MDC.put("requestId", requestId);
        try {
            var first = provisioning.provision(clerkId);
            var second = provisioning.provision(clerkId);

            assertThat(second.getId()).isEqualTo(first.getId());
            assertThat(first.getId().version()).isEqualTo(7);
            var eventIds = jdbc.queryForList("""
                    SELECT id FROM outbox_events
                    WHERE aggregate_id = ? AND event_type = 'USER_PROFILE_SYNC_REQUESTED'
                    """, java.util.UUID.class, first.getId());
            assertThat(eventIds).hasSize(1);
            assertThat(eventIds.getFirst().version()).isEqualTo(7);

            var claim = outbox.claim("test-worker", 60, 5).orElseThrow();
            assertThat(claim.eventId()).isEqualTo(eventIds.getFirst());
            assertThat(claim.requestId()).isEqualTo(requestId);
            assertThat(outbox.complete(claim, new ProfileSnapshot(
                    "test@example.com", "Test User", null))).isTrue();
            assertThat(jdbc.queryForObject("""
                    SELECT profile_sync_status FROM users WHERE id = ?
                    """, String.class, first.getId())).isEqualTo("SYNCED");
        } finally {
            MDC.remove("requestId");
        }
    }
}
