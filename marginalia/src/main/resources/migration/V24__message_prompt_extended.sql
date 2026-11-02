-- message_prompts (MessagePrompt) becomes an ExtendableEntity: the lore entries a generation used (loreUsage, an extended
-- attribute) are kept with the prompt, which is only read when somebody asks for it, not with the part that is loaded with
-- the whole branch. extendedContent is gzipped by the entity, _fulltext is not filled (the prompt is not searched).
-- The rows that exist have no content: a part made before this has no record of the lore it used.
ALTER TABLE message_prompts ADD COLUMN extendedContent blob;
ALTER TABLE message_prompts ADD COLUMN _fulltext clob;
