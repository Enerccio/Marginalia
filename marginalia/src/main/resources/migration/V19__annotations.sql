 -- annotations (Annotation): notes of the author, a model, an agent or an extension about a passage of a part. The passage is
-- a range of offsets in the text of the part (startOffset, endOffset; both null for a note about the whole part); the
-- note, the quoted passage and its context are in the extended content. part_id is nullable: the cleanup nulls the
-- reference before it purges the owner
CREATE TABLE annotations
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
    status          varchar(16) not null,
    source          varchar(16) not null,
    startOffset     integer,
    endOffset       integer
);

-- the annotations of a part (the story view), of a book by status (the panel) go through the part
CREATE INDEX ix_annotations_part ON annotations (part_id, is_deleted, status);
CREATE INDEX ix_annotations_user ON annotations (userId, is_deleted);

-- id generator of the entity
CREATE TABLE annotations_SEQ
(
    next_val bigint
);
INSERT INTO annotations_SEQ (next_val) VALUES (1);
