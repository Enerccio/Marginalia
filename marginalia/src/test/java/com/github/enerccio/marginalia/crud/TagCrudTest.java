package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class TagCrudTest extends ExtendableCrudContract<Tag> {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private LorebookService lorebookService;

    @Override
    protected OwnedService<Tag, ?> service() {
        return tagService;
    }

    @Override
    protected Tag newEntity() {
        Tag tag = new Tag();
        tag.setValue("fantasy");
        return tag;
    }

    @Override
    protected void assertCreated(Tag loaded) {
        assertThat(loaded.getValue()).isEqualTo("fantasy");
    }

    @Override
    protected void modify(Tag entity) {
        entity.setValue("sci-fi");
    }

    @Override
    protected void assertModified(Tag loaded) {
        assertThat(loaded.getValue()).isEqualTo("sci-fi");
    }

    private Tag tag(String value) throws Exception {
        Tag tag = new Tag();
        tag.setValue(value);
        return tagService.save(tag);
    }

    @Test
    void searchIsCaseInsensitivePagedAndPerUser() throws Exception {
        String prefix = uniqueName("Search");
        tag(prefix + "-alpha");
        tag(prefix + "-beta");
        tag(prefix + "-gamma");
        tagService.delete(tag(prefix + "-deleted"), false);

        assertThat(tagService.countTagsForUser(prefix.toUpperCase())).isEqualTo(3);
        assertThat(tagService.searchTagsForUser(prefix.toLowerCase(), 0, 10)).extracting(Tag::getValue)
                .containsExactly(prefix + "-alpha", prefix + "-beta", prefix + "-gamma");
        assertThat(tagService.searchTagsForUser(prefix, 1, 1)).extracting(Tag::getValue).containsExactly(prefix + "-beta");
        assertThat(tagService.searchTagsForUser("  " + prefix + "-ga  ", 0, 10)).hasSize(1);

        login();
        assertThat(tagService.countTagsForUser(prefix)).isZero();
        assertThat(tagService.searchTagsForUser(prefix, 0, 10)).isEmpty();
    }

    @Test
    void getOrCreateCreatesMissingTag() throws Exception {
        String value = uniqueName("fresh");

        Tag created = tagService.getOrCreateForUser("  " + value + " ");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getValue()).isEqualTo(value);
        assertThat(created.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(tagService.getOrCreateForUser(value).getId()).isEqualTo(created.getId());
        assertThat(tagService.getOrCreateForUser(value.toUpperCase()).getId()).isEqualTo(created.getId());
        assertThat(tagService.getOrCreateForUser("  ")).isNull();
        assertThat(tagService.getOrCreateForUser(null)).isNull();
    }

    @Test
    void getOrCreateFindsExactMatchAmongManySimilarTags() throws Exception {
        String base = uniqueName("dark");
        for (int i = 0; i < 12; i++) {
            tag(base + "-variant-" + i);
        }
        Tag exact = tag(base);

        assertThat(tagService.getOrCreateForUser(base).getId()).isEqualTo(exact.getId());
        assertThat(tagService.countTagsForUser(base)).isEqualTo(13);
    }

    @Test
    void getOrCreateIgnoresDeletedAndForeignTags() throws Exception {
        String value = uniqueName("gone");
        Tag deleted = tag(value);
        tagService.delete(deleted, false);

        Tag recreated = tagService.getOrCreateForUser(value);
        assertThat(recreated.getId()).isNotEqualTo(deleted.getId());

        login();
        Tag foreign = tagService.getOrCreateForUser(value);
        assertThat(foreign.getId()).isNotEqualTo(recreated.getId());
        assertThat(foreign.getOwner().getId()).isEqualTo(currentUser.getId());
    }

    @Test
    void deletingTagDeletesItsRelations() throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName("lore");
        lorebook = lorebookService.save(lorebook);
        Tag tag = tag("doomed");
        tagRelationService.createRelation(tag, lorebook);

        tagService.delete(reload(tag), true);

        assertThat(reload(tag)).isNull();
        assertThat(tagRelationService.getTagsForObject(lorebook)).isEmpty();
    }

    @Test
    void softDeletingTagHidesItsRelations() throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName("lore");
        lorebook = lorebookService.save(lorebook);
        Tag tag = tag("hidden");
        tagRelationService.createRelation(tag, lorebook);

        tagService.delete(reload(tag), false);

        assertThat(tagRelationService.getTagsForObject(lorebook)).isEmpty();
    }
}
