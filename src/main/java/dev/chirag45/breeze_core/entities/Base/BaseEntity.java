package dev.chirag45.breeze_core.entities.Base;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Foundation for persisted aggregates with an application-generated UUIDv7.
 */
@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    protected void assignId() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
