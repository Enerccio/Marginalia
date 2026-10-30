package com.github.enerccio.marginalia.revision;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionData;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The versions of a part ({@link MessageRevision}): recorded in order with who wrote them, kept apart from the part, listed
 * without their text, deleted and restored with the part and the book.
 */
class MessageRevisionServiceTest extends MarginaliaTestBase {

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TrashService trashService;

    @Autowired
    private CleanupService cleanupService;

    private Manuscript book;
    private ChatMessage first;
    private ChatMessage second;

    @BeforeEach
    void createStory() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        first = part(null, "First.");
        second = part(first, "Second.");
    }

    private ChatMessage part(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private ChatMessage edit(ChatMessage part, String text) throws Exception {
        ChatMessage stored = chatMessageService.find(part.getId());
        stored.setResponse(text);
        stored.setWordCount(text.split("\\s+").length);
        return chatMessageService.save(stored);
    }

    @Test
    void aPartWithoutVersionsHasNone() throws Exception {
        assertThat(messageRevisionService.findLatest(first)).isNull();
        assertThat(messageRevisionService.list(first, true)).isEmpty();
        assertThat(messageRevisionService.countFor(List.of(first, second))).isEmpty();
    }

    @Test
    void theVersionsAreNumberedAndTheNewestMirrorsThePart() throws Exception {
        MessageRevision one = messageRevisionService.record(chatMessageService.find(first.getId()), RevisionSource.HUMAN, null, false);
        MessageRevision two = messageRevisionService.record(edit(first, "First, edited."), RevisionSource.HUMAN, null, false);

        assertThat(one.getOwner().getId()).isEqualTo(first.getOwner().getId());
        assertThat(messageRevisionService.findLatest(first).getResponse()).isEqualTo("First, edited.");
        assertThat(messageRevisionService.list(first, false)).extracting(RevisionRow::id).containsExactly(two.getId(), one.getId());
        assertThat(messageRevisionService.countFor(List.of(first, second))).containsOnly(java.util.Map.entry(first.getId(), 2L));
    }

    @Test
    void versionsMadeInTheSameMomentKeepTheOrderTheyWereMadeIn() throws Exception {
        List<Long> made = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            made.add(messageRevisionService.record(edit(first, "Text " + i), RevisionSource.HUMAN, null, false).getId());
        }

        assertThat(messageRevisionService.list(first, false)).extracting(RevisionRow::id).containsExactlyElementsOf(made.reversed());
        assertThat(messageRevisionService.findLatest(first).getId()).isEqualTo(made.getLast());
    }

    @Test
    void aTextThatDidNotChangeAndAnEmptyTextMakeNoVersion() throws Exception {
        messageRevisionService.record(chatMessageService.find(first.getId()), RevisionSource.HUMAN, null, false);

        assertThat(messageRevisionService.record(chatMessageService.find(first.getId()), RevisionSource.PLUGIN, "x", false)).isNull();
        assertThat(messageRevisionService.record(edit(first, ""), RevisionSource.HUMAN, null, false)).isNull();
        assertThat(messageRevisionService.list(first, true)).hasSize(1);
    }

    @Test
    void theSourceTheDetailAndTheAgentFlagAreKept() throws Exception {
        MessageRevision revision = messageRevisionService.record(chatMessageService.find(first.getId()), RevisionSource.PLUGIN,
                "  reviewer  ", true);

        RevisionRow row = messageRevisionService.list(first, false).getFirst();
        assertThat(row.id()).isEqualTo(revision.getId());
        assertThat(row.uuid()).isEqualTo(revision.getUuid());
        assertThat(row.source()).isEqualTo(RevisionSource.PLUGIN);
        assertThat(row.sourceDetail()).isEqualTo("reviewer");
        assertThat(row.agent()).isTrue();
        assertThat(row.label()).isNull();
        assertThat(row.deleted()).isFalse();
        assertThat(row.creation()).isNotNull();
    }

    @Test
    void theReasoningIsKeptOnlyForAModelSource() throws Exception {
        ChatMessage stored = chatMessageService.find(first.getId());
        stored.setResponseReasoning("They should meet early.");
        stored = chatMessageService.save(stored);

        MessageRevision generated = messageRevisionService.record(stored, RevisionSource.GENERATE, "model · protocol", false);
        MessageRevision typed = messageRevisionService.record(edit(first, "First, typed."), RevisionSource.HUMAN, null, false);

        assertThat(messageRevisionService.find(generated.getId()).getResponseReasoning()).isEqualTo("They should meet early.");
        assertThat(messageRevisionService.find(typed.getId()).getResponseReasoning()).isNull();
    }

    @Test
    void aWriterCanGiveItsOwnReasoningAndTextAndCounts() throws Exception {
        // a redraft has a reasoning of its own, the part still has the one of the generation
        ChatMessage stored = chatMessageService.find(first.getId());
        stored.setResponseReasoning("The reasoning of the generation.");
        stored = chatMessageService.save(stored);

        MessageRevision redrafted = messageRevisionService.record(stored, new RevisionData(RevisionSource.REDRAFT, "model · protocol",
                false, "First, redrafted in a new voice.", "The reasoning of the redraft.", null, 5, 12L));

        MessageRevision loaded = messageRevisionService.find(redrafted.getId());
        assertThat(loaded.getResponse()).isEqualTo("First, redrafted in a new voice.");
        assertThat(loaded.getResponseReasoning()).isEqualTo("The reasoning of the redraft.");
        assertThat(loaded.getSource()).isEqualTo(RevisionSource.REDRAFT);
        assertThat(loaded.getWordCount()).isEqualTo(5);
        assertThat(loaded.getTokenCount()).isEqualTo(12L);
        // the text of the part was not touched by it
        assertThat(chatMessageService.find(first.getId()).getResponse()).isEqualTo("First.");
        assertThat(chatMessageService.find(first.getId()).getResponseReasoning()).isEqualTo("The reasoning of the generation.");
    }

    @Test
    void missingCountsAreWorkedOutAndALabelIsKept() throws Exception {
        ChatMessage stored = chatMessageService.find(first.getId());

        MessageRevision other = messageRevisionService.record(stored,
                new RevisionData(RevisionSource.SNAPSHOT, null, false, "One two three.", null, "  the label  ", null, null));
        MessageRevision same = messageRevisionService.record(chatMessageService.find(second.getId()),
                new RevisionData(RevisionSource.HUMAN, null, false, "Second.", null, null, null, null));

        // the tokens of the part are no use for the text of another one
        assertThat(messageRevisionService.find(other.getId()).getWordCount()).isEqualTo(3);
        assertThat(messageRevisionService.find(other.getId()).getTokenCount()).isZero();
        assertThat(messageRevisionService.find(other.getId()).getLabel()).isEqualTo("the label");
        assertThat(messageRevisionService.find(same.getId()).getWordCount()).isEqualTo(1);
        assertThat(messageRevisionService.find(same.getId()).getTokenCount()).isEqualTo(chatMessageService.find(second.getId()).getTokenCount());
        assertThat(messageRevisionService.list(first, false).getFirst().label()).isEqualTo("the label");
    }

    @Test
    void theDataOfThePartDropsTheReasoningOfAnotherWriter() throws Exception {
        ChatMessage stored = chatMessageService.find(first.getId());
        stored.setResponseReasoning("The reasoning of the generation.");
        stored = chatMessageService.save(stored);

        assertThat(RevisionData.of(stored, RevisionSource.HUMAN, null, false).reasoning()).isNull();
        assertThat(RevisionData.of(stored, RevisionSource.REGENERATE, null, false).reasoning()).isEqualTo("The reasoning of the generation.");
        assertThat(RevisionData.of(stored, null, null, false).source()).isEqualTo(RevisionSource.HUMAN);
    }

    @Test
    void theTextIsInTheExtendedContentAndNotInThePart() throws Exception {
        String text = ("Příliš žluťoučký kůň úpěl ďábelské ódy. 吾輩は猫である。\n").repeat(3000);
        MessageRevision revision = messageRevisionService.record(edit(first, text), RevisionSource.HUMAN, null, false);

        assertThat(messageRevisionService.find(revision.getId()).getResponse()).isEqualTo(text);
        assertThat(new String(chatMessageService.find(first.getId()).getExtendedContent(), StandardCharsets.UTF_8))
                .doesNotContain("revision");
    }

    @Test
    void theTrashIsListedOnlyOnRequestAndTheOrderStaysTheOrderTheyWereMade() throws Exception {
        MessageRevision one = messageRevisionService.record(chatMessageService.find(first.getId()), RevisionSource.HUMAN, null, false);
        MessageRevision two = messageRevisionService.record(edit(first, "Two."), RevisionSource.HUMAN, null, false);
        messageRevisionService.delete(messageRevisionService.find(two.getId()), false);

        assertThat(messageRevisionService.list(first, false)).extracting(RevisionRow::id).containsExactly(one.getId());
        assertThat(messageRevisionService.list(first, true)).extracting(RevisionRow::id).containsExactly(two.getId(), one.getId());
        assertThat(messageRevisionService.list(first, true).getFirst().deleted()).isTrue();
        assertThat(messageRevisionService.findLatest(first).getId()).isEqualTo(one.getId());
        assertThat(messageRevisionService.countFor(List.of(first))).containsEntry(first.getId(), 1L);

        MessageRevision three = messageRevisionService.record(edit(first, "Three."), RevisionSource.HUMAN, null, false);
        assertThat(messageRevisionService.list(first, true)).extracting(RevisionRow::id).containsExactly(three.getId(), two.getId(), one.getId());
    }

    @Test
    void anotherUserDoesNotSeeTheVersions() throws Exception {
        messageRevisionService.record(chatMessageService.find(first.getId()), RevisionSource.HUMAN, null, false);
        User other = createUser();

        loginAs(other);
        assertThat(messageRevisionService.list(first, true)).isEmpty();
        assertThat(messageRevisionService.countFor(List.of(first))).isEmpty();
        assertThat(messageRevisionService.findLatest(first)).isNull();
        assertThat(messageRevisionService.findAllForUser()).isEmpty();
    }

    @Test
    void deletingAPartDeletesItsVersionsAndRestoringItBringsThemBack() throws Exception {
        messageRevisionService.record(chatMessageService.find(first.getId()), RevisionSource.HUMAN, null, false);
        messageRevisionService.record(chatMessageService.find(second.getId()), RevisionSource.HUMAN, null, false);

        chatMessageService.delete(chatMessageService.find(second.getId()), false);
        assertThat(messageRevisionService.list(second, false)).isEmpty();
        assertThat(messageRevisionService.list(second, true)).hasSize(1);
        assertThat(messageRevisionService.list(first, false)).hasSize(1);

        trashService.restore(List.of(new EntityKey(ChatMessage.class, second.getId())));
        assertThat(messageRevisionService.list(second, false)).hasSize(1);
    }

    @Test
    void aHardDeleteRemovesTheVersions() throws Exception {
        MessageRevision stored = messageRevisionService.record(chatMessageService.find(second.getId()), RevisionSource.HUMAN, null, false);

        chatMessageService.delete(chatMessageService.find(second.getId()), true);

        assertThat(messageRevisionService.find(stored.getId())).isNull();
    }

    @Test
    void deletingTheBookDeletesTheVersionsAndAHardDeleteRemovesThem() throws Exception {
        MessageRevision stored = messageRevisionService.record(chatMessageService.find(first.getId()), RevisionSource.HUMAN, null, false);

        manuscriptService.delete(manuscriptService.find(book.getId()), false);
        assertThat(messageRevisionService.find(stored.getId()).isDeleted()).isTrue();

        chatMessageService.hardDeleteForManuscript(manuscriptService.find(book.getId()));
        assertThat(messageRevisionService.find(stored.getId())).isNull();
    }

    @Test
    void theCleanupPurgesTheVersionsWithThePart() throws Exception {
        MessageRevision stored = messageRevisionService.record(chatMessageService.find(second.getId()), RevisionSource.HUMAN, null, false);
        chatMessageService.delete(chatMessageService.find(second.getId()), false);

        loginAdmin();
        cleanupService.purge();

        assertThat(chatMessageService.find(second.getId())).isNull();
        assertThat(messageRevisionService.find(stored.getId())).isNull();
    }

    @Test
    void aPartThatIsNotSavedHasNoVersions() {
        assertThatThrownBy(() -> messageRevisionService.record(new ChatMessage(), RevisionSource.HUMAN, null, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
