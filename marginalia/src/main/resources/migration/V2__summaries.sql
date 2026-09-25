ALTER TABLE messages
    ADD COLUMN summary_id BIGINT REFERENCES summaries(id);