CREATE SEQUENCE IF NOT EXISTS notification_preferences_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE notification_preferences (
    id                   BIGINT       NOT NULL PRIMARY KEY,
    user_id              VARCHAR(255) NOT NULL UNIQUE,
    email                VARCHAR(255),
    email_enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    web_push_enabled     BOOLEAN      NOT NULL DEFAULT FALSE,
    unsubscribe_token    VARCHAR(36)  NOT NULL UNIQUE,
    push_subscription_json TEXT
);

CREATE INDEX idx_notification_preferences_unsubscribe_token ON notification_preferences(unsubscribe_token);
