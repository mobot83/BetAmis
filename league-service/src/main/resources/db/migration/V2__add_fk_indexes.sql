-- PostgreSQL does not auto-create indexes on FK columns.
-- These indexes are required for efficient JOIN when loading a league's collections.
CREATE INDEX idx_memberships_league_entity_id ON memberships (league_entity_id);
CREATE INDEX idx_invitations_league_entity_id ON invitations (league_entity_id);
