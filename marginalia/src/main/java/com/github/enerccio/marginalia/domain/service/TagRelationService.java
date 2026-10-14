package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.TagRelation;
import com.github.enerccio.marginalia.domain.repository.TagRelationRepository;

import java.util.List;
import java.util.Map;

public interface TagRelationService extends ExtendableService<TagRelation, TagRelationRepository> {

    TagRelation createRelation(Tag tag, BaseEntity object) throws Exception;

    TagRelation createRelation(Tag tag, BaseEntity object, boolean negative) throws Exception;

    TagRelation createRelation(Tag tag, Manuscript manuscript) throws Exception;

    TagRelation createRelation(Tag tag, Long objectId, Class<?> clazz) throws Exception;

    TagRelation createRelation(Tag tag, Long objectId, Class<?> clazz, boolean negative) throws Exception;

    void deleteForTag(Tag tag, boolean hard) throws Exception;

    /**
     * Moves the relations of the tag to another tag. A relation the object already has with the other tag is deleted
     * instead of moved, so the object is not tagged twice.
     */
    void moveRelations(Tag from, Tag to) throws Exception;

    /**
     * Number of relations (positive and negative) that are not deleted of each tag of the current user, keyed by tag
     * id. Tags without a relation are not in the map.
     */
    Map<Long, Long> countRelationsByTag() throws Exception;

    /**
     * Deletes all relations of the object (positive and negative) to tags, for the delete of the tagged object.
     */
    void deleteForObject(BaseEntity object, boolean hard) throws Exception;

    List<Tag> getTagsForObject(BaseEntity object) throws Exception;

    List<Tag> getTagsForObject(BaseEntity object, boolean negative) throws Exception;

    List<Tag> getTagsForObject(Long objectId, Class<?> clazz) throws Exception;

    List<Tag> getTagsForObject(Long objectId, Class<?> clazz, boolean negative) throws Exception;

    List<Long> getObjectIdsForTag(Tag tag, Class<?> clazz) throws Exception;

    List<Long> getObjectIdsForTag(Tag tag, Class<?> clazz, boolean negative) throws Exception;

    <T extends BaseEntity> List<T> getObjectsForTag(Tag tag, Class<T> clazz) throws Exception;

    <T extends BaseEntity> List<T> getObjectsForTag(Tag tag, Class<T> clazz, boolean negative) throws Exception;

    void removeRelation(Tag tag, Long objectId, Class<?> clazz) throws Exception;

    void removeRelation(Tag tag, Long objectId, Class<?> clazz, boolean negative) throws Exception;

    void removeRelation(Tag tag, BaseEntity object) throws Exception;

    void removeRelation(Tag tag, BaseEntity object, boolean negative) throws Exception;
}