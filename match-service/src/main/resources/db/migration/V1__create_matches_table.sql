CREATE TABLE matches (
    id              BIGSERIAL PRIMARY KEY,
    match_id        VARCHAR(36)  NOT NULL UNIQUE,
    external_id     BIGINT       UNIQUE,
    home_team_id    VARCHAR(255) NOT NULL,
    away_team_id    VARCHAR(255) NOT NULL,
    home_team_score INT          NOT NULL DEFAULT 0,
    away_team_score INT          NOT NULL DEFAULT 0,
    status          VARCHAR(20)  NOT NULL
);
