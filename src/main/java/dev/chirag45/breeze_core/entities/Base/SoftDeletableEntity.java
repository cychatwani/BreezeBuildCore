package dev.chirag45.breeze_core.entities.Base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Adds soft-deletion state without imposing query filtering behavior.
 */
@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class SoftDeletableEntity extends AuditableEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
