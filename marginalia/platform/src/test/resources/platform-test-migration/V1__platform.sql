-- The tables of the platform, for the platform's own tests only (never shipped: the test-jar has them in
-- platform-test-migration, an application keeps its own Flyway history, Marginalia's is src/main/resources/migration).
-- Generated from Marginalia's schema at the migration below; PlatformSchemaTest (in Marginalia's tests) checks that the
-- columns still match, a change of a platform table there is copied here as a new migration. Hibernate's temporary
-- tables (HTE_*, HT_*) are left out, Hibernate creates them when it needs them.
-- Marginalia schema version: 31

CREATE TABLE users
(
    id           bigint      not null
        primary key,
    creation     timestamp,
    is_deleted   boolean     not null,
    modification timestamp,
    uuid         varchar(36) not null
        unique,
    fullName     clob,
    isAdmin      boolean     not null,
    login        varchar(64)
        unique,
    passwordHash clob,
    savedLogins  clob
, failedLogins int NOT NULL DEFAULT 0, lockedUntil bigint NOT NULL DEFAULT 0);

CREATE TABLE resources
(
    id           bigint      not null
        primary key,
    creation     timestamp,
    is_deleted   boolean     not null,
    modification timestamp,
    uuid         varchar(36) not null
        unique,
    hash         clob,
    mimeType     varchar(255),
    originalName clob,
    path         varchar(255),
    size         bigint      not null,
    userId       bigint
, objectId BIGINT, clazz VARCHAR(255));

CREATE TABLE settings
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    DTYPE           varchar(255),
    key             varchar(64),
    userId          bigint
, _fulltext clob);

CREATE TABLE ais
(
    id                  bigint      not null
        primary key,
    creation            timestamp,
    is_deleted          boolean     not null,
    modification        timestamp,
    uuid                varchar(36) not null
        unique,
    extendedContent     blob,
    ai_type             tinyint,
    enabledReasoning    boolean,
    jailbreak           clob,
    maxCompletionTokens integer,
    maxContext          integer,
    name                clob,
    needsJailbreak      boolean,
    reasoningEffort     varchar(255),
    userId              bigint
, _fulltext clob, shared boolean not null default false);

CREATE TABLE ais_openaicompat
(
    apiKey    clob,
    model     clob,
    modelName clob,
    uri       clob,
    id        bigint not null
        primary key
);

CREATE TABLE ais_extension
(
    extensionId varchar(255) not null,
    id          bigint       not null
        primary key
);

CREATE TABLE "protocols"
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
, _fulltext clob, shared boolean not null default false);

CREATE TABLE protocols_chatcompletion
(
    id bigint not null
        primary key
);

CREATE TABLE tags
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    value           varchar(255),
    userId          bigint
, _fulltext clob);

CREATE TABLE t2e
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    clazz           varchar(255),
    negative        boolean,
    objectId        bigint,
    userId          bigint,
    tag_id          bigint
, _fulltext clob);

CREATE TABLE "usages"
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

CREATE TABLE jobs
(
    id           bigint       not null primary key,
    creation     timestamp,
    is_deleted   boolean      not null,
    modification timestamp,
    uuid         varchar(36)  not null unique,
    userId       bigint,
    ownerLogin   varchar(255),
    kind         varchar(64)  not null,
    label        varchar(255),
    status       varchar(16)  not null,
    startedAt    timestamp    not null,
    endedAt      timestamp,
    callCount    integer      not null default 0,
    failedCalls  integer      not null default 0
);

CREATE TABLE job_calls
(
    id           bigint       not null primary key,
    creation     timestamp,
    is_deleted   boolean      not null,
    modification timestamp,
    uuid         varchar(36)  not null unique,
    job_id       bigint       not null REFERENCES jobs (id),
    callType     varchar(16)  not null,
    backend      varchar(255),
    model        varchar(255),
    status       varchar(16)  not null,
    startedAt    timestamp    not null,
    endedAt      timestamp,
    error        varchar(1000)
);

CREATE TABLE gpus
(
    id           bigint       not null primary key,
    creation     timestamp,
    is_deleted   boolean      not null,
    modification timestamp,
    uuid         varchar(36)  not null unique,
    name         varchar(255) not null
, used_outside boolean not null default 0);

CREATE TABLE gpu_steps
(
    id             bigint        not null primary key,
    creation       timestamp,
    is_deleted     boolean       not null,
    modification   timestamp,
    uuid           varchar(36)   not null unique,
    name           varchar(255)  not null,
    url            varchar(2000) not null,
    method         varchar(16)   not null,
    headers        clob,
    body           clob,
    timeoutSeconds integer       not null default 0,
    waitSeconds    integer       not null default 0
);

CREATE TABLE gpu_locks
(
    id            bigint       not null primary key,
    creation      timestamp,
    is_deleted    boolean      not null,
    modification  timestamp,
    uuid          varchar(36)  not null unique,
    name          varchar(255) not null,
    gpu_id        bigint       not null REFERENCES gpus (id),
    maxConcurrent integer      not null default 0
);

CREATE TABLE gpu_lock_acquire_steps
(
    lock_id  bigint  not null REFERENCES gpu_locks (id),
    step_id  bigint  not null REFERENCES gpu_steps (id),
    ordinal  integer not null,
    primary key (lock_id, ordinal)
);

CREATE TABLE gpu_lock_release_steps
(
    lock_id  bigint  not null REFERENCES gpu_locks (id),
    step_id  bigint  not null REFERENCES gpu_steps (id),
    ordinal  integer not null,
    primary key (lock_id, ordinal)
);

CREATE TABLE permissions
(
    id              bigint      not null primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null unique,
    extendedContent blob,
    _fulltext       clob,
    userId          bigint,
    permissionType  varchar(64) not null,
    holder_id       bigint,
    granted         boolean     not null default false
);

CREATE TABLE api_keys
(
    id              bigint       not null primary key,
    creation        timestamp,
    is_deleted      boolean      not null,
    modification    timestamp,
    uuid            varchar(36)  not null unique,
    extendedContent blob,
    _fulltext       clob,
    userId          bigint,
    name            varchar(255) not null,
    keyPrefix       varchar(32)  not null unique,
    secretHash      varchar(64)  not null
);

CREATE TABLE users_SEQ
(
    next_val bigint
);

CREATE TABLE resources_SEQ
(
    next_val bigint
);

CREATE TABLE settings_SEQ
(
    next_val bigint
);

CREATE TABLE ais_SEQ
(
    next_val bigint
);

CREATE TABLE protocols_SEQ
(
    next_val bigint
);

CREATE TABLE tags_SEQ
(
    next_val bigint
);

CREATE TABLE t2e_SEQ
(
    next_val bigint
);

CREATE TABLE usages_SEQ
(
    next_val bigint
);

CREATE TABLE jobs_SEQ
(
    next_val bigint
);

CREATE TABLE job_calls_SEQ
(
    next_val bigint
);

CREATE TABLE gpus_SEQ
(
    next_val bigint
);

CREATE TABLE gpu_steps_SEQ
(
    next_val bigint
);

CREATE TABLE gpu_locks_SEQ
(
    next_val bigint
);

CREATE TABLE permissions_SEQ
(
    next_val bigint
);

CREATE TABLE api_keys_SEQ
(
    next_val bigint
);

INSERT INTO users_SEQ (next_val) VALUES (1);

INSERT INTO resources_SEQ (next_val) VALUES (1);

INSERT INTO settings_SEQ (next_val) VALUES (1);

INSERT INTO ais_SEQ (next_val) VALUES (1);

INSERT INTO protocols_SEQ (next_val) VALUES (1);

INSERT INTO tags_SEQ (next_val) VALUES (1);

INSERT INTO t2e_SEQ (next_val) VALUES (1);

INSERT INTO usages_SEQ (next_val) VALUES (1);

INSERT INTO jobs_SEQ (next_val) VALUES (1);

INSERT INTO job_calls_SEQ (next_val) VALUES (1);

INSERT INTO gpus_SEQ (next_val) VALUES (1);

INSERT INTO gpu_steps_SEQ (next_val) VALUES (1);

INSERT INTO gpu_locks_SEQ (next_val) VALUES (1);

INSERT INTO permissions_SEQ (next_val) VALUES (1);

INSERT INTO api_keys_SEQ (next_val) VALUES (1);

CREATE INDEX user_is_deleted_idx
    on users (is_deleted);

CREATE INDEX ix_resources_object ON resources (clazz, objectId);

CREATE INDEX ix_resources_user_hash ON resources (userId, hash);

CREATE INDEX ix_resources_user_list ON resources (userId, is_deleted, creation);

CREATE INDEX resource_is_deleted_ix
    on resources (is_deleted);

CREATE INDEX ix_settings_key_user ON settings (key, userId);

CREATE INDEX ai_is_deleted_idx
    on ais (is_deleted);

CREATE INDEX ai_type_idx
    on ais (ai_type);

CREATE INDEX ix_ais_shared ON ais (shared, is_deleted);

CREATE INDEX ix_ais_user ON ais (userId, is_deleted);

CREATE INDEX ix_protocols_shared ON protocols (shared, is_deleted);

CREATE INDEX ix_protocols_user ON protocols (userId, is_deleted);

CREATE INDEX protocol_is_deleted_idx ON protocols (is_deleted);

CREATE INDEX ix_tags_user ON tags (userId, is_deleted);

CREATE INDEX ix_t2e_object ON t2e (objectId, clazz, negative);

CREATE INDEX ix_t2e_tag ON t2e (tag_id, clazz, negative);

CREATE INDEX ix_usages_user ON usages (userId, is_deleted);

CREATE UNIQUE INDEX ux_usages_ai_user ON usages (ai_id, userId);

CREATE INDEX ix_jobs_started ON jobs (startedAt);

CREATE INDEX ix_jobs_user ON jobs (userId, startedAt);

CREATE INDEX ix_job_calls_job ON job_calls (job_id, startedAt);

CREATE INDEX ix_gpu_locks_gpu ON gpu_locks (gpu_id);

CREATE INDEX ix_permissions_holder ON permissions (holder_id, permissionType, is_deleted);

CREATE INDEX ix_api_keys_owner ON api_keys (userId, is_deleted);
