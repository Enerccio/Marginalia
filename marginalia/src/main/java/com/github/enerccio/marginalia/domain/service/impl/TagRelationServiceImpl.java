package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.TagRelation;
import com.github.enerccio.marginalia.domain.repository.TagRelationRepository;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;

import java.util.Collections;
import java.util.List;

public class TagRelationServiceImpl extends ExtendableServiceImpl<TagRelation, TagRelationRepository> implements TagRelationService {

    @Override
    @CommonTx
    public TagRelation createRelation(Tag tag, BaseEntity object) throws Exception {
        if (tag == null || object == null || object.getId() == null) {
            throw new IllegalArgumentException("Tag and Object with valid ID must not be null");
        }
        return createRelation(tag, object.getId(), object.getClass());
    }

    @Override
    @CommonTx
    public TagRelation createRelation(Tag tag, Manuscript manuscript) throws Exception {
        return createRelation(tag, (BaseEntity) manuscript);
    }

    @Override
    @CommonTx
    public TagRelation createRelation(Tag tag, Long objectId, Class<?> clazz) throws Exception {
        if (tag == null || objectId == null || clazz == null) {
            throw new IllegalArgumentException("Tag, objectId, and clazz must not be null");
        }
        String clazzName = clazz.getName();
        TagRelation existing = getRepository().findByTagAndObject(tag.getId(), objectId, clazzName);
        if (existing != null) {
            return existing;
        }

        TagRelation relation = new TagRelation();
        relation.setTag(tag);
        relation.setObjectId(objectId);
        relation.setClazz(clazzName);
        return save(relation);
    }

    @Override
    @CommonTx
    public void deleteForTag(Tag tag, boolean hard) throws Exception {
        if (tag == null || tag.getId() == null) {
            return;
        }
        List<TagRelation> relations = getRepository().findAllByTag(tag);
        for (TagRelation relation : relations) {
            delete(relation, hard);
        }
    }

    @Override
    @CommonTxReadOnly
    public List<Tag> getTagsForObject(BaseEntity object) throws Exception {
        if (object == null || object.getId() == null) {
            return Collections.emptyList();
        }
        return getTagsForObject(object.getId(), object.getClass());
    }

    @Override
    @CommonTxReadOnly
    public List<Tag> getTagsForObject(Long objectId, Class<?> clazz) throws Exception {
        if (objectId == null || clazz == null) {
            return Collections.emptyList();
        }
        return getRepository().findTagsForObject(objectId, clazz.getName());
    }

    @Override
    @CommonTxReadOnly
    public List<Long> getObjectIdsForTag(Tag tag, Class<?> clazz) throws Exception {
        if (tag == null || tag.getId() == null || clazz == null) {
            return Collections.emptyList();
        }
        return getRepository().findObjectIdsForTag(tag.getId(), clazz.getName());
    }

    @Override
    @CommonTxReadOnly
    public <T extends BaseEntity> List<T> getObjectsForTag(Tag tag, Class<T> clazz) throws Exception {
        if (tag == null || tag.getId() == null || clazz == null) {
            return Collections.emptyList();
        }
        return getRepository().findObjectsForTag(tag.getId(), clazz);
    }

    @Override
    @CommonTx
    public void removeRelation(Tag tag, Long objectId, Class<?> clazz) throws Exception {
        if (tag == null || objectId == null || clazz == null) {
            return;
        }
        TagRelation tr = getRepository().findByTagAndObject(tag.getId(), objectId, clazz.getName());
        if (tr != null) {
            delete(tr, true);
        }
    }

    @Override
    @CommonTx
    public void removeRelation(Tag tag, BaseEntity object) throws Exception {
        if (object == null || object.getId() == null) {
            return;
        }
        removeRelation(tag, object.getId(), object.getClass());
    }
}