package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.TagRepository;

import java.util.List;

public interface TagService extends ExtendableService<Tag, TagRepository> {

    List<Tag> searchTagsForUser(String filter, int offset, int limit) throws Exception;

    int countTagsForUser(String filter) throws Exception;
}