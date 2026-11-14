-- the log of the jobs (a unit of work that talks to an external AI: a generation, a summary, an audiobook chapter...) and of the
-- calls they made (a request to a model, a speech or an image backend), metadata only, for the administrator's overview.
-- Not foreign keys on purpose for the user: the row keeps the login it was made under (ownerLogin) and is purged with the user by
-- cleanup; the calls belong to their job (job_id) and go with it.
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

CREATE INDEX ix_jobs_started ON jobs (startedAt);
CREATE INDEX ix_jobs_user ON jobs (userId, startedAt);

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

CREATE INDEX ix_job_calls_job ON job_calls (job_id, startedAt);

CREATE TABLE jobs_SEQ
(
    next_val bigint
);
INSERT INTO jobs_SEQ (next_val) VALUES (1);

CREATE TABLE job_calls_SEQ
(
    next_val bigint
);
INSERT INTO job_calls_SEQ (next_val) VALUES (1);
