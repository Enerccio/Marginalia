package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.TagRelation;

import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TagRelationRepository extends ExtendableRepository<TagRelation> {

    List<TagRelation> findAllByTag(Tag tag) throws Exception;

    /**
     * Number of relations that are not deleted (positive and negative) of each non-deleted tag of the user, keyed by
     * tag id. Tags without a relation are not in the map.
     */
    Map<Long, Long> countByTag(User user) throws Exception;

    /**
     * Relations of the object that are not deleted, positive and negative.
     */
    List<TagRelation> findAllByObject(Long objectId, String clazz) throws Exception;

    List<Tag> findTagsForObject(Long objectId, String clazz) throws Exception;

    List<Tag> findTagsForObject(Long objectId, String clazz, boolean negative) throws Exception;

    /**
     * The tags (positive relations) of each of the objects, keyed by object id; an object without a tag is not in the
     * map.
     */
    Map<Long, List<Tag>> findTagsForObjects(Collection<Long> objectIds, String clazz) throws Exception;

    List<Long> findObjectIdsForTag(Long tagId, String clazz) throws Exception;

    List<Long> findObjectIdsForTag(Long tagId, String clazz, boolean negative) throws Exception;

    <T extends BaseEntity> List<T> findObjectsForTag(Long tagId, Class<T> clazz) throws Exception;

    <T extends BaseEntity> List<T> findObjectsForTag(Long tagId, Class<T> clazz, boolean negative) throws Exception;

    TagRelation findByTagAndObject(Long tagId, Long objectId, String clazz) throws Exception;

    TagRelation findByTagAndObject(Long tagId, Long objectId, String clazz, boolean negative) throws Exception;
}