-- the writing statistics of a book, per day (see TODO.USAGE.MD): the counts (ManuscriptStatistic), the time as stretches of the
-- clock (ActivityInterval) and the size of the book at the end of the day (ManuscriptSize). The owner is the owner of the book.
-- bookUuid is the uuid of the book and not a foreign key on purpose: the statistics outlive the book (its trash and its purge
-- leave them), so the name of the book and the path of its folder are copied into every row. day is ISO text (2026-10-31), the
-- day of the zone of the server.

-- the counts: at most two rows per book and day, the author's (is_agent = 0) and the agents'. The counters are added up in SQL
CREATE TABLE manuscript_statistics
(
    id             bigint       not null primary key,
    creation       timestamp,
    is_deleted     boolean      not null,
    modification   timestamp,
    uuid           varchar(36)  not null unique,
    userId         bigint,
    bookUuid       varchar(36)  not null,
    bookName       varchar(255),
    folderPath     varchar(2048),
    day            varchar(10)  not null,
    is_agent       boolean      not null,
    prefillTokens  bigint       not null default 0,
    generatedTokens bigint      not null default 0,
    requests       bigint       not null default 0,
    wordsWritten   bigint       not null default 0,
    wordsGenerated bigint       not null default 0,
    wordsRemoved   bigint       not null default 0,
    partsCreated   bigint       not null default 0,
    partsDeleted   bigint       not null default 0,
    edits          bigint       not null default 0,
    reconstructed  boolean      not null default 0,
    lastChange     timestamp
);

CREATE UNIQUE INDEX ux_manuscript_statistics_book_day ON manuscript_statistics (bookUuid, day, is_agent);
CREATE INDEX ix_manuscript_statistics_user ON manuscript_statistics (userId, day);

CREATE TABLE manuscript_statistics_SEQ
(
    next_val bigint
);
INSERT INTO manuscript_statistics_SEQ (next_val) VALUES (1);

-- the time: one row per stretch during which a kind of activity (OPEN, EDIT, GENERATE, RUN) went on in a book, cut at midnight
CREATE TABLE activity_intervals
(
    id           bigint       not null primary key,
    creation     timestamp,
    is_deleted   boolean      not null,
    modification timestamp,
    uuid         varchar(36)  not null unique,
    userId       bigint,
    bookUuid     varchar(36)  not null,
    bookName     varchar(255),
    folderPath   varchar(2048),
    kind         varchar(16)  not null,
    is_agent     boolean      not null,
    day          varchar(10)  not null,
    startTime    timestamp    not null,
    endTime      timestamp    not null
);

CREATE INDEX ix_activity_intervals_user ON activity_intervals (userId, day);
CREATE INDEX ix_activity_intervals_book ON activity_intervals (bookUuid, day);

CREATE TABLE activity_intervals_SEQ
(
    next_val bigint
);
INSERT INTO activity_intervals_SEQ (next_val) VALUES (1);

-- the size of the book at the end of the day: one row per book and day, the latest value of the day
CREATE TABLE manuscript_sizes
(
    id            bigint       not null primary key,
    creation      timestamp,
    is_deleted    boolean      not null,
    modification  timestamp,
    uuid          varchar(36)  not null unique,
    userId        bigint,
    bookUuid      varchar(36)  not null,
    bookName      varchar(255),
    folderPath    varchar(2048),
    day           varchar(10)  not null,
    bookWords     bigint       not null default 0,
    bookTokens    bigint       not null default 0,
    branchWords   bigint       not null default 0,
    branchTokens  bigint       not null default 0,
    leafUuid      varchar(36),
    baseline      boolean      not null default 0,
    reconstructed boolean      not null default 0,
    lastChange    timestamp
);

CREATE UNIQUE INDEX ux_manuscript_sizes_book_day ON manuscript_sizes (bookUuid, day);
CREATE INDEX ix_manuscript_sizes_user ON manuscript_sizes (userId, day);

CREATE TABLE manuscript_sizes_SEQ
(
    next_val bigint
);
INSERT INTO manuscript_sizes_SEQ (next_val) VALUES (1);
