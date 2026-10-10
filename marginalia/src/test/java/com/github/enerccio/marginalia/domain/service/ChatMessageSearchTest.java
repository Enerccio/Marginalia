package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-text search over all branches of a book and the choice of the branch that shows a found part.
 * <pre>
 * root ─ a ─ b1
 *         └─ b2 ─ c2 ─ d2
 *      └─ x ─ y ─ z
 *           └─ w
 * </pre>
 */
class ChatMessageSearchTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript manuscript;
    private ChatMessage root, a, b1, b2, c2, d2, x, y, z, w;

    private ChatMessage child(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        Thread.sleep(3);  // messages are ordered by creation time
        return parent == null
                ? chatMessageService.createRoot(manuscript, message)
                : chatMessageService.addChild(parent, message);
    }

    @BeforeEach
    void createTree() throws Exception {
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        manuscript = manuscriptService.save(m);

        root = child(null, "The harbour at dawn");
        a = child(root, "Mara found the lighthouse key");
        b1 = child(a, "ÉTÉ was hot in the harbour");
        b2 = child(a, "Another version of the night");
        c2 = child(b2, "The lamp turned");
        d2 = child(c2, "The key was gone");
        x = child(root, "A storm came");
        y = child(x, "The sea rose");
        z = child(y, "Nobody left");
        w = child(x, "The storm passed");

        activate(b1);
    }

    private void activate(ChatMessage leaf) throws Exception {
        manuscript = manuscriptService.find(manuscript);
        manuscript.setActiveLeaf(leaf);
        manuscript = manuscriptService.save(manuscript);
    }

    private List<Long> ids(String query) throws Exception {
        return chatMessageService.searchFulltext(manuscript, query).stream().map(FulltextHit::id).toList();
    }

    @Test
    void searchesAllBranchesOldestFirst() throws Exception {
        assertThat(ids("harbour")).containsExactly(root.getId(), b1.getId());
        assertThat(ids("key")).containsExactly(a.getId(), d2.getId());
        assertThat(ids("storm")).containsExactly(x.getId(), w.getId());
    }

    @Test
    void matchesAllWordsPhrasesAndIgnoresCase() throws Exception {
        assertThat(ids("KEY mara")).containsExactly(a.getId());
        assertThat(ids("\"the key\"")).containsExactly(d2.getId());
        assertThat(ids("été")).containsExactly(b1.getId());
        assertThat(ids("Été")).containsExactly(b1.getId());
        assertThat(ids("hou*e")).containsExactly(a.getId());
        assertThat(ids("\"light*e key\"")).containsExactly(a.getId());
        assertThat(ids("*")).hasSize(10);
        assertThat(ids("nothing like this")).isEmpty();
        assertThat(ids("  ")).isEmpty();
    }

    @Test
    void likeCharactersAreSearchedAsTheyAre() throws Exception {
        d2.setResponse("Only 100% of it_was gone\\");
        chatMessageService.save(d2);

        assertThat(ids("100%")).containsExactly(d2.getId());
        assertThat(ids("it_was")).containsExactly(d2.getId());
        assertThat(ids("100_")).isEmpty();
        assertThat(ids("it_wa_")).isEmpty();
        assertThat(ids("gone\\")).containsExactly(d2.getId());
        assertThat(ids("%")).containsExactly(d2.getId());
    }

    @Test
    void hitsHaveASnippet() throws Exception {
        FulltextHit hit = chatMessageService.searchFulltext(manuscript, "lighthouse").getFirst();

        assertThat(hit.id()).isEqualTo(a.getId());
        assertThat(hit.snippet()).isEqualTo("Mara found the lighthouse key");
    }

    @Test
    void edits_deletes_and_otherUsersAreRespected() throws Exception {
        d2.setResponse("The lantern was gone");
        chatMessageService.save(d2);
        assertThat(ids("key")).containsExactly(a.getId());
        assertThat(ids("lantern")).containsExactly(d2.getId());

        chatMessageService.delete(a, false);
        assertThat(ids("key")).isEmpty();

        login();
        assertThat(ids("harbour")).isEmpty();
    }

    @Test
    void keepsTheActiveBranchWhenThePartIsOnIt() throws Exception {
        assertThat(chatMessageService.findLeafFor(manuscript, root).getId()).isEqualTo(b1.getId());
        assertThat(chatMessageService.findLeafFor(manuscript, a).getId()).isEqualTo(b1.getId());
        assertThat(chatMessageService.findLeafFor(manuscript, b1).getId()).isEqualTo(b1.getId());
    }

    @Test
    void switchesToTheNearestEndBelowThePartOnAnotherBranch() throws Exception {
        // only one end below
        assertThat(chatMessageService.findLeafFor(manuscript, b2).getId()).isEqualTo(d2.getId());
        assertThat(chatMessageService.findLeafFor(manuscript, c2).getId()).isEqualTo(d2.getId());
        // w is one step below x, z two
        assertThat(chatMessageService.findLeafFor(manuscript, x).getId()).isEqualTo(w.getId());
        assertThat(chatMessageService.findLeafFor(manuscript, y).getId()).isEqualTo(z.getId());

        activate(d2);
        assertThat(chatMessageService.findLeafFor(manuscript, a).getId()).isEqualTo(d2.getId());
        assertThat(chatMessageService.findLeafFor(manuscript, b1).getId()).isEqualTo(b1.getId());
    }

    @Test
    void foreignOrDeletedPartsHaveNoBranch() throws Exception {
        Manuscript other = new Manuscript();
        other.setName(uniqueName("other"));
        other = manuscriptService.save(other);
        assertThat(chatMessageService.findLeafFor(other, a)).isNull();

        chatMessageService.delete(w, false);
        assertThat(chatMessageService.findLeafFor(manuscript, w)).isNull();

        login();
        assertThat(chatMessageService.findLeafFor(manuscript, a)).isNull();
    }
}
