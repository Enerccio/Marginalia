-- a lorebook or a protocol can be shared: other users see it in "Import from other user" and can import a copy of it (the
-- original stays private to its owner, nobody but the owner can change it). Nothing is shared until its owner says so.
ALTER TABLE lorebooks
    ADD COLUMN shared boolean not null default false;

ALTER TABLE protocols
    ADD COLUMN shared boolean not null default false;

CREATE INDEX ix_lorebooks_shared ON lorebooks (shared, is_deleted);

CREATE INDEX ix_protocols_shared ON protocols (shared, is_deleted);
