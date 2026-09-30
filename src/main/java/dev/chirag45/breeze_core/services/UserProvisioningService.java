package dev.chirag45.breeze_core.services;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.chirag45.breeze_core.entities.UserEntity;
import dev.chirag45.breeze_core.outbox.OutboxStore;
import dev.chirag45.breeze_core.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserProvisioningService {

    private static final int MAX_ID_GENERATION_ATTEMPTS = 3;
    private static final Logger log = LoggerFactory.getLogger(UserProvisioningService.class);

    private final UserRepository userRepository;
    private final OutboxStore outboxStore;
    private final TransactionTemplate transactionTemplate;

    public UserProvisioningService(
            UserRepository userRepository,
            OutboxStore outboxStore,
            PlatformTransactionManager transactionManager
    ) {
        this.userRepository = userRepository;
        this.outboxStore = outboxStore;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public UserEntity provision(String clerkUserId) {
        if (clerkUserId == null || clerkUserId.isBlank()) {
            throw new IllegalArgumentException("Clerk JWT subject must be present.");
        }

        log.info("user_provisioning_started");

        DataIntegrityViolationException lastPrimaryKeyViolation = null;

        for (int attempt = 1; attempt <= MAX_ID_GENERATION_ATTEMPTS; attempt++) {
            log.trace("user_provisioning_attempt_started attempt={}", attempt);
            UUID candidateUserId = UuidCreator.getTimeOrderedEpoch();

            try {
                return Objects.requireNonNull(transactionTemplate.execute(status -> {
                    log.trace("user_provisioning_transaction_started");
                    userRepository.upsertByClerkUserId(candidateUserId, clerkUserId);

                    log.debug("user_provisioning_upsert_completed");
                    log.trace("user_provisioning_user_lookup_started");

                    UserEntity user = userRepository.findByClerkUserId(clerkUserId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "User was not available after a successful provisioning upsert."
                            ));
                    outboxStore.requestInitialProfileSync(user.getId());
                    log.debug("user_profile_sync_requested userId={}", user.getId());
                    return user;
                }));
            } catch (DataIntegrityViolationException exception) {
                if (!isUsersPrimaryKeyViolation(exception)) {
                    log.warn(
                            "user_provisioning_data_integrity_failure exceptionType={}",
                            exception.getClass().getSimpleName()
                    );
                    throw exception;
                }

                log.warn("user_provisioning_primary_key_collision attempt={}", attempt);
                lastPrimaryKeyViolation = exception;
            }
        }

        throw new IllegalStateException(
                "UUIDv7 collision on users primary key after "
                        + MAX_ID_GENERATION_ATTEMPTS
                        + " attempts. Investigate UUID generation immediately.",
                lastPrimaryKeyViolation
        );
    }

    private boolean isUsersPrimaryKeyViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && "pk_users".equals(constraintViolation.getConstraintName())) {
                return true;
            }

            if (cause instanceof SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())
                    && sqlException.getMessage() != null
                    && sqlException.getMessage().contains("pk_users")) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}
