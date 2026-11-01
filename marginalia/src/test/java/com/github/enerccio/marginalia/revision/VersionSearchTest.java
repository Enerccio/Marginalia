package com.github.enerccio.marginalia.revision;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionData;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Options;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The older versions of a part are found only when the search asks for them: the library search, the search in the book and
 * what both are made of. The main search and Replace never see them.
 * <pre>
 * root "The harbour at dawn" (edited to "Fog over the water") ─ a "Mara found the key" (active)
 *                                                          └─ b "Another harbour night" (not active)
 * </pre>
 */
class VersionSearchTest extends MarginaliaTestBase {

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ManuscriptReplaceService replaceService;

    private Manuscript book;
    private ChatMessage root, a, b;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        root = chatMessageService.createPart(book, null, "The harbour at dawn", null);
        Thread.sleep(3);
        a = chatMessageService.createPart(book, root, "Mara found the key", null);
        Thread.sleep(3);
        b = chatMessageService.createPart(book, root, "Another harbour night", null);
        book = manuscriptService.find(book.getId());
        book.setActiveLeaf(a);
        book = manuscriptService.save(book);
        chatMessageService.updateResponse(book, root, "Fog over the water");
    }

    private List<FulltextHit> search(String query, boolean includeVersions) throws Exception {
        return chatMessageService.searchFulltext(book, query, includeVersions);
    }

    private MessageRevision versionOf(ChatMessage part, String text) throws Exception {
        for (RevisionRow row : messageRevisionService.list(part, true)) {
            MessageRevision revision = messageRevisionService.find(row.id());
            if (text.equals(revision.getResponse())) {
                return revision;
            }
        }
        throw new AssertionError("no version " + text);
    }

    private List<Long> library(String text, boolean allBranches, boolean includeVersions) throws Exception {
        ManuscriptSearchCriteria criteria = new ManuscriptSearchCriteria();
        criteria.setName(book.getName());
        criteria.setFulltext(text);
        criteria.setAllBranches(allBranches);
        criteria.setIncludeVersions(includeVersions);
        return manuscriptService.searchAdvanced(criteria);
    }

    @Test
    void theMainSearchDoesNotSeeTheOldText() throws Exception {
        assertThat(search("dawn", false)).isEmpty();
        assertThat(search("fog", false)).extracting(FulltextHit::id).containsExactly(root.getId());
    }

    @Test
    void anOldVersionIsFoundWhenAskedForWithTheVersionItWasFoundIn() throws Exception {
        List<FulltextHit> hits = search("dawn", true);

        assertThat(hits).hasSize(1);
        FulltextHit hit = hits.getFirst();
        assertThat(hit.id()).isEqualTo(root.getId());
        assertThat(hit.isVersion()).isTrue();
        assertThat(hit.version()).isEqualTo(versionOf(root, "The harbour at dawn").getUuid());
        assertThat(hit.versionSource()).isEqualTo(RevisionSource.HUMAN);
        assertThat(hit.versionTime()).isNotNull();
        assertThat(hit.snippet()).contains("dawn");
    }

    @Test
    void aPartThatHasTheWordsItselfIsNotAVersionHit() throws Exception {
        chatMessageService.updateResponse(book, root, "Dawn again");

        List<FulltextHit> hits = search("dawn", true);

        assertThat(hits).hasSize(1);
        assertThat(hits.getFirst().isVersion()).as("the text of the part wins").isFalse();
        assertThat(hits.getFirst().snippet()).contains("Dawn again");
    }

    @Test
    void theNewestVersionWithTheWordsIsShown() throws Exception {
        chatMessageService.updateResponse(book, root, "Dusk at the harbour");
        chatMessageService.updateResponse(book, root, "Only the sea");

        FulltextHit hit = search("harbour", true).stream().filter(h -> h.id().equals(root.getId())).findFirst().orElseThrow();

        assertThat(hit.version()).isEqualTo(versionOf(root, "Dusk at the harbour").getUuid());
    }

    @Test
    void theHitsOfTheTextComeBeforeTheOnesOfTheVersions() throws Exception {
        List<FulltextHit> hits = search("harbour", true);

        assertThat(hits).extracting(FulltextHit::id).containsExactly(b.getId(), root.getId());
        assertThat(hits.get(0).isVersion()).isFalse();
        assertThat(hits.get(1).isVersion()).isTrue();
    }

    @Test
    void aVersionInTheTrashIsNotFound() throws Exception {
        messageRevisionService.trash(versionOf(root, "The harbour at dawn"));

        assertThat(search("dawn", true)).isEmpty();

        messageRevisionService.undelete(versionOf(root, "The harbour at dawn"));
        assertThat(search("dawn", true)).hasSize(1);
    }

    @Test
    void aPurgedVersionIsNotFound() throws Exception {
        messageRevisionService.trash(versionOf(root, "The harbour at dawn"));
        messageRevisionService.purge(versionOf(root, "The harbour at dawn"));

        assertThat(search("dawn", true)).isEmpty();
    }

    @Test
    void theVersionsOfADeletedPartAreNotFound() throws Exception {
        chatMessageService.delete(chatMessageService.find(root.getId()), false);

        assertThat(search("dawn", true)).isEmpty();
    }

    @Test
    void theReasoningIsNotSearched() throws Exception {
        messageRevisionService.record(chatMessageService.find(a.getId()), new RevisionData(RevisionSource.GENERATE, null, false,
                "Quiet room", "I will mention the lighthouse here", null, null, null));

        assertThat(search("lighthouse", true)).isEmpty();
        assertThat(search("quiet", true)).hasSize(1);
    }

    @Test
    void anotherUserFindsNothing() throws Exception {
        loginAs(createUser());

        assertThat(search("dawn", true)).isEmpty();
        assertThat(messageRevisionService.searchFulltext(book, "dawn")).isEmpty();
        assertThat(library("dawn", true, true)).isEmpty();
    }

    @Test
    void theLibrarySearchFindsABookThroughAVersionOnlyWhenAskedFor() throws Exception {
        assertThat(library("dawn", true, false)).doesNotContain(book.getId());
        assertThat(library("dawn", true, true)).contains(book.getId());
        assertThat(library("fog", true, false)).contains(book.getId());
    }

    @Test
    void theLibrarySearchOfTheActiveBranchOnlyCountsTheVersionsOfThatBranch() throws Exception {
        chatMessageService.updateResponse(book, b, "Nothing more");

        // "harbour" was in the part b of the branch that is not active, and in the first version of the root
        assertThat(library("night", false, true)).as("the old text of b is on another branch").doesNotContain(book.getId());
        assertThat(library("night", true, true)).contains(book.getId());
        assertThat(library("dawn", false, true)).as("root is on the active branch").contains(book.getId());
    }

    @Test
    void replaceDoesNotTouchTheVersions() throws Exception {
        chatMessageService.updateResponse(book, root, "The harbour again");

        replaceService.apply(book, replaceService.plan(book, new Options("harbour", "port", false, true, false, false, true), TaskProgress.NONE), TaskProgress.NONE);

        assertThat(chatMessageService.find(root.getId()).getResponse()).isEqualTo("The port again");
        assertThat(versionOf(root, "The harbour at dawn").getResponse()).isEqualTo("The harbour at dawn");
        assertThat(versionOf(root, "The harbour again").getResponse()).isEqualTo("The harbour again");
        assertThat(search("harbour", false).stream().map(FulltextHit::id)).containsExactly(b.getId());
    }
}
