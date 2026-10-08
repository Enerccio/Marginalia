package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.TagRelation;
import com.github.enerccio.marginalia.domain.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagRelationCrudTest extends ExtendableCrudContract<TagRelation> {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private Tag tag;
    private Lorebook lorebook;

    @BeforeEach
    void createTagAndObject() throws Exception {
        tag = tag("magic");
        Lorebook l = new Lorebook();
        l.setName("lore");
        lorebook = lorebookService.save(l);
    }

    private Tag tag(String value) throws Exception {
        Tag t = new Tag();
        t.setValue(value);
        return tagService.save(t);
    }

    private LorebookEntry entry(String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        return lorebookEntryService.save(entry);
    }

    @Override
    protected OwnedService<TagRelation, ?> service() {
        return tagRelationService;
    }

    @Override
    protected TagRelation newEntity() {
        TagRelation relation = new TagRelation();
        relation.setTag(tag);
        relation.setObjectId(lorebook.getId());
        relation.setClazz(Lorebook.class.getName());
        relation.setNegative(true);
        return relation;
    }

    @Override
    protected void assertCreated(TagRelation loaded) {
        assertThat(idOf(loaded.getTag())).isEqualTo(tag.getId());
        assertThat(loaded.getObjectId()).isEqualTo(lorebook.getId());
        assertThat(loaded.getClazz()).isEqualTo(Lorebook.class.getName());
        assertThat(loaded.isNegative()).isTrue();
    }

    @Override
    protected void modify(TagRelation entity) {
        entity.setNegative(false);
    }

    @Override
    protected void assertModified(TagRelation loaded) {
        assertThat(loaded.isNegative()).isFalse();
        assertThat(loaded.getObjectId()).isEqualTo(lorebook.getId());
    }

    @Test
    void createRelationIsIdempotent() throws Exception {
        TagRelation first = tagRelationService.createRelation(tag, lorebook);
        TagRelation second = tagRelationService.createRelation(tag, lorebook);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(tagRelationService.getTagsForObject(lorebook)).extracting(BaseEntity::getId).containsExactly(tag.getId());
    }

    @Test
    void positiveAndNegativeRelationsAreSeparate() throws Exception {
        Tag other = tag("night");
        tagRelationService.createRelation(tag, lorebook);
        tagRelationService.createRelation(other, lorebook, true);

        assertThat(tagRelationService.getTagsForObject(lorebook)).extracting(Tag::getValue).containsExactly("magic");
        assertThat(tagRelationService.getTagsForObject(lorebook, true)).extracting(Tag::getValue).containsExactly("night");
        assertThat(tagRelationService.getTagsForObject(lorebook.getId(), Lorebook.class)).hasSize(1);
        assertThat(tagRelationService.getObjectIdsForTag(other, Lorebook.class)).isEmpty();
        assertThat(tagRelationService.getObjectIdsForTag(other, Lorebook.class, true)).containsExactly(lorebook.getId());
    }

    @Test
    void relationsAreScopedByClass() throws Exception {
        LorebookEntry entry = entry("entry");
        tagRelationService.createRelation(tag, entry);
        tagRelationService.createRelation(tag, lorebook);

        assertThat(tagRelationService.getObjectIdsForTag(tag, Lorebook.class)).containsExactly(lorebook.getId());
        assertThat(tagRelationService.getObjectIdsForTag(tag, LorebookEntry.class)).containsExactly(entry.getId());
        assertThat(tagRelationService.getObjectsForTag(tag, LorebookEntry.class)).extracting(BaseEntity::getId)
                .containsExactly(entry.getId());
    }

    @Test
    void removeRelation() throws Exception {
        LorebookEntry entry = entry("entry");
        tagRelationService.createRelation(tag, entry);
        tagRelationService.createRelation(tag, entry, true);

        tagRelationService.removeRelation(tag, entry);

        assertThat(tagRelationService.getTagsForObject(entry)).isEmpty();
        assertThat(tagRelationService.getTagsForObject(entry, true)).hasSize(1);

        tagRelationService.removeRelation(tag, entry.getId(), LorebookEntry.class, true);
        assertThat(tagRelationService.getTagsForObject(entry, true)).isEmpty();
    }

    @Test
    void unsavedObjectsAreRejectedOrEmpty() throws Exception {
        assertThatThrownBy(() -> tagRelationService.createRelation(tag, new Lorebook()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(tagRelationService.getTagsForObject(new Lorebook())).isEmpty();
        assertThat(tagRelationService.getObjectIdsForTag(new Tag(), Lorebook.class)).isEmpty();
    }
}
