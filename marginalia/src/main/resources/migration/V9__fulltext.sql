-- text of the @Fulltextable fields of an entity, filled together with extendedContent (ExtendableEntityListener);
-- the column belongs to ExtendableEntity, so every table of an extendable entity has it
ALTER TABLE ais ADD COLUMN _fulltext clob;
ALTER TABLE entries ADD COLUMN _fulltext clob;
ALTER TABLE lorebooks ADD COLUMN _fulltext clob;
ALTER TABLE manuscripts ADD COLUMN _fulltext clob;
ALTER TABLE messages ADD COLUMN _fulltext clob;
ALTER TABLE protocols ADD COLUMN _fulltext clob;
ALTER TABLE settings ADD COLUMN _fulltext clob;
ALTER TABLE summaries ADD COLUMN _fulltext clob;
ALTER TABLE t2e ADD COLUMN _fulltext clob;
ALTER TABLE tags ADD COLUMN _fulltext clob;

-- manuscripts.description is an extended attribute only (it was stored in both places): keep values that never made
-- it into extendedContent, then drop the column
UPDATE manuscripts
SET extendedContent = CAST(json_set(
        CASE WHEN extendedContent IS NOT NULL AND json_valid(CAST(extendedContent AS TEXT))
                 THEN CAST(extendedContent AS TEXT) ELSE '{}' END,
        '$.description', description) AS BLOB)
WHERE description IS NOT NULL
  AND description <> ''
  AND (extendedContent IS NULL
    OR NOT json_valid(CAST(extendedContent AS TEXT))
    OR json_extract(CAST(extendedContent AS TEXT), '$.description') IS NULL);

ALTER TABLE manuscripts DROP COLUMN description;
