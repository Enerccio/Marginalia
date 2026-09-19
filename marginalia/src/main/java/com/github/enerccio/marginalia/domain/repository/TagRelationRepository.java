package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.TagRelation;

import java.util.List;

public interface TagRelationRepository extends BaseRepository<TagRelation> {

    List<TagRelation> findAllByTag(Tag tag) throws Exception;

    List<Tag> findTagsForObject(Long objectId, String clazz) throws Exception;

    List<Long> findObjectIdsForTag(Long tagId, String clazz) throws Exception;

    <T extends BaseEntity> List<T> findObjectsForTag(Long tagId, Class<T> clazz) throws Exception;

    TagRelation findByTagAndObject(Long tagId, Long objectId, String clazz) throws Exception;
}