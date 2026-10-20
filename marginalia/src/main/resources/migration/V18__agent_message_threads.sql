-- the conversation of a sub-agent is stored with the transcript of its run: thread is the handle of the sub-agent (sub1,
-- sub2...), null for the conversation of the agent itself, which is all the transcript queries read. The ordinal counts
-- per thread.
ALTER TABLE agent_messages
    ADD COLUMN thread varchar(16);

CREATE INDEX ix_agent_messages_thread ON agent_messages (run_id, thread, is_deleted, ordinal);
