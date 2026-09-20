package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.TagRelation;
import com.github.enerccio.marginalia.domain.repository.TagRelationRepository;

import java.util.List;

public class JpaTagRelationRepository extends JpaExtendableRepository<TagRelation> implements TagRelationRepository {

    @Override
    protected Class<TagRelation> getEntityClass() {
        return TagRelation.class;
    }

    @Override
    public List<TagRelation> findAllByTag(Tag tag) throws Exception {
        return getEntityManager().createQuery(
                        "SELECT tr FROM TagRelation tr WHERE tr.tag.id = :tagId", TagRelation.class)
                .setParameter("tagId", tag.getId())
                .getResultList();
    }

    @Override
    public List<Tag> findTagsForObject(Long objectId, String clazz) throws Exception {
        return findTagsForObject(objectId, clazz, false);
    }

    @Override
    public List<Tag> findTagsForObject(Long objectId, String clazz, boolean negative) throws Exception {
        return getEntityManager().createQuery(
                        "SELECT tr.tag FROM TagRelation tr WHERE tr.objectId = :objectId AND tr.clazz = :clazz AND tr.negative = :negative AND tr.deleted = false AND tr.tag.deleted = false", Tag.class)
                .setParameter("objectId", objectId)
                .setParameter("clazz", clazz)
                .setParameter("negative", negative)
                .getResultList();
    }

    @Override
    public List<Long> findObjectIdsForTag(Long tagId, String clazz) throws Exception {
        return findObjectIdsForTag(tagId, clazz, false);
    }

    @Override
    public List<Long> findObjectIdsForTag(Long tagId, String clazz, boolean negative) throws Exception {
        return getEntityManager().createQuery(
                        "SELECT tr.objectId FROM TagRelation tr WHERE tr.tag.id = :tagId AND tr.clazz = :clazz AND tr.negative = :negative AND tr.deleted = false", Long.class)
                .setParameter("tagId", tagId)
                .setParameter("clazz", clazz)
                .setParameter("negative", negative)
                .getResultList();
    }

    @Override
    public <T extends BaseEntity> List<T> findObjectsForTag(Long tagId, Class<T> clazz) throws Exception {
        return findObjectsForTag(tagId, clazz, false);
    }

    @Override
    public <T extends BaseEntity> List<T> findObjectsForTag(Long tagId, Class<T> clazz, boolean negative) throws Exception {
        String entityName = clazz.getSimpleName();
        String jpql = "SELECT e FROM " + entityName + " e, TagRelation tr " +
                "WHERE tr.tag.id = :tagId AND tr.objectId = e.id AND tr.clazz = :clazz " +
                "AND tr.negative = :negative AND tr.deleted = false AND e.deleted = false";
        return getEntityManager().createQuery(jpql, clazz)
                .setParameter("tagId", tagId)
                .setParameter("clazz", clazz.getName())
                .setParameter("negative", negative)
                .getResultList();
    }

    @Override
    public TagRelation findByTagAndObject(Long tagId, Long objectId, String clazz) throws Exception {
        return findByTagAndObject(tagId, objectId, clazz, false);
    }

    @Override
    public TagRelation findByTagAndObject(Long tagId, Long objectId, String clazz, boolean negative) throws Exception {
        List<TagRelation> results = getEntityManager().createQuery(
                        "SELECT tr FROM TagRelation tr WHERE tr.tag.id = :tagId AND tr.objectId = :objectId AND tr.clazz = :clazz AND tr.negative = :negative AND tr.deleted = false", TagRelation.class)
                .setParameter("tagId", tagId)
                .setParameter("objectId", objectId)
                .setParameter("clazz", clazz)
                .setParameter("negative", negative)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : results.getFirst();
    }
}