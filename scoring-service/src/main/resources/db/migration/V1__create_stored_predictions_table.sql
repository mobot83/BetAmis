CREATE SEQUENCE IF NOT EXISTS stored_predictions_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE stored_predictions (
    id                   BIGINT       NOT NULL PRIMARY KEY,
    prediction_id        VARCHAR(36)  NOT NULL UNIQUE,
    match_id             VARCHAR(36)  NOT NULL,
    user_id              VARCHAR(36)  NOT NULL,
    predicted_home_score INT          NOT NULL,
    predicted_away_score INT          NOT NULL
);

CREATE INDEX idx_stored_predictions_match_id ON stored_predictions (match_id);
