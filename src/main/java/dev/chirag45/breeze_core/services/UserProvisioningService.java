package dev.chirag45.breeze_core.services;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.chirag45.breeze_core.entities.UserEntity;
import dev.chirag45.breeze_core.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
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

    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    public UserProvisioningService(
            UserRepository userRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.userRepository = userRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public UserEntity provision(String clerkUserId) {
        if (clerkUserId == null || clerkUserId.isBlank()) {
            throw new IllegalArgumentException("Clerk JWT subject must be present.");
        }

        DataIntegrityViolationException lastPrimaryKeyViolation = null;

        for (int attempt = 1; attempt <= MAX_ID_GENERATION_ATTEMPTS; attempt++) {
            UUID candidateUserId = UuidCreator.getTimeOrderedEpoch();

            try {
                return Objects.requireNonNull(transactionTemplate.execute(status -> {
                    userRepository.upsertByClerkUserId(candidateUserId, clerkUserId);

                    return userRepository.findByClerkUserId(clerkUserId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "User was not available after a successful provisioning upsert."
                            ));
                }));
            } catch (DataIntegrityViolationException exception) {
                if (!isUsersPrimaryKeyViolation(exception)) {
                    throw exception;
                }

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
