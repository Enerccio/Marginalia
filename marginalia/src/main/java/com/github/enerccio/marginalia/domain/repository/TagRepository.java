package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public interface TagRepository extends ExtendableRepository<Tag> {

    List<Tag> searchByValue(String filter, User user, int offset, int limit) throws Exception;

    int countByValue(String filter, User user) throws Exception;

    /**
     * Non-deleted tag of the user with exactly this value (case-insensitive), the oldest one if there are several.
     */
    Tag findByValue(String value, User user) throws Exception;
}