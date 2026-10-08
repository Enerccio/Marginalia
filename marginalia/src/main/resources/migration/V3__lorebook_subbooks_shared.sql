-- lorebook can be subbook of multiple lorebooks, drop unique constraint on subbooks_id
CREATE TABLE lorebooks_lorebooks_new
(
    Lorebook_id bigint not null,
    subbooks_id bigint not null,
    primary key (Lorebook_id, subbooks_id)
);

INSERT OR IGNORE INTO lorebooks_lorebooks_new (Lorebook_id, subbooks_id)
SELECT Lorebook_id, subbooks_id FROM lorebooks_lorebooks;

DROP TABLE lorebooks_lorebooks;

ALTER TABLE lorebooks_lorebooks_new RENAME TO lorebooks_lorebooks;

CREATE INDEX ix_lorebooks_lorebooks_subbooks ON lorebooks_lorebooks (subbooks_id);
