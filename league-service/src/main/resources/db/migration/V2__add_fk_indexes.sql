-- PostgreSQL does not auto-create indexes on FK columns.
-- An index on invitations(league_entity_id) is needed for efficient JOIN when loading a league's collections.
-- Note: memberships already has UNIQUE (league_entity_id, user_id), which PostgreSQL implements
-- as a B-tree index with league_entity_id as the leftmost column, covering FK lookups on that column.
CREATE INDEX idx_invitations_league_entity_id ON invitations (league_entity_id);
