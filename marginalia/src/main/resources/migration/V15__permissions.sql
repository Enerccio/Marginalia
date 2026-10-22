-- what an administrator allowed a user to do (Permission): one row per user and type, the owner is the administrator
-- who gave it. holder_id is nullable: the cleanup nulls the reference before it purges the user
CREATE TABLE permissions
(
    id              bigint      not null primary key,
    creation        timestamp,
    is_deleted      boolean     not null,
    modification    timestamp,
    uuid            varchar(36) not null unique,
    extendedContent blob,
    _fulltext       clob,
    userId          bigint,
    permissionType  varchar(64) not null,
    holder_id       bigint,
    granted         boolean     not null default false
);

CREATE INDEX ix_permissions_holder ON permissions (holder_id, permissionType, is_deleted);

-- id generator of the entity
CREATE TABLE permissions_SEQ
(
    next_val bigint
);
INSERT INTO permissions_SEQ (next_val) VALUES (1);
