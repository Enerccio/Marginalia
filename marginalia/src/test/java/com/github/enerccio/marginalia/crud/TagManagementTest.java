package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.TagService.TagUsage;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What the Tags section of the user settings does: usage counts, rename, merge on alias and delete.
 */
class TagManagementTest extends MarginaliaTestBase {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private LorebookService lorebookService;

    @BeforeEach
    void loginOwner() throws Exception {
        login();
    }

    private Tag tag(String value) throws Exception {
        return tagService.getOrCreateForUser(value);
    }

    private Lorebook lorebook(String name) throws Exception {
        Lorebook l = new Lorebook();
        l.setName(name);
        return lorebookService.save(l);
    }

    private long objects(String value) throws Exception {
        return tagService.findUsageForUser().stream()
                .filter(u -> u.tag().getValue().equals(value))
                .mapToLong(TagUsage::objects)
                .findFirst().orElseThrow();
    }

    @Test
    void usageCountsRelationsAndSortsByValue() throws Exception {
        Tag b = tag("b-tag");
        Tag a = tag("A-tag");
        tag("unused");
        tagRelationService.createRelation(b, lorebook("one"));
        tagRelationService.createRelation(b, lorebook("two"));
        tagRelationService.createRelation(a, lorebook("three"), true);

        assertThat(tagService.findUsageForUser()).extracting(u -> u.tag().getValue(), TagUsage::objects)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("A-tag", 1L),
                        org.assertj.core.groups.Tuple.tuple("b-tag", 2L),
                        org.assertj.core.groups.Tuple.tuple("unused", 0L));
    }

    @Test
    void usageIsPerUser() throws Exception {
        tagRelationService.createRelation(tag("mine"), lorebook("one"));

        loginAs(createUser());

        assertThat(tagService.findUsageForUser()).isEmpty();
    }

    @Test
    void renameWithoutConflict() throws Exception {
        Tag tag = tag("magic");
        Lorebook lorebook = lorebook("one");
        tagRelationService.createRelation(tag, lorebook);

        assertThat(tagService.findRenameConflict(tag, "sorcery")).isNull();
        Tag renamed = tagService.rename(tag, "  sorcery ");

        assertThat(renamed.getValue()).isEqualTo("sorcery");
        assertThat(tagRelationService.getTagsForObject(lorebook)).extracting(Tag::getValue).containsExactly("sorcery");
    }

    @Test
    void renameChangingOnlyCaseIsNotAConflict() throws Exception {
        Tag tag = tag("magic");

        assertThat(tagService.findRenameConflict(tag, "Magic")).isNull();
        assertThat(tagService.rename(tag, "Magic").getValue()).isEqualTo("Magic");
    }

    @Test
    void renameToExistingValueIsAConflictAndFails() throws Exception {
        Tag magic = tag("magic");
        Tag old = tag("sorcery");

        assertThat(tagService.findRenameConflict(magic, "SORCERY").getId()).isEqualTo(old.getId());
        assertThatThrownBy(() -> tagService.rename(magic, "sorcery")).isInstanceOf(IllegalStateException.class);
        assertThat(tagService.find(magic.getId()).getValue()).isEqualTo("magic");
    }

    @Test
    void mergeMovesRelationsToTheExistingTagAndDeletesTheCurrentOne() throws Exception {
        Tag current = tag("magic");
        Tag existing = tag("sorcery");
        Lorebook onlyCurrent = lorebook("only current");
        Lorebook both = lorebook("both");
        Lorebook negative = lorebook("negative");
        tagRelationService.createRelation(current, onlyCurrent);
        tagRelationService.createRelation(current, both);
        tagRelationService.createRelation(existing, both);
        tagRelationService.createRelation(current, negative, true);

        tagService.merge(current, existing);

        assertThat(tagService.find(current.getId()).isDeleted()).isTrue();
        assertThat(tagService.findUsageForUser()).extracting(u -> u.tag().getValue()).containsExactly("sorcery");
        assertThat(objects("sorcery")).isEqualTo(3);
        assertThat(tagRelationService.getTagsForObject(onlyCurrent)).extracting(Tag::getValue).containsExactly("sorcery");
        assertThat(tagRelationService.getTagsForObject(both)).extracting(Tag::getValue).containsExactly("sorcery");
        assertThat(tagRelationService.getTagsForObject(negative, true)).extracting(Tag::getValue).containsExactly("sorcery");
    }

    @Test
    void deleteSoftDeletesTheRelations() throws Exception {
        Tag tag = tag("magic");
        Lorebook lorebook = lorebook("one");
        var relation = tagRelationService.createRelation(tag, lorebook);

        tagService.delete(tag, false);

        assertThat(tagService.findUsageForUser()).isEmpty();
        assertThat(tagRelationService.getTagsForObject(lorebook)).isEmpty();
        assertThat(tagRelationService.find(relation.getId()).isDeleted()).isTrue();
    }

    @Test
    void foreignTagsCannotBeChanged() throws Exception {
        Tag foreign = tag("magic");
        loginAs(createUser());

        assertThat(tagService.rename(foreign, "other")).isNull();
        tagService.merge(foreign, tag("mine"));
        assertThat(tagService.find(foreign.getId()).isDeleted()).isFalse();
        assertThat(tagService.find(foreign.getId()).getValue()).isEqualTo("magic");
    }
}
