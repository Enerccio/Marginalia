-- indexes matching the actual queries; indexes live only in migrations (no @Table(indexes) on the entities)

-- tag relations: the old indexes started with the primary key and were never used
DROP INDEX IF EXISTS ix__tag__id_clazz;
DROP INDEX IF EXISTS ix__tag__id_clazz_neg;
-- tags of an object (findTagsForObject, fillEntries) and objects of a tag (findObjectIdsForTag, filters)
CREATE INDEX ix_t2e_object ON t2e (objectId, clazz, negative);
CREATE INDEX ix_t2e_tag ON t2e (tag_id, clazz, negative);

-- duplicate of the unique constraint on users.login
DROP INDEX IF EXISTS user_login_idx;

-- owner lookups (list / find by uuid of an owned entity) always filter is_deleted too
DROP INDEX IF EXISTS ai_user_id_ix;
CREATE INDEX ix_ais_user ON ais (userId, is_deleted);
DROP INDEX IF EXISTS protocol_user_id_ix;
CREATE INDEX ix_protocols_user ON protocols (userId, is_deleted);
CREATE INDEX ix_manuscripts_user ON manuscripts (userId, is_deleted);
CREATE INDEX ix_lorebooks_user ON lorebooks (userId, is_deleted);
CREATE INDEX ix_tags_user ON tags (userId, is_deleted);

-- resources are looked up by owner and hash (deduplication of uploads)
DROP INDEX IF EXISTS resource_user_id_ix;
DROP INDEX IF EXISTS resource_hash_ix;
CREATE INDEX ix_resources_user_hash ON resources (userId, hash);

-- entries of a lorebook in order (every generation)
CREATE INDEX ix_entries_lorebook ON entries (lorebook_id, ordinal);

-- parts of a book in order
DROP INDEX IF EXISTS ix_msg_parent_script;
CREATE INDEX ix_msg_parent_script ON messages (parentScript_id, creation);
-- parts using a summary (cleanup references)
CREATE INDEX ix_msg_summary ON messages (summary_id);

-- settings by key and owner
CREATE INDEX ix_settings_key_user ON settings (key, userId);
