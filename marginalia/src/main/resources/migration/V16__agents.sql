-- agents: LLMs that work on one manuscript on their own (Agent), the configuration is in the extended content.
-- manuscript_id (and agent_id of the runs) are nullable: the cleanup nulls the reference before it purges the owner
CREATE TABLE agents
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
    enabled         boolean      not null default true,
    manuscript_id   bigint
);

CREATE INDEX ix_agents_manuscript ON agents (manuscript_id, is_deleted);
CREATE INDEX ix_agents_user ON agents (userId, is_deleted);

-- id generator of the entity
CREATE TABLE agents_SEQ
(
    next_val bigint
);
INSERT INTO agents_SEQ (next_val) VALUES (1);

-- executions of an agent (AgentRun): the status is queried (one unfinished run per agent, runs interrupted by a
-- restart), the counters are added up in SQL
CREATE TABLE agent_runs
(
    id              bigint       not null primary key,
    creation        timestamp,
    is_deleted      boolean      not null,
    modification    timestamp,
    uuid            varchar(36)  not null unique,
    extendedContent blob,
    _fulltext       clob,
    userId          bigint,
    agent_id        bigint,
    status          varchar(32)  not null,
    statusReason    varchar(2000),
    attention       boolean      not null default false,
    workingCopy_id  bigint,
    prefillTokens   bigint       not null default 0,
    generatedTokens bigint       not null default 0,
    rounds          bigint       not null default 0,
    startedAt       timestamp,
    finishedAt      timestamp
);

CREATE INDEX ix_agent_runs_agent ON agent_runs (agent_id, is_deleted, status);
CREATE INDEX ix_agent_runs_user ON agent_runs (userId, is_deleted, status);
CREATE INDEX ix_agent_runs_status ON agent_runs (status, is_deleted);

-- id generator of the entity
CREATE TABLE agent_runs_SEQ
(
    next_val bigint
);
INSERT INTO agent_runs_SEQ (next_val) VALUES (1);

-- at most one unfinished run per agent (the statuses that are not terminal, see AgentRunStatus#isTerminal); the
-- service checks it first, this is what keeps two parallel starts from both creating one
CREATE UNIQUE INDEX ux_agent_runs_unfinished ON agent_runs (agent_id)
    WHERE is_deleted = 0 AND status NOT IN ('STOPPED_DISCARDED', 'FAILED', 'APPLIED');

-- the transcript of a run (AgentMessage): one row per chat message, the message itself is in the extended content
CREATE TABLE agent_messages
(
    id              bigint       not null primary key,
    creation        timestamp,
    is_deleted      boolean      not null,
    modification    timestamp,
    uuid            varchar(36)  not null unique,
    extendedContent blob,
    _fulltext       clob,
    userId          bigint,
    run_id          bigint,
    ordinal         integer      not null
);

CREATE INDEX ix_agent_messages_run ON agent_messages (run_id, is_deleted, ordinal);

-- id generator of the entity
CREATE TABLE agent_messages_SEQ
(
    next_val bigint
);
INSERT INTO agent_messages_SEQ (next_val) VALUES (1);

-- books that are the working copy of an agent (Manuscript.agentic, Lorebook.agentic), not listed with the books of the user
ALTER TABLE manuscripts
    ADD COLUMN agentic boolean not null default false;
ALTER TABLE lorebooks
    ADD COLUMN agentic boolean not null default false;
