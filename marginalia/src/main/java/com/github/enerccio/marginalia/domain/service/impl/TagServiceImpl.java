package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.TagRepository;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import org.springframework.beans.factory.annotation.Autowired;

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
}