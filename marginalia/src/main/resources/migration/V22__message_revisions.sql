-- message_revisions (MessageRevision): the versions of the text of a part, each with who wrote it (source, isAgent,
-- sourceDetail), an optional label (a pinned version) and, for a text a model wrote, its reasoning. The text and the reasoning
-- are in the extended content (gzipped), _fulltext is the text for the search that includes versions; the other columns are
-- what the history list reads without the text. is_deleted is the trash of the history. message_id is nullable: the cleanup
-- nulls the reference before it purges the part. The rows of the existing parts are made by the app migration
-- PartRevisionMigration
CREATE TABLE message_revisions
(
    id              bigint       not null primary key,
    creation        timestamp,
    is_deleted      boolean      not null,
    modification    timestamp,
    uuid            varchar(36)  not null unique,
    extendedContent blob,
    _fulltext       clob,
    userId          bigint,
    message_id      bigint,
    source          varchar(16)  not null,
    is_agent        boolean      not null,
    sourceDetail    varchar(255),
    label           varchar(255),
    wordCount       integer      not null,
    tokenCount      bigint       not null
);

-- the versions of a part in order (the history list, the newest one); the order is creation, then id
CREATE INDEX ix_message_revisions_message ON message_revisions (message_id, is_deleted, creation);
CREATE INDEX ix_message_revisions_user ON message_revisions (userId, is_deleted);

-- id generator of the entity
CREATE TABLE message_revisions_SEQ
(
    next_val bigint
);
INSERT INTO message_revisions_SEQ (next_val) VALUES (1);
