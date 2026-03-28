CREATE SEQUENCE IF NOT EXISTS scheduled_notifications_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE scheduled_notifications (
    id            BIGINT       NOT NULL PRIMARY KEY,
    match_id      VARCHAR(255) NOT NULL,
    home_team_id  VARCHAR(255) NOT NULL,
    away_team_id  VARCHAR(255) NOT NULL,
    kickoff_at    TIMESTAMPTZ  NOT NULL,
    notify_at     TIMESTAMPTZ  NOT NULL,
    sent          BOOLEAN      NOT NULL DEFAULT FALSE,
    sent_at       TIMESTAMPTZ,
    UNIQUE (match_id, notify_at)
);

CREATE INDEX idx_scheduled_notifications_notify_at_sent ON scheduled_notifications(notify_at, sent);
