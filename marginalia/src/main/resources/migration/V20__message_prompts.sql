-- message_prompts (MessagePrompt): the prompt that was sent to the model to write a part, and the lore in it. It was in
-- the extended content of the part, where every load of the part (all the parts of a branch are loaded together) had to
-- read it, hundreds of kilobytes each. The rows are made from the extended content by the app migration
-- ExtendedContentMigration. The texts are deflated (UTF-8 bytes). message_id is nullable: the cleanup nulls the reference before it purges the part
CREATE TABLE message_prompts
(
    id             bigint      not null primary key,
    creation       timestamp,
    is_deleted     boolean     not null,
    modification   timestamp,
    uuid           varchar(36) not null unique,
    userId         bigint,
    message_id     bigint,
    builtPrompt    blob,
    backgroundLore blob
);

-- one prompt of a part, read by the part
CREATE INDEX ix_message_prompts_message ON message_prompts (message_id, is_deleted);
CREATE INDEX ix_message_prompts_user ON message_prompts (userId, is_deleted);

-- id generator of the entity
CREATE TABLE message_prompts_SEQ
(
    next_val bigint
);
INSERT INTO message_prompts_SEQ (next_val) VALUES (1);
