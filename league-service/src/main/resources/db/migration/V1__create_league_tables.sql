CREATE SEQUENCE IF NOT EXISTS leagues_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS memberships_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS invitations_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE leagues (
    id          BIGINT          NOT NULL PRIMARY KEY,
    league_id   VARCHAR(36)     NOT NULL UNIQUE,
    name        VARCHAR(255)    NOT NULL,
    owner_id    VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL
);

CREATE TABLE memberships (
    id                  BIGINT          NOT NULL PRIMARY KEY,
    league_entity_id    BIGINT          NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    user_id             VARCHAR(255)    NOT NULL,
    joined_at           TIMESTAMPTZ     NOT NULL,
    UNIQUE (league_entity_id, user_id)
);

CREATE TABLE invitations (
    id                  BIGINT          NOT NULL PRIMARY KEY,
    league_entity_id    BIGINT          NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    code                VARCHAR(6)      NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL,
    expires_at          TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_invitations_code ON invitations (code);

