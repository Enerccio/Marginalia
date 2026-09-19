package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.TagRelation;
import com.github.enerccio.marginalia.domain.repository.TagRelationRepository;

import java.util.List;

public interface TagRelationService extends BaseService<TagRelation, TagRelationRepository> {

    TagRelation createRelation(Tag tag, BaseEntity object) throws Exception;

    TagRelation createRelation(Tag tag, Manuscript manuscript) throws Exception;

    TagRelation createRelation(Tag tag, Long objectId, Class<?> clazz) throws Exception;

    void deleteForTag(Tag tag, boolean hard) throws Exception;

    List<Tag> getTagsForObject(BaseEntity object) throws Exception;

    List<Tag> getTagsForObject(Long objectId, Class<?> clazz) throws Exception;

    List<Long> getObjectIdsForTag(Tag tag, Class<?> clazz) throws Exception;

    <T extends BaseEntity> List<T> getObjectsForTag(Tag tag, Class<T> clazz) throws Exception;
}