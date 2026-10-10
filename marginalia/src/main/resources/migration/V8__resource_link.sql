-- loose link of a resource to the object that uses it (class name + id, like t2e), bookkeeping only
ALTER TABLE resources
    ADD COLUMN objectId BIGINT;

ALTER TABLE resources
    ADD COLUMN clazz VARCHAR(255);

-- the Resources tab: resources of the user, newest first
CREATE INDEX ix_resources_user_list ON resources (userId, is_deleted, creation);
-- resources of an object
CREATE INDEX ix_resources_object ON resources (clazz, objectId);
