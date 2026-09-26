create table HTE_ais
(
    aiType              tinyint,
    deleted             boolean,
    enabledReasoning    boolean,
    maxCompletionTokens integer,
    maxContext          integer,
    needsJailbreak      boolean,
    rn_                 integer not null,
    creation            timestamp,
    id                  bigint,
    modification        timestamp,
    owner_id            bigint,
    hib_sess_id         char    not null,
    uuid                varchar(36),
    extendedContent     blob,
    jailbreak           clob,
    name                clob,
    reasoningEffort     varchar(255),
    primary key (rn_, hib_sess_id)
);

create table HTE_ais_openaicompat
(
    aiType              tinyint,
    deleted             boolean,
    enabledReasoning    boolean,
    maxCompletionTokens integer,
    maxContext          integer,
    needsJailbreak      boolean,
    rn_                 integer not null,
    creation            timestamp,
    id                  bigint,
    modification        timestamp,
    owner_id            bigint,
    hib_sess_id         char    not null,
    uuid                varchar(36),
    apiKey              clob,
    extendedContent     blob,
    jailbreak           clob,
    model               clob,
    modelName           clob,
    name                clob,
    reasoningEffort     varchar(255),
    uri                 clob,
    primary key (rn_, hib_sess_id)
);

create table HTE_entries
(
    deleted         boolean,
    enabled         boolean,
    ordinal         integer,
    rn_             integer not null,
    creation        timestamp,
    id              bigint,
    lorebook_id     bigint,
    modification    timestamp,
    owner_id        bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    extendedContent blob,
    name            clob,
    primary key (rn_, hib_sess_id)
);

create table HTE_lorebooks
(
    deleted         boolean,
    enabled         boolean,
    rn_             integer not null,
    creation        timestamp,
    id              bigint,
    modification    timestamp,
    owner_id        bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    extendedContent blob,
    name            clob,
    primary key (rn_, hib_sess_id)
);

create table HTE_manuscripts
(
    deleted         boolean,
    rn_             integer not null,
    activeLeaf_id   bigint,
    ai_id           bigint,
    creation        timestamp,
    id              bigint,
    lorebook_id     bigint,
    modification    timestamp,
    owner_id        bigint,
    protocol_id     bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    description     varchar(255),
    extendedContent blob,
    name            clob,
    primary key (rn_, hib_sess_id)
);

create table HTE_messages
(
    deleted         boolean,
    edited          boolean,
    rn_             integer not null,
    tokenCount      integer,
    wordCount       integer,
    creation        timestamp,
    id              bigint,
    modification    timestamp,
    owner_id        bigint,
    parentScript_id bigint,
    parent_id       bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    extendedContent blob,
    primary key (rn_, hib_sess_id)
);

create table HTE_protocols
(
    deleted         boolean,
    maxTokens       integer,
    protocolType    tinyint,
    replyTokens     integer,
    rn_             integer not null,
    creation        timestamp,
    id              bigint,
    modification    timestamp,
    owner_id        bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    extendedContent blob,
    name            clob,
    primary key (rn_, hib_sess_id)
);

create table HTE_protocols_chatcompletion
(
    deleted         boolean,
    maxTokens       integer,
    protocolType    tinyint,
    replyTokens     integer,
    rn_             integer not null,
    creation        timestamp,
    id              bigint,
    modification    timestamp,
    owner_id        bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    extendedContent blob,
    name            clob,
    primary key (rn_, hib_sess_id)
);

create table HTE_resources
(
    deleted      boolean,
    rn_          integer not null,
    creation     timestamp,
    id           bigint,
    modification timestamp,
    owner_id     bigint,
    size         bigint,
    hib_sess_id  char    not null,
    uuid         varchar(36),
    hash         clob,
    mimeType     varchar(255),
    originalName clob,
    path         varchar(255),
    primary key (rn_, hib_sess_id)
);

create table HTE_settings
(
    deleted         boolean,
    rn_             integer not null,
    creation        timestamp,
    id              bigint,
    modification    timestamp,
    owner_id        bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    key             varchar(64),
    dtype           varchar(255),
    extendedContent blob,
    primary key (rn_, hib_sess_id)
);

create table HTE_settings1
(
    deleted         boolean,
    rn_             integer not null,
    creation        timestamp,
    id              bigint,
    modification    timestamp,
    owner_id        bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    key             varchar(64),
    dtype           varchar(255),
    extendedContent blob,
    primary key (rn_, hib_sess_id)
);

create table HTE_settings2
(
    deleted         boolean,
    rn_             integer not null,
    creation        timestamp,
    id              bigint,
    modification    timestamp,
    owner_id        bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    key             varchar(64),
    dtype           varchar(255),
    extendedContent blob,
    primary key (rn_, hib_sess_id)
);

create table HTE_summaries
(
    deleted         boolean,
    rn_             integer not null,
    creation        timestamp,
    id              bigint,
    modification    timestamp,
    owner_id        bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    extendedContent blob,
    primary key (rn_, hib_sess_id)
);

create table HTE_t2e
(
    deleted      boolean,
    rn_          integer not null,
    creation     timestamp,
    id           bigint,
    modification timestamp,
    objectId     bigint,
    tag_id       bigint,
    hib_sess_id  char    not null,
    uuid         varchar(36),
    clazz        varchar(255),
    primary key (rn_, hib_sess_id)
);

create table HTE_tags
(
    deleted         boolean,
    rn_             integer not null,
    creation        timestamp,
    id              bigint,
    modification    timestamp,
    owner_id        bigint,
    hib_sess_id     char    not null,
    uuid            varchar(36),
    extendedContent blob,
    value           varchar(255),
    primary key (rn_, hib_sess_id)
);

create table HTE_users
(
    deleted      boolean,
    isAdmin      boolean,
    rn_          integer not null,
    creation     timestamp,
    id           bigint,
    modification timestamp,
    hib_sess_id  char    not null,
    uuid         varchar(36),
    login        varchar(64),
    fullName     clob,
    passwordHash clob,
    savedLogins  clob,
    primary key (rn_, hib_sess_id)
);

create table HT_ais
(
    id          bigint not null,
    hib_sess_id char   not null,
    primary key (id, hib_sess_id)
);

create table HT_protocols
(
    id          bigint not null,
    hib_sess_id char   not null,
    primary key (id, hib_sess_id)
);

create table ais
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
);

create index ai_is_deleted_idx
    on ais (is_deleted);

create index ai_type_idx
    on ais (ai_type);

create index ai_user_id_ix
    on ais (userId);

create table ais_SEQ
(
    next_val bigint
);

create table ais_openaicompat
(
    apiKey    clob,
    model     clob,
    modelName clob,
    uri       clob,
    id        bigint not null
        primary key
);

create table entries
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    enabled         boolean     not null,
    name            clob,
    ordinal         integer     not null,
    userId          bigint,
    lorebook_id     bigint
);

create table entries_SEQ
(
    next_val bigint
);

create table lorebooks
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    enabled         boolean     not null,
    name            clob,
    userId          bigint
);

create table lorebooks_SEQ
(
    next_val bigint
);

create table lorebooks_lorebooks
(
    Lorebook_id bigint not null,
    subbooks_id bigint not null
        unique
);

create table manuscripts
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    description     varchar(255),
    name            clob,
    userId          bigint,
    activeLeaf_id   bigint
        unique,
    ai_id           bigint,
    lorebook_id     bigint,
    protocol_id     bigint
);

create table manuscripts_SEQ
(
    next_val bigint
);

create table messages
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    edited          boolean     not null,
    tokenCount      bigint      not null,
    wordCount       integer     not null,
    userId          bigint,
    parent_id       bigint,
    parentScript_id bigint
);

create index ix_msg_is_deleted
    on messages (is_deleted);

create index ix_msg_parent_msg
    on messages (parent_id);

create index ix_msg_parent_script
    on messages (parentScript_id);

create index ix_msg_user_id
    on messages (userId);

create table messages_SEQ
(
    next_val bigint
);

create table protocols
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    maxTokens       integer     not null,
    name            clob,
    protocolType    tinyint,
    replyTokens     integer     not null,
    userId          bigint
);

create index protocol_is_deleted_idx
    on protocols (is_deleted);

create index protocol_user_id_ix
    on protocols (userId);

create table protocols_SEQ
(
    next_val bigint
);

create table protocols_chatcompletion
(
    id bigint not null
        primary key
);

create table resources
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
);

create index resource_hash_ix
    on resources (hash);

create index resource_is_deleted_ix
    on resources (is_deleted);

create index resource_user_id_ix
    on resources (userId);

create table resources_SEQ
(
    next_val bigint
);

create table settings
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
);

create table settings_SEQ
(
    next_val bigint
);

create table summaries
(
    id              bigint      not null
        primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null
        unique,
    extendedContent blob,
    userId          bigint
);

create table summaries_SEQ
(
    next_val bigint
);

create table t2e
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
);

create index ix__tag__id_clazz
    on t2e (id, clazz);

create index ix__tag__id_clazz_neg
    on t2e (id, clazz, negative);

create table t2e_SEQ
(
    next_val bigint
);

create table tags
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
);

create table tags_SEQ
(
    next_val bigint
);

create table users
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
);

create index user_is_deleted_idx
    on users (is_deleted);

create index user_login_idx
    on users (login);

create table users_SEQ
(
    next_val bigint
);

INSERT INTO ais_SEQ (next_val) VALUES (1);
INSERT INTO entries_SEQ (next_val) VALUES (1);
INSERT INTO lorebooks_SEQ (next_val) VALUES (1);
INSERT INTO manuscripts_SEQ (next_val) VALUES (1);
INSERT INTO messages_SEQ (next_val) VALUES (1);
INSERT INTO protocols_SEQ (next_val) VALUES (1);
INSERT INTO resources_SEQ (next_val) VALUES (1);
INSERT INTO settings_SEQ (next_val) VALUES (1);
INSERT INTO summaries_SEQ (next_val) VALUES (1);
INSERT INTO t2e_SEQ (next_val) VALUES (1);
INSERT INTO tags_SEQ (next_val) VALUES (1);
INSERT INTO users_SEQ (next_val) VALUES (1);