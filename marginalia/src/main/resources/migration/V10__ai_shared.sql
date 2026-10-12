-- an AI owned by an administrator can be shared: every user can use it in their books (but not see or edit it)
ALTER TABLE ais
    ADD COLUMN shared boolean not null default false;

CREATE INDEX ix_ais_shared ON ais (shared, is_deleted);
