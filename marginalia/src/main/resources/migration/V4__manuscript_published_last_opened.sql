ALTER TABLE manuscripts
    ADD COLUMN published boolean not null default false;

ALTER TABLE manuscripts
    ADD COLUMN lastOpened timestamp;
