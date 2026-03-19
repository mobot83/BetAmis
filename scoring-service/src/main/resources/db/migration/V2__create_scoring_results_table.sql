CREATE SEQUENCE IF NOT EXISTS scoring_results_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE scoring_results (
    id            BIGINT      NOT NULL PRIMARY KEY,
    prediction_id VARCHAR(36) NOT NULL UNIQUE,
    match_id      VARCHAR(36) NOT NULL,
    user_id       VARCHAR(36) NOT NULL,
    points        INT         NOT NULL
);
