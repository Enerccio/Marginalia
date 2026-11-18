package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.TagRepository;

import java.util.List;

public interface TagService extends ExtendableService<Tag, TagRepository> {

    List<Tag> searchTagsForUser(String filter, int offset, int limit) throws Exception;

    int countTagsForUser(String filter) throws Exception;

    /**
     * Tag of the current user with given value (trimmed, matched case-insensitively), created when missing.
     *
     * @return {@code null} for a blank value
     */
    Tag getOrCreateForUser(String value) throws Exception;

    /**
     * A tag of the current user and the number of objects related to it (positive and negative relations).
     */
    record TagUsage(Tag tag, long objects) {
    }

    /**
     * All tags of the current user with their usage, sorted by value.
     */
    List<TagUsage> findUsageForUser() throws Exception;

    /**
     * The other tag of the current user that already has this value (trimmed, matched case-insensitively), i.e. the
     * tag the given tag would become an alias of when renamed to it. {@code null} when there is none.
     */
    Tag findRenameConflict(Tag tag, String newValue) throws Exception;

    /**
     * Renames the tag. Does not merge: fails when another tag already has the value, see {@link #findRenameConflict}.
     *
     * @return the renamed tag, {@code null} when the tag no longer exists
     */
    Tag rename(Tag tag, String newValue) throws Exception;

    /**
     * Makes {@code source} an alias of {@code target}: moves the relations of the source tag to the target tag and
     * deletes the source tag.
     */
    void merge(Tag source, Tag target) throws Exception;
}