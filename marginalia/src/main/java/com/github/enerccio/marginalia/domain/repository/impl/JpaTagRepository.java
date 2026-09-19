package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.TagRepository;

public class JpaTagRepository extends JpaExtendableRepository<Tag> implements TagRepository {

    @Override
    protected Class<Tag> getEntityClass() {
        return Tag.class;
    }
}
