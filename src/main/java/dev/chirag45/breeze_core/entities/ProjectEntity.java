package dev.chirag45.breeze_core.entities;

import dev.chirag45.breeze_core.entities.Base.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

@Entity
@Getter
@Table(
        name = "projects",
        indexes = @Index(name = "idx_projects_owner_user_id", columnList = "owner_user_id")
)
public class ProjectEntity extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "owner_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_projects_owner_user")
    )
    private UserEntity owner;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "work_space_initialized_on", nullable = true)
    private Instant workSpaceInitializedOn;

    protected ProjectEntity() {
    }

    public ProjectEntity(UserEntity owner, String name, String description) {
        this.owner = owner;
        this.name = name;
        this.description = description;
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
