-- GPU cooperation (Gpu, GpuLock, GpuStep): set up by the administrator, picked by every user for an AI or a backend of an extension.
-- A GPU is one machine that holds one model at a time; a lock is a way to use it (what its acquire steps load); a step is an HTTP request
-- (load a model, unload it, stop a container) that several locks can share. Not owned by a user: they belong to the installation.

CREATE TABLE gpus
(
    id           bigint       not null primary key,
    creation     timestamp,
    is_deleted   boolean      not null,
    modification timestamp,
    uuid         varchar(36)  not null unique,
    name         varchar(255) not null
);

CREATE TABLE gpus_SEQ
(
    next_val bigint
);
INSERT INTO gpus_SEQ (next_val) VALUES (1);

-- headers are encrypted with the key of the installation (they usually hold a bearer key)
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

CREATE TABLE gpu_steps_SEQ
(
    next_val bigint
);
INSERT INTO gpu_steps_SEQ (next_val) VALUES (1);

-- maxConcurrent: how many jobs of the lock run at once on the GPU, 0 for no limit
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

CREATE INDEX ix_gpu_locks_gpu ON gpu_locks (gpu_id);

CREATE TABLE gpu_locks_SEQ
(
    next_val bigint
);
INSERT INTO gpu_locks_SEQ (next_val) VALUES (1);

-- the steps of a lock, in order: the ones that load it, the ones that free the GPU of it
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
