-- tokens used by a user with an AI: one row per AI and user, so an AI shared by an administrator has a usage of its
-- own for everyone who uses it
CREATE TABLE usages
(
    id              bigint      not null primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null unique,
    userId          bigint,
    ai_id           bigint      not null,
    prefillTokens    bigint      not null default 0,
    generatedTokens bigint      not null default 0,
    requests        bigint      not null default 0,
    lastUsed        timestamp
);

CREATE UNIQUE INDEX ux_usages_ai_user ON usages (ai_id, userId);
CREATE INDEX ix_usages_user ON usages (userId, is_deleted);

-- id generator of the entity
CREATE TABLE usages_SEQ
(
    next_val bigint
);
INSERT INTO usages_SEQ (next_val) VALUES (1);
