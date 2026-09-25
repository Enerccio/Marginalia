create table main.HTE_ais
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

create table main.HTE_ais_openaicompat
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

create table main.HTE_entries
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

create table main.HTE_lorebooks
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

create table main.HTE_manuscripts
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

create table main.HTE_messages
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

create table main.HTE_protocols
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

create table main.HTE_protocols_chatcompletion
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

create table main.HTE_resources
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

create table main.HTE_settings
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

create table main.HTE_settings1
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

create table main.HTE_settings2
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

create table main.HTE_summaries
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

create table main.HTE_t2e
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

create table main.HTE_tags
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

create table main.HTE_users
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

create table main.HT_ais
(
    id          bigint not null,
    hib_sess_id char   not null,
    primary key (id, hib_sess_id)
);

create table main.HT_protocols
(
    id          bigint not null,
    hib_sess_id char   not null,
    primary key (id, hib_sess_id)
);

create table main.ais
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

create index main.ai_is_deleted_idx
    on main.ais (is_deleted);

create index main.ai_type_idx
    on main.ais (ai_type);

create index main.ai_user_id_ix
    on main.ais (userId);

create table main.ais_SEQ
(
    next_val bigint
);

create table main.ais_openaicompat
(
    apiKey    clob,
    model     clob,
    modelName clob,
    uri       clob,
    id        bigint not null
        primary key
);

create table main.entries
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

create table main.entries_SEQ
(
    next_val bigint
);

create table main.lorebooks
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

create table main.lorebooks_SEQ
(
    next_val bigint
);

create table main.lorebooks_lorebooks
(
    Lorebook_id bigint not null,
    subbooks_id bigint not null
        unique
);

create table main.manuscripts
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

create table main.manuscripts_SEQ
(
    next_val bigint
);

create table main.messages
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

create index main.ix_msg_is_deleted
    on main.messages (is_deleted);

create index main.ix_msg_parent_msg
    on main.messages (parent_id);

create index main.ix_msg_parent_script
    on main.messages (parentScript_id);

create index main.ix_msg_user_id
    on main.messages (userId);

create table main.messages_SEQ
(
    next_val bigint
);

create table main.protocols
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

create index main.protocol_is_deleted_idx
    on main.protocols (is_deleted);

create index main.protocol_user_id_ix
    on main.protocols (userId);

create table main.protocols_SEQ
(
    next_val bigint
);

create table main.protocols_chatcompletion
(
    id bigint not null
        primary key
);

create table main.resources
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

create index main.resource_hash_ix
    on main.resources (hash);

create index main.resource_is_deleted_ix
    on main.resources (is_deleted);

create index main.resource_user_id_ix
    on main.resources (userId);

create table main.resources_SEQ
(
    next_val bigint
);

create table main.settings
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

create table main.settings_SEQ
(
    next_val bigint
);

create table main.summaries
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

create table main.summaries_SEQ
(
    next_val bigint
);

create table main.t2e
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

create index main.ix__tag__id_clazz
    on main.t2e (id, clazz);

create index main.ix__tag__id_clazz_neg
    on main.t2e (id, clazz, negative);

create table main.t2e_SEQ
(
    next_val bigint
);

create table main.tags
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

create table main.tags_SEQ
(
    next_val bigint
);

create table main.users
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

create index main.user_is_deleted_idx
    on main.users (is_deleted);

create index main.user_login_idx
    on main.users (login);

create table main.users_SEQ
(
    next_val bigint
);

