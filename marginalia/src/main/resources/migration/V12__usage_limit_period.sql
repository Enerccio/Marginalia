-- tokens a user used since the last reset of their token limit on an AI (the limits themselves are in the AI)
ALTER TABLE usages
    ADD COLUMN periodPrefillTokens bigint not null default 0;
ALTER TABLE usages
    ADD COLUMN periodGeneratedTokens bigint not null default 0;
