CREATE SEQUENCE IF NOT EXISTS user_league_memberships_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE user_league_memberships (
    id         BIGINT       NOT NULL PRIMARY KEY,
    user_id    VARCHAR(255) NOT NULL,
    league_id  VARCHAR(255) NOT NULL,
    joined_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_user_league UNIQUE (user_id, league_id)
);

CREATE INDEX idx_user_league_memberships_user_id ON user_league_memberships (user_id);
