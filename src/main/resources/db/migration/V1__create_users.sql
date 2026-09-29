CREATE TABLE users (
    id                  uuid         NOT NULL,
    clerk_user_id       varchar(255) NOT NULL,
    email               varchar(320),
    display_name        varchar(255),
    avatar_url          varchar(2048),
    status              varchar(32)  NOT NULL,
    profile_sync_status varchar(32)  NOT NULL,
    profile_synced_at   timestamptz,
    created_at          timestamptz   NOT NULL DEFAULT now(),
    updated_at          timestamptz   NOT NULL DEFAULT now(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_clerk_user_id UNIQUE (clerk_user_id),
    CONSTRAINT ck_users_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED')),
    CONSTRAINT ck_users_profile_sync_status
        CHECK (profile_sync_status IN ('PENDING', 'SYNCED', 'FAILED'))
);

CREATE INDEX idx_users_profile_sync_status
    ON users (profile_sync_status);
