-- resources is an ExtendableEntity (V33 of Marginalia)
ALTER TABLE resources ADD COLUMN extendedContent blob;
ALTER TABLE resources ADD COLUMN _fulltext clob;
