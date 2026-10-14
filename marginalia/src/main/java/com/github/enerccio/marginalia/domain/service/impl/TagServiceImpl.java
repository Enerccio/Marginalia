package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.TagRepository;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TagServiceImpl extends ExtendableServiceImpl<Tag, TagRepository> implements TagService {

    @Autowired
    private TagRelationService tagRelationService;

    @Override
    @CommonTx
    public Tag delete(Tag entity, boolean hard) throws Exception {
        if (entity != null) {
            tagRelationService.deleteForTag(entity, hard);
        }
        return super.delete(entity, hard);
    }

    @Override
    @CommonTxReadOnly
    public List<Tag> searchTagsForUser(String filter, int offset, int limit) throws Exception {
        return getRepository().searchByValue(filter != null ? filter.trim() : "", currentUser, offset, limit);
    }

    @Override
    @CommonTxReadOnly
    public int countTagsForUser(String filter) throws Exception {
        return getRepository().countByValue(filter != null ? filter.trim() : "", currentUser);
    }

    @Override
    @CommonTxReadOnly
    public List<TagUsage> findUsageForUser() throws Exception {
        Map<Long, Long> counts = tagRelationService.countRelationsByTag();
        return findAllForUser().stream()
                .map(tag -> new TagUsage(tag, counts.getOrDefault(tag.getId(), 0L)))
                .sorted(Comparator.comparing((TagUsage usage) -> StringUtils.defaultString(usage.tag().getValue()), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    @CommonTxReadOnly
    public Tag findRenameConflict(Tag tag, String newValue) throws Exception {
        if (tag == null || tag.getId() == null || StringUtils.isBlank(newValue)) {
            return null;
        }
        return getRepository().findOtherByValue(newValue.trim(), tag.getId(), currentUser);
    }

    @Override
    @CommonTx
    public Tag rename(Tag tag, String newValue) throws Exception {
        if (StringUtils.isBlank(newValue)) {
            throw new IllegalArgumentException("Tag value must not be blank");
        }
        Tag current = findOwned(tag);
        if (current == null) {
            return null;
        }
        if (getRepository().findOtherByValue(newValue.trim(), current.getId(), currentUser) != null) {
            throw new IllegalStateException("Another tag already has the value " + newValue.trim());
        }
        current.setValue(newValue.trim());
        return save(current);
    }

    @Override
    @CommonTx
    public void merge(Tag source, Tag target) throws Exception {
        Tag from = findOwned(source);
        Tag to = findOwned(target);
        if (from == null || to == null || from.getId().equals(to.getId())) {
            return;
        }
        tagRelationService.moveRelations(from, to);
        delete(from, false);
    }

    /**
     * The persisted non-deleted tag for the (possibly detached) one, only when it belongs to the current user.
     */
    private Tag findOwned(Tag tag) throws Exception {
        if (tag == null) {
            return null;
        }
        Tag found = getRepository().findByUuid(tag.getUuid(), currentUser);
        return found == null || found.isDeleted() ? null : found;
    }

    @Override
    @CommonTx
    public Tag getOrCreateForUser(String value) throws Exception {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        Tag tag = getRepository().findByValue(trimmed, currentUser);
        if (tag == null) {
            tag = new Tag();
            tag.setValue(trimmed);
            tag = save(tag);
        }
        return tag;
    }
}