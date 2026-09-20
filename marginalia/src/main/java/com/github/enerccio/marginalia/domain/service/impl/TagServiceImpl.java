package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.TagRepository;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
}