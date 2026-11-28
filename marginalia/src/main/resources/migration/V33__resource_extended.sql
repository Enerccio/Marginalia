-- resources (Resource) becomes an ExtendableEntity: extensions keep their own data about a file in its attributes
-- (extendedContent, gzipped by the entity when it is not small); _fulltext is not filled (the files are not searched).
-- The rows that exist have no content.
ALTER TABLE resources ADD COLUMN extendedContent blob;
ALTER TABLE resources ADD COLUMN _fulltext clob;
