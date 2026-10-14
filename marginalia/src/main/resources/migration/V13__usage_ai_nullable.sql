-- purging an AI nulls the references to it (CleanupReference OWNED_BY) before the row goes, so usages.ai_id must allow
-- NULL (SQLite can't drop NOT NULL, rebuild the table)
CREATE TABLE usages_new
(
    id                    bigint      not null primary key,
    creation              timestamp,
    is_deleted            boolean     not null,
    modification          timestamp,
    uuid                  varchar(36) not null unique,
    userId                bigint,
    ai_id                 bigint,
    prefillTokens         bigint      not null default 0,
    generatedTokens       bigint      not null default 0,
    requests              bigint      not null default 0,
    lastUsed              timestamp,
    periodPrefillTokens   bigint      not null default 0,
    periodGeneratedTokens bigint      not null default 0
);

INSERT INTO usages_new (id, creation, is_deleted, modification, uuid, userId, ai_id, prefillTokens, generatedTokens,
                        requests, lastUsed, periodPrefillTokens, periodGeneratedTokens)
SELECT id, creation, is_deleted, modification, uuid, userId, ai_id, prefillTokens, generatedTokens,
       requests, lastUsed, periodPrefillTokens, periodGeneratedTokens
FROM usages;

DROP TABLE usages;
ALTER TABLE usages_new RENAME TO usages;

CREATE UNIQUE INDEX ux_usages_ai_user ON usages (ai_id, userId);
CREATE INDEX ix_usages_user ON usages (userId, is_deleted);
