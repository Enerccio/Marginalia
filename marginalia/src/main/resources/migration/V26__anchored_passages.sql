-- annotations are now one of the kinds of an anchored passage (AnchoredPassage, the table is shared, "kind" is the discriminator):
-- the editorial note of the author (EDITORIAL, every existing row) and the passages of the extensions (PASSAGE, passageKind says
-- whose); a passage has no status and no source, so those two columns must allow NULL (SQLite can't drop NOT NULL, rebuild the
-- table and its indexes)
CREATE TABLE annotations_new
(
    id              bigint      not null primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null unique,
    extendedContent blob,
    _fulltext       clob,
    userId          bigint,
    part_id         bigint,
    kind            varchar(16) not null default 'EDITORIAL',
    passageKind     varchar(64),
    status          varchar(16),
    source          varchar(16),
    startOffset     integer,
    endOffset       integer
);

INSERT INTO annotations_new (id, creation, is_deleted, modification, uuid, extendedContent, _fulltext, userId, part_id, kind,
                             passageKind, status, source, startOffset, endOffset)
SELECT id, creation, is_deleted, modification, uuid, extendedContent, _fulltext, userId, part_id, 'EDITORIAL',
       NULL, status, source, startOffset, endOffset
FROM annotations;

DROP TABLE annotations;
ALTER TABLE annotations_new RENAME TO annotations;

CREATE INDEX ix_annotations_part ON annotations (part_id, is_deleted, status);
CREATE INDEX ix_annotations_user ON annotations (userId, is_deleted);
-- the passages of a kind (an extension's), by part
CREATE INDEX ix_annotations_kind ON annotations (passageKind, part_id, is_deleted);
