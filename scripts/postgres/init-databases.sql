-- =============================================================
-- BetAmis — Initialisation des bases de données PostgreSQL
-- Instance unique, 4 bases (une par service)
-- L'utilisateur "betamis" est créé par POSTGRES_USER dans Docker
-- "league_db" est créée par POSTGRES_DB dans Docker
-- =============================================================

-- ── match_db ─────────────────────────────────────────────────
CREATE DATABASE match_db
    WITH OWNER = betamis
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

COMMENT ON DATABASE match_db IS 'Base de données du match-service';

-- ── prediction_db ─────────────────────────────────────────────
CREATE DATABASE prediction_db
    WITH OWNER = betamis
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

COMMENT ON DATABASE prediction_db IS 'Base de données du prediction-service';

-- ── scoring_db ────────────────────────────────────────────────
CREATE DATABASE scoring_db
    WITH OWNER = betamis
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

COMMENT ON DATABASE scoring_db IS 'Base de données du scoring-service';

-- =============================================================
-- Récapitulatif des bases disponibles :
--   league_db     → league-service    (port 5432)
--   match_db      → match-service     (port 5432)
--   prediction_db → prediction-service (port 5432)
--   scoring_db    → scoring-service   (port 5432)
-- Utilisateur unique : betamis / betamis_secret
-- =============================================================

