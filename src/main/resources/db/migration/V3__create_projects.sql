CREATE TABLE projects (
    id            uuid          NOT NULL,
    owner_user_id uuid          NOT NULL,
    name          varchar(255)  NOT NULL,
    description   varchar(2000),
    created_at    timestamptz   NOT NULL DEFAULT now(),
    updated_at    timestamptz   NOT NULL DEFAULT now(),
    version       bigint        NOT NULL DEFAULT 0,

    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT fk_projects_owner_user FOREIGN KEY (owner_user_id) REFERENCES users (id)
);

CREATE INDEX idx_projects_owner_user_id ON projects (owner_user_id);
