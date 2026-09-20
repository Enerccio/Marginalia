package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public interface TagRepository extends ExtendableRepository<Tag> {

    List<Tag> searchByValue(String filter, User user, int offset, int limit) throws Exception;

    int countByValue(String filter, User user) throws Exception;
}