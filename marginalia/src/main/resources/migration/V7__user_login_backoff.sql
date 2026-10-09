ALTER TABLE users
    ADD COLUMN failedLogins int NOT NULL DEFAULT 0;

ALTER TABLE users
    ADD COLUMN lockedUntil bigint NOT NULL DEFAULT 0;
