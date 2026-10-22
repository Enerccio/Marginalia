-- which item of the working copy of an agent run is which item of the original (AgentRunPair): the copy has new uuids,
-- the merge, the checkpoint restore and the handles of the agent match the items by these rows. A plain table, no
-- uuid / soft delete / extended content: thousands of rows per run, deleted with the working copy
CREATE TABLE agent_run_pairs
(
    id           bigint      not null primary key,
    run_id       bigint      not null,
    kind         varchar(16) not null,
    copyUuid     varchar(36) not null,
    originalUuid varchar(36),
    handle       varchar(16)
);

-- one row per item of the copy, the lookup of both directions, and the handle the agent is given (null until it is)
CREATE UNIQUE INDEX ux_agent_run_pairs_copy ON agent_run_pairs (run_id, copyUuid);
CREATE INDEX ix_agent_run_pairs_original ON agent_run_pairs (run_id, originalUuid);
CREATE UNIQUE INDEX ux_agent_run_pairs_handle ON agent_run_pairs (run_id, handle);

-- id generator of the entity
CREATE TABLE agent_run_pairs_SEQ
(
    next_val bigint
);
INSERT INTO agent_run_pairs_SEQ (next_val) VALUES (1);
