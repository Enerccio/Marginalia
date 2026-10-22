package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code BackupService.cloneManuscript}: the Clone button of the book list.
 */
class ManuscriptCloneTest extends BackupTestBase {

    private AI ai;
    private Protocol protocol;
    private Lorebook lorebook;
    private Manuscript original;

    @BeforeEach
    void createOriginal() throws Exception {
        login();
        ai = ai(uniqueName("ai"));
        protocol = protocol(uniqueName("protocol"));
        lorebook = lorebook(uniqueName("lore"));
        original = manuscript("Original", ai, protocol, lorebook);
        tagRelationService.createRelation(tag("fantasy"), original);
        original = story(original);
    }

    @Test
    void cloneOfAllBranchesHasTheWholeStory() throws Exception {
        Manuscript clone = reload(backupService.cloneManuscript(original, "Whole", false));

        assertThat(clone.getName()).isEqualTo("Whole");
        assertThat(clone.getId()).isNotEqualTo(original.getId());
        assertThat(tree(clone)).isEqualTo(storyTree());
        assertThat(activeLeafResponse(clone)).isEqualTo("chapter 2a");
        assertThat(summaryService.find(messageByResponse(clone, "chapter 2a").getSummary().getId()).getSummary())
                .isEqualTo("summary up to 2a");
    }

    @Test
    void cloneOfCurrentBranchLeavesTheOtherBranchesOut() throws Exception {
        Manuscript clone = reload(backupService.cloneManuscript(original, "Branch", true));

        Map<String, String> expected = new TreeMap<>(storyTree());
        expected.remove("chapter 2b");
        assertThat(tree(clone)).isEqualTo(expected);
        assertThat(activeLeafResponse(clone)).isEqualTo("chapter 2a");
        // the original keeps all of it
        assertThat(tree(original)).isEqualTo(storyTree());
    }

    @Test
    void cloneKeepsSettingsAndLinksTheLorebook() throws Exception {
        Manuscript clone = reload(backupService.cloneManuscript(original, "Linked", true));

        assertThat(idOf(clone.getAi())).isEqualTo(ai.getId());
        assertThat(idOf(clone.getProtocol())).isEqualTo(protocol.getId());
        assertThat(idOf(clone.getLorebook())).isEqualTo(lorebook.getId());
        assertThat(clone.getDescription()).isEqualTo("description of Original");
        assertThat(clone.getPov()).isEqualTo("first person");
        assertThat(clone.getAttributes().get("plugin.flag").getAsString()).isEqualTo("on");
        assertThat(tagValues(clone)).containsExactly("fantasy");
        assertThat(clone.isPublished()).isFalse();
        assertThat(lorebookNames()).containsOnlyOnce(lorebook.getName());
    }

    @Test
    void blankNameIsRefused() {
        assertThatThrownBy(() -> backupService.cloneManuscript(original, "  ", true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
