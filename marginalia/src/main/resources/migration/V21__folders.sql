-- folders (Folder): groups of books, nested through parent_id (null is the top level). The books in a folder inherit the settings
-- of the folder (prompts, style, language...), which are in the extended content. The name is a column because the list and the
-- rules of the service (unique among the folders of a parent) work with it. parent_id is nullable: the cleanup nulls the
-- reference before it purges the owner
CREATE TABLE folders
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
    parent_id       bigint
);

-- the folders below a folder, of the top level (parent_id is null) of a user
CREATE INDEX ix_folders_parent ON folders (parent_id, is_deleted);
CREATE INDEX ix_folders_user ON folders (userId, is_deleted);

-- id generator of the entity
CREATE TABLE folders_SEQ
(
    next_val bigint
);
INSERT INTO folders_SEQ (next_val) VALUES (1);

-- the folder of a book (null is the top level)
ALTER TABLE manuscripts
    ADD COLUMN folder_id bigint;
CREATE INDEX ix_manuscripts_folder ON manuscripts (folder_id, is_deleted);
