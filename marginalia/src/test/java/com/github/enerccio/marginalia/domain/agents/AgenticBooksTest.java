package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The working copies of agents (agentic books) are kept apart from the books of the user: the old methods return
 * both kinds, the regular ones what the UI lists, the agentic ones the copies.
 */
class AgenticBooksTest extends AgentTestBase {

    @Autowired
    private LorebookService lorebookService;

    private Manuscript agenticManuscript() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("copy"));
        manuscript.setAgentic(true);
        return manuscriptService.save(manuscript);
    }

    private Lorebook lorebook(boolean agentic) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(uniqueName("lore"));
        lorebook.setAgentic(agentic);
        return lorebookService.save(lorebook);
    }

    @Test
    void booksAreNotAgenticByDefault() throws Exception {
        loginAgentUser();

        assertThat(manuscript().isAgentic()).isFalse();
        assertThat(lorebook(false).isAgentic()).isFalse();
        assertThat(agenticManuscript().isAgentic()).isTrue();
    }

    @Test
    void manuscriptsAreSplitByTheFlag() throws Exception {
        loginAgentUser();
        Manuscript plain = manuscript();
        Manuscript copy = agenticManuscript();

        assertThat(manuscriptService.findAllRegularForUser()).extracting(Manuscript::getUuid)
                .contains(plain.getUuid()).doesNotContain(copy.getUuid());
        assertThat(manuscriptService.findAllAgenticForUser()).extracting(Manuscript::getUuid)
                .contains(copy.getUuid()).doesNotContain(plain.getUuid());
        assertThat(manuscriptService.findAllForUser()).extracting(Manuscript::getUuid)
                .contains(plain.getUuid(), copy.getUuid());
    }

    @Test
    void theSearchIsSplitByTheFlag() throws Exception {
        loginAgentUser();
        Manuscript plain = manuscript();
        Manuscript copy = agenticManuscript();
        ManuscriptFilterValues filter = new ManuscriptFilterValues();

        for (ManuscriptFilterValues values : new ManuscriptFilterValues[]{null, filter}) {
            assertThat(manuscriptService.searchRegularManuscripts(values, new Sorter[0]))
                    .contains(plain.getId()).doesNotContain(copy.getId());
            assertThat(manuscriptService.searchAgenticManuscripts(values, new Sorter[0]))
                    .contains(copy.getId()).doesNotContain(plain.getId());
            assertThat(manuscriptService.searchManuscripts(values, new Sorter[0]))
                    .contains(plain.getId(), copy.getId());
        }
    }

    @Test
    void lorebooksAreSplitByTheFlag() throws Exception {
        loginAgentUser();
        Lorebook plain = lorebook(false);
        Lorebook copy = lorebook(true);

        assertThat(lorebookService.findAllRegularForUser()).extracting(Lorebook::getUuid)
                .contains(plain.getUuid()).doesNotContain(copy.getUuid());
        assertThat(lorebookService.findAllAgenticForUser()).extracting(Lorebook::getUuid)
                .contains(copy.getUuid()).doesNotContain(plain.getUuid());
        assertThat(lorebookService.findAllForUser()).extracting(Lorebook::getUuid)
                .contains(plain.getUuid(), copy.getUuid());
    }

    @Test
    void anotherUserDoesNotSeeTheCopies() throws Exception {
        loginAgentUser();
        Manuscript copy = agenticManuscript();
        Lorebook lore = lorebook(true);

        loginAs(createUser());

        assertThat(manuscriptService.findAllAgenticForUser()).extracting(Manuscript::getUuid).doesNotContain(copy.getUuid());
        assertThat(lorebookService.findAllAgenticForUser()).extracting(Lorebook::getUuid).doesNotContain(lore.getUuid());
    }
}
