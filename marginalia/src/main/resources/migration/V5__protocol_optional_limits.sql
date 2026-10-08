-- protocol limits are optional overrides of the AI limits: allow NULL (SQLite can't drop NOT NULL, rebuild the table)
-- and turn the old "not set" values (0, negative) into NULL
CREATE TABLE protocols_new
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    maxTokens       integer,
    name            clob,
    protocolType    tinyint,
    replyTokens     integer,
    userId          bigint
);

INSERT INTO protocols_new (id, creation, is_deleted, modification, uuid, extendedContent, maxTokens, name,
                           protocolType, replyTokens, userId)
SELECT id,
       creation,
       is_deleted,
       modification,
       uuid,
       extendedContent,
       CASE WHEN maxTokens > 0 THEN maxTokens END,
       name,
       protocolType,
       CASE WHEN replyTokens > 0 THEN replyTokens END,
       userId
FROM protocols;

DROP TABLE protocols;

ALTER TABLE protocols_new RENAME TO protocols;

CREATE INDEX protocol_is_deleted_idx ON protocols (is_deleted);

CREATE INDEX protocol_user_id_ix ON protocols (userId);
