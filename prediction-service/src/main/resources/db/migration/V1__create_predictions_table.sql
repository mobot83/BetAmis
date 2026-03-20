CREATE SEQUENCE IF NOT EXISTS predictions_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE predictions (
    id            BIGINT       NOT NULL PRIMARY KEY,
    prediction_id VARCHAR(36)  NOT NULL UNIQUE,
    match_id      VARCHAR(36)  NOT NULL,
    user_id       VARCHAR(255) NOT NULL,
    home_score    INT          NOT NULL,
    away_score    INT          NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    submitted_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_predictions_match_id    ON predictions (match_id);
CREATE INDEX idx_predictions_user_match  ON predictions (user_id, match_id);
