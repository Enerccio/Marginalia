-- keys of the HTTP API (ApiKey): a user's bearer keys, the owner (userId) is the user the key acts as. Only a SHA-256 hash of the key
-- is stored; keyPrefix is the public part of the key it is looked up by. What the key may do (access), when it expires and when it was
-- last used are extended attributes. A revoked key is deleted (is_deleted), the cleanup purges it with its user.
CREATE TABLE api_keys
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
    keyPrefix       varchar(32)  not null unique,
    secretHash      varchar(64)  not null
);

CREATE INDEX ix_api_keys_owner ON api_keys (userId, is_deleted);

-- id generator of the entity
CREATE TABLE api_keys_SEQ
(
    next_val bigint
);
INSERT INTO api_keys_SEQ (next_val) VALUES (1);
