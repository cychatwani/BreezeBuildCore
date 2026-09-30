CREATE TABLE outbox_events (
    id uuid PRIMARY KEY,
    event_type varchar(64) NOT NULL,
    aggregate_id uuid NOT NULL,
    payload jsonb NOT NULL,
    status varchar(32) NOT NULL DEFAULT 'PENDING',
    attempts integer NOT NULL DEFAULT 0,
    next_attempt_at timestamptz NOT NULL DEFAULT now(),
    locked_by varchar(255),
    locked_until timestamptz,
    last_error text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    completed_at timestamptz,
    CONSTRAINT ck_outbox_events_type CHECK (event_type IN ('USER_PROFILE_SYNC_REQUESTED')),
    CONSTRAINT ck_outbox_events_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'DEAD')),
    CONSTRAINT ck_outbox_events_attempts CHECK (attempts >= 0),
    CONSTRAINT uk_outbox_events_initial_profile_sync UNIQUE (event_type, aggregate_id)
);

CREATE INDEX idx_outbox_events_polling ON outbox_events(status, next_attempt_at, created_at);
CREATE INDEX idx_outbox_events_aggregate ON outbox_events(aggregate_id);
CREATE INDEX idx_outbox_events_expired_claims ON outbox_events(locked_until)
    WHERE status = 'PROCESSING';
