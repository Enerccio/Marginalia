package com.github.enerccio.marginalia.revision;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.PartEdit;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What the author does with the history of a part: labels and saved versions, putting an earlier version back on top or going
 * back to it, the trash and what empties it.
 */
class PartHistoryActionsTest extends MarginaliaTestBase {

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript book;
    private ChatMessage part;

    // the versions, oldest first: "One." typed, "Two." edited, "Three." edited
    private MessageRevision one;
    private MessageRevision two;
    private MessageRevision three;

    @BeforeEach
    void createPart() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        part = chatMessageService.createPart(book, null, "One.", null);
        one = messageRevisionService.findLatest(part);
        chatMessageService.updateResponse(book, part, "Two.");
        two = messageRevisionService.findLatest(part);
        chatMessageService.updateResponse(book, part, "Three. And more.");
        three = messageRevisionService.findLatest(part);
    }

    private ChatMessage stored() throws Exception {
        return chatMessageService.find(part.getId());
    }

    private List<String> texts(boolean trash) throws Exception {
        List<String> texts = new ArrayList<>();
        for (RevisionRow row : messageRevisionService.list(part, trash).reversed()) {
            texts.add(messageRevisionService.find(row.id()).getResponse());
        }
        return texts;
    }

    @Test
    void aSnapshotIsALabeledCopyOfTheTextTheNewestVersionHasToo() throws Exception {
        MessageRevision snapshot = messageRevisionService.snapshot(book, part, "  before the duel ");

        assertThat(snapshot.getSource()).isEqualTo(RevisionSource.SNAPSHOT);
        assertThat(snapshot.getLabel()).isEqualTo("before the duel");
        assertThat(snapshot.getResponse()).isEqualTo("Three. And more.");
        assertThat(snapshot.getWordCount()).isEqualTo(stored().getWordCount());
        assertThat(texts(false)).containsExactly("One.", "Two.", "Three. And more.", "Three. And more.");
        assertThat(messageRevisionService.list(part, false).getFirst().label()).isEqualTo("before the duel");
        assertThat(messageRevisionService.findLatest(part).getId()).isEqualTo(snapshot.getId());
    }

    @Test
    void aSnapshotKeepsTheReasoningOfTheGeneratedText() throws Exception {
        ChatMessage generated = chatMessageService.createPart(book, part, "Generated.", null, RevisionOrigin.model(RevisionSource.GENERATE,
                "model · protocol", false, "The reasoning."));

        MessageRevision snapshot = messageRevisionService.snapshot(book, generated, "good one");

        assertThat(snapshot.getResponseReasoning()).isEqualTo("The reasoning.");
        // another text than the one of the newest version: no reasoning
        chatMessageService.updateResponse(book, generated, "Edited.");
        assertThat(messageRevisionService.snapshot(book, generated, "edited").getResponseReasoning()).isNull();
    }

    @Test
    void aSnapshotNeedsALabelAndATextAndAPartOfTheBook() throws Exception {
        assertThatThrownBy(() -> messageRevisionService.snapshot(book, part, "  ")).isInstanceOf(IllegalArgumentException.class);
        ChatMessage empty = new ChatMessage();
        empty = chatMessageService.createRoot(book, empty);
        ChatMessage emptyPart = empty;
        assertThatThrownBy(() -> messageRevisionService.snapshot(book, emptyPart, "x")).isInstanceOf(IllegalArgumentException.class);

        Manuscript other = new Manuscript();
        other.setName(uniqueName("other"));
        Manuscript otherBook = manuscriptService.save(other);
        assertThatThrownBy(() -> messageRevisionService.snapshot(otherBook, part, "x")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anyVersionCanBeLabeledAndUnlabeled() throws Exception {
        assertThat(messageRevisionService.label(one, " the start ").getLabel()).isEqualTo("the start");
        assertThat(messageRevisionService.find(one.getId()).getLabel()).isEqualTo("the start");
        assertThat(messageRevisionService.label(one, "another").getLabel()).isEqualTo("another");

        assertThat(messageRevisionService.label(one, "  ").getLabel()).isNull();
        assertThat(messageRevisionService.find(one.getId()).getLabel()).isNull();
        // the text is not touched
        assertThat(messageRevisionService.find(one.getId()).getResponse()).isEqualTo("One.");
    }

    @Test
    void restoringOnTopAddsACopyAndKeepsEverythingBetween() throws Exception {
        PartEdit edit = messageRevisionService.restoreOnTop(book, part, two);

        assertThat(edit.message().getResponse()).isEqualTo("Two.");
        assertThat(stored().getResponse()).isEqualTo("Two.");
        assertThat(stored().getWordCount()).isEqualTo(1);
        assertThat(texts(false)).containsExactly("One.", "Two.", "Three. And more.", "Two.");
        MessageRevision restored = messageRevisionService.findLatest(part);
        assertThat(restored.getSource()).isEqualTo(RevisionSource.RESTORE);
        assertThat(restored.getRestoredFrom()).isEqualTo(two.getUuid());
        assertThat(restored.getId()).isNotEqualTo(two.getId());
        assertThat(messageRevisionService.find(two.getId()).isDeleted()).isFalse();
    }

    @Test
    void restoringBringsBackTheReasoningOfAGeneratedText() throws Exception {
        ChatMessage generated = chatMessageService.createPart(book, part, "Generated.", null, RevisionOrigin.model(RevisionSource.GENERATE,
                "model · protocol", false, "The reasoning."));
        generated = chatMessageService.find(generated.getId());
        generated.setResponseReasoning("The reasoning.");
        chatMessageService.save(generated);
        MessageRevision first = messageRevisionService.findLatest(generated);
        chatMessageService.updateResponse(book, generated, "Edited.");
        chatMessageService.storeResponse(book, generated, "Redrafted.", RevisionOrigin.model(RevisionSource.REDRAFT, "m · p", false, "Redraft reasoning."));

        messageRevisionService.restoreOnTop(book, generated, first);

        MessageRevision restored = messageRevisionService.findLatest(generated);
        assertThat(restored.getResponse()).isEqualTo("Generated.");
        assertThat(restored.getResponseReasoning()).isEqualTo("The reasoning.");
        assertThat(chatMessageService.find(generated.getId()).getResponseReasoning()).isEqualTo("The reasoning.");
    }

    @Test
    void restoringTheTextThePartHasDoesNothing() throws Exception {
        messageRevisionService.restoreOnTop(book, part, three);

        assertThat(texts(false)).containsExactly("One.", "Two.", "Three. And more.");
    }

    @Test
    void aVersionInTheTrashCanBeRestoredOnTop() throws Exception {
        messageRevisionService.trash(one);

        messageRevisionService.restoreOnTop(book, part, one);

        assertThat(stored().getResponse()).isEqualTo("One.");
        assertThat(texts(false)).containsExactly("Two.", "Three. And more.", "One.");
        assertThat(messageRevisionService.find(one.getId()).isDeleted()).isTrue();
    }

    @Test
    void aTextThatWasNotRecordedIsKeptWhenAnEarlierVersionIsRestored() throws Exception {
        ChatMessage direct = stored();
        direct.setResponse("Written directly.");
        chatMessageService.save(direct);

        messageRevisionService.restoreOnTop(book, part, one);

        assertThat(texts(false)).containsExactly("One.", "Two.", "Three. And more.", "Written directly.", "One.");
    }

    @Test
    void rollingBackPutsTheNewerVersionsInTheTrashAndAddsNone() throws Exception {
        PartEdit edit = messageRevisionService.rollBackTo(book, part, two);

        assertThat(edit.message().getResponse()).isEqualTo("Two.");
        assertThat(stored().getResponse()).isEqualTo("Two.");
        assertThat(texts(false)).containsExactly("One.", "Two.");
        assertThat(texts(true)).containsExactly("One.", "Two.", "Three. And more.");
        assertThat(messageRevisionService.find(three.getId()).isDeleted()).isTrue();
        assertThat(messageRevisionService.findLatest(part).getId()).isEqualTo(two.getId());
    }

    @Test
    void aRollBackSendsAnUnrecordedTextToTheTrashToo() throws Exception {
        ChatMessage direct = stored();
        direct.setResponse("Written directly.");
        chatMessageService.save(direct);

        messageRevisionService.rollBackTo(book, part, one);

        assertThat(stored().getResponse()).isEqualTo("One.");
        assertThat(texts(false)).containsExactly("One.");
        assertThat(texts(true)).containsExactly("One.", "Two.", "Three. And more.", "Written directly.");
    }

    @Test
    void rollingBackToAVersionInTheTrashTakesItOut() throws Exception {
        messageRevisionService.rollBackTo(book, part, one);

        messageRevisionService.rollBackTo(book, part, two);

        assertThat(stored().getResponse()).isEqualTo("Two.");
        assertThat(texts(false)).containsExactly("One.", "Two.");
        assertThat(messageRevisionService.find(three.getId()).isDeleted()).isTrue();
    }

    @Test
    void rollingBackToTheNewestVersionChangesNothing() throws Exception {
        messageRevisionService.rollBackTo(book, part, three);

        assertThat(texts(false)).containsExactly("One.", "Two.", "Three. And more.");
        assertThat(texts(true)).hasSize(3);
    }

    @Test
    void theTrashKeepsTheOrderAndUndeleteBringsAVersionBackInItsPlace() throws Exception {
        messageRevisionService.trash(two);

        assertThat(texts(false)).containsExactly("One.", "Three. And more.");
        assertThat(texts(true)).containsExactly("One.", "Two.", "Three. And more.");
        assertThat(messageRevisionService.list(part, true).stream().filter(RevisionRow::deleted).map(RevisionRow::id)).containsExactly(two.getId());

        messageRevisionService.undelete(two);

        assertThat(texts(false)).containsExactly("One.", "Two.", "Three. And more.");
    }

    @Test
    void theNewestVersionCannotBeTrashed() throws Exception {
        assertThatThrownBy(() -> messageRevisionService.trash(three)).isInstanceOf(IllegalStateException.class);

        // after a roll back the newest one that is not in the trash is the text of the part
        messageRevisionService.rollBackTo(book, part, two);
        assertThatThrownBy(() -> messageRevisionService.trash(two)).isInstanceOf(IllegalStateException.class);
        messageRevisionService.trash(one);
        assertThat(texts(false)).containsExactly("Two.");
    }

    @Test
    void onlyAVersionInTheTrashIsPurged() throws Exception {
        assertThatThrownBy(() -> messageRevisionService.purge(one)).isInstanceOf(IllegalStateException.class);
        messageRevisionService.trash(one);

        messageRevisionService.purge(one);

        assertThat(messageRevisionService.find(one.getId())).isNull();
        assertThat(texts(true)).containsExactly("Two.", "Three. And more.");
    }

    @Test
    void emptyingTheTrashOfAPartAndOfTheBookPurgesOnlyTheTrash() throws Exception {
        ChatMessage second = chatMessageService.createPart(book, part, "Second part.", null);
        chatMessageService.updateResponse(book, second, "Second part, edited.");
        messageRevisionService.trash(one);
        // the oldest version of the second part
        messageRevisionService.trash(messageRevisionService.find(messageRevisionService.list(second, false).getLast().id()));

        assertThat(messageRevisionService.emptyTrash(part)).isEqualTo(1);
        assertThat(texts(true)).containsExactly("Two.", "Three. And more.");
        assertThat(messageRevisionService.list(second, true)).hasSize(2);

        assertThat(messageRevisionService.emptyTrash(book)).isEqualTo(1);
        assertThat(messageRevisionService.list(second, true)).hasSize(1);
        assertThat(messageRevisionService.emptyTrash(book)).isZero();
    }

    @Test
    void theTrashOfADeletedPartIsNotEmptiedWithTheBooksTrash() throws Exception {
        ChatMessage second = chatMessageService.createPart(book, part, "Second part.", null);
        chatMessageService.delete(chatMessageService.find(second.getId()), false);

        assertThat(messageRevisionService.emptyTrash(book)).isZero();
        assertThat(messageRevisionService.list(second, true)).hasSize(1);
    }

    @Test
    void anotherUserCannotChangeTheHistory() throws Exception {
        User other = createUser();
        loginAs(other);

        assertThatThrownBy(() -> messageRevisionService.label(one, "mine")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> messageRevisionService.trash(one)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> messageRevisionService.undelete(one)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> messageRevisionService.purge(one)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> messageRevisionService.restoreOnTop(book, part, one)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> messageRevisionService.rollBackTo(book, part, one)).isInstanceOf(IllegalArgumentException.class);
        assertThat(messageRevisionService.emptyTrash(part)).isZero();
    }

    @Test
    void aVersionOfAnotherPartIsNotRestored() throws Exception {
        ChatMessage second = chatMessageService.createPart(book, part, "Second part.", null);
        MessageRevision foreign = messageRevisionService.findLatest(second);

        assertThatThrownBy(() -> messageRevisionService.restoreOnTop(book, part, foreign)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> messageRevisionService.rollBackTo(book, part, foreign)).isInstanceOf(IllegalArgumentException.class);
        assertThat(stored().getResponse()).isEqualTo("Three. And more.");
    }
}
