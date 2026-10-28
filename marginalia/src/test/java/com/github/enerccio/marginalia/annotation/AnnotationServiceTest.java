package com.github.enerccio.marginalia.annotation;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.components.PartMerge;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Options;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Annotations of a part: what the service stores, and how the passages follow the text through the edits of the author, the
 * merge of two parts, the branch, the backup and the deletes.
 */
class AnnotationServiceTest extends MarginaliaTestBase {

    private static final String FIRST = "Anna met John at the bar. They talked for hours. Then she left.";
    private static final String SECOND = "The next morning John woke up late.";

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private BackupService backupService;

    @Autowired
    private ManuscriptReplaceService replaceService;

    private Manuscript book;
    private ChatMessage first;
    private ChatMessage second;

    @BeforeEach
    void createStory() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        first = part(null, FIRST);
        second = part(first, SECOND);
    }

    private ChatMessage part(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        ChatMessage saved = parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
        Thread.sleep(2);
        return saved;
    }

    private Annotation annotate(ChatMessage part, String passage, String note) throws Exception {
        String text = chatMessageService.find(part.getId()).getResponse();
        int start = text.indexOf(passage);
        assertThat(start).as("passage in the text").isGreaterThanOrEqualTo(0);
        return annotationService.create(book, part, start, start + passage.length(), note, AnnotationSource.AUTHOR, null, null);
    }

    private Annotation stored(Annotation annotation) throws Exception {
        return annotationService.find(annotation.getId());
    }

    private String passage(ChatMessage part, Annotation annotation) throws Exception {
        Annotation loaded = stored(annotation);
        assertThat(loaded.hasRange()).as("annotation has a range").isTrue();
        return chatMessageService.find(part.getId()).getResponse().substring(loaded.getStart(), loaded.getEnd());
    }

    private void edit(ChatMessage part, String text) throws Exception {
        chatMessageService.storeResponse(book, part, text);
    }

    // ------------------------------------------------------------------------------------------------ create

    @Test
    void createRemembersThePassageAndItsSurroundings() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");

        Annotation loaded = stored(annotation);
        assertThat(loaded.getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(loaded.getSource()).isEqualTo(AnnotationSource.AUTHOR);
        assertThat(loaded.getText()).isEqualTo("Weak scene");
        assertThat(loaded.getQuote()).isEqualTo("They talked for hours.");
        assertThat(loaded.getContextBefore()).isEqualTo(FIRST.substring(0, FIRST.indexOf("They")));
        assertThat(loaded.getContextAfter()).isEqualTo(" Then she left.");
        assertThat(loaded.isSendToPrompt()).isFalse();
        assertThat(loaded.getOwner().getId()).isEqualTo(currentUser.getId());
        assertThat(passage(first, loaded)).isEqualTo("They talked for hours.");
    }

    @Test
    void whiteSpaceAtTheEdgesOfThePassageIsLeftOut() throws Exception {
        int start = FIRST.indexOf(" They");
        Annotation annotation = annotationService.create(book, first, start, start + " They talked ".length(), "Weak",
                AnnotationSource.AUTHOR, null, null);

        assertThat(passage(first, annotation)).isEqualTo("They talked");
    }

    @Test
    void anAnnotationCanBeAboutThePartAsAWhole() throws Exception {
        Annotation annotation = annotationService.create(book, first, null, null, "Rewrite this part", AnnotationSource.AUTHOR, "style", null);

        Annotation loaded = stored(annotation);
        assertThat(loaded.hasRange()).isFalse();
        assertThat(loaded.getType()).isEqualTo("style");
        assertThat(loaded.getQuote()).isNull();
    }

    @Test
    void aPassageThatIsNotInTheTextIsRefused() {
        assertThatThrownBy(() -> annotationService.create(book, first, 5, 9999, "x", AnnotationSource.AUTHOR, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotationService.create(book, first, 9, 9, "x", AnnotationSource.AUTHOR, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotationService.create(book, first, -1, 5, "x", AnnotationSource.AUTHOR, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotationService.create(book, first, 5, null, "x", AnnotationSource.AUTHOR, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotationService.create(book, first, 0, 4, "  ", AnnotationSource.AUTHOR, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aQuoteIsAnchoredToTheTextAndAQuoteThatIsNotThereIsNotLocated() throws Exception {
        Annotation located = annotationService.createFromQuote(book, first, "they talked for hours", null, null, "Weak",
                AnnotationSource.AI, null, "model");
        Annotation lost = annotationService.createFromQuote(book, first, "A dragon landed on the church roof", null, null, "Contradicts the lore",
                AnnotationSource.AI, null, "model");

        assertThat(passage(first, located)).isEqualTo("They talked for hours");
        assertThat(stored(located).isNotLocated()).isFalse();
        Annotation notLocated = stored(lost);
        assertThat(notLocated.hasRange()).isFalse();
        assertThat(notLocated.isNotLocated()).isTrue();
        assertThat(notLocated.getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(notLocated.getQuote()).isEqualTo("A dragon landed on the church roof");
        assertThat(notLocated.getLabel()).isEqualTo("model");
    }

    @Test
    void updateStatusAndReattach() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");

        annotationService.update(book, annotation, "Very weak scene", "pacing", true);
        Annotation updated = stored(annotation);
        assertThat(updated.getText()).isEqualTo("Very weak scene");
        assertThat(updated.getType()).isEqualTo("pacing");
        assertThat(updated.isSendToPrompt()).isTrue();

        // null leaves the value
        annotationService.update(book, annotation, null, null, null);
        assertThat(stored(annotation).getText()).isEqualTo("Very weak scene");
        assertThat(stored(annotation).getType()).isEqualTo("pacing");

        annotationService.setStatus(book, annotation, AnnotationStatus.RESOLVED);
        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.RESOLVED);
        assertThatThrownBy(() -> annotationService.setStatus(book, annotation, AnnotationStatus.ORPHANED))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotationService.update(book, annotation, " ", null, null))
                .isInstanceOf(IllegalArgumentException.class);

        int start = FIRST.indexOf("Then she left.");
        annotationService.reattach(book, annotation, start, start + 4);
        assertThat(passage(first, annotation)).isEqualTo("Then");
        annotationService.reattach(book, annotation, null, null);
        assertThat(stored(annotation).hasRange()).isFalse();
    }

    @Test
    void listsTheAnnotationsOfAPartInTheOrderOfTheText() throws Exception {
        Annotation late = annotate(first, "Then she left.", "late");
        Annotation whole = annotationService.create(book, first, null, null, "whole", AnnotationSource.AUTHOR, null, null);
        Annotation early = annotate(first, "Anna met John", "early");
        annotate(second, "John woke up", "other part");

        assertThat(annotationService.listForPart(book, first)).extracting(Annotation::getId)
                .containsExactly(whole.getId(), early.getId(), late.getId());
        assertThat(annotationService.listForManuscript(book)).hasSize(4);
        assertThat(annotationService.listForPart(book, second)).hasSize(1);
    }

    // ------------------------------------------------------------------------------------------------ edits

    @Test
    void anEditBeforeThePassageMovesIt() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");

        edit(first, "Prologue. " + FIRST);

        assertThat(passage(first, annotation)).isEqualTo("They talked for hours.");
        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.OPEN);
    }

    @Test
    void anEditInsideThePassageChangesItsSize() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");

        edit(first, FIRST.replace("for hours", "for many long hours"));

        assertThat(passage(first, annotation)).isEqualTo("They talked for many long hours.");
        // the quote follows, for the day the offsets can't
        assertThat(stored(annotation).getQuote()).isEqualTo("They talked for many long hours.");
    }

    @Test
    void textTypedAtTheEdgeOfThePassageIsOutsideOfIt() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");

        edit(first, FIRST.replace("They talked for hours.", "Well. They talked for hours. Very much."));

        assertThat(passage(first, annotation)).isEqualTo("They talked for hours.");
    }

    @Test
    void aPassageThatWasDeletedMakesTheAnnotationOrphanedAndATextTypedBackFindsItAgain() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        Annotation closed = annotate(first, "They talked for hours.", "Closed one");
        annotationService.setStatus(book, closed, AnnotationStatus.DISMISSED);

        edit(first, "Anna met John at the bar. Then she left.");

        Annotation orphaned = stored(annotation);
        assertThat(orphaned.getStatus()).isEqualTo(AnnotationStatus.ORPHANED);
        assertThat(orphaned.hasRange()).isFalse();
        assertThat(orphaned.getQuote()).isEqualTo("They talked for hours.");
        // the author closed this one, it stays closed
        assertThat(stored(closed).getStatus()).isEqualTo(AnnotationStatus.DISMISSED);
        assertThat(stored(closed).hasRange()).isFalse();

        edit(first, FIRST);

        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(passage(first, annotation)).isEqualTo("They talked for hours.");
    }

    @Test
    void anOrphanedAnnotationCanBeAttachedToAnotherPassage() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        edit(first, "Anna met John at the bar. Then she left.");
        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.ORPHANED);

        String text = "Anna met John at the bar. Then she left.";
        int start = text.indexOf("Then");
        annotationService.reattach(book, annotation, start - 16, start + 4);

        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(stored(annotation).hasRange()).isTrue();
    }

    @Test
    void aFixedTypoDoesNotLoseAOneWordPassage() throws Exception {
        Annotation annotation = annotate(first, "Anna", "Spelling of the name?");

        edit(first, FIRST.replace("Anna", "Annabel"));

        assertThat(passage(first, annotation)).isEqualTo("Anna");
        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.OPEN);
    }

    @Test
    void aTextReplacedAsAWholeIsFollowedByTheQuote() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        // so different that the words can't be followed one by one: ten thousand other words around the same sentence
        StringBuilder words = new StringBuilder();
        for (int i = 0; i < 2500; i++) {
            words.append("word").append(i).append(' ');
        }
        String replaced = "Intro. " + words + "They talked for hours. " + words + "End.";

        edit(first, replaced);

        assertThat(passage(first, annotation)).isEqualTo("They talked for hours.");
        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.OPEN);
    }

    @Test
    void aTextReplacedAsAWholeOrphansTheAnnotationsWhoseQuoteIsGone() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        StringBuilder words = new StringBuilder();
        for (int i = 0; i < 2500; i++) {
            words.append("word").append(i).append(' ');
        }

        edit(first, "Intro. " + words + "End.");

        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.ORPHANED);
    }

    @Test
    void aSearchAndReplaceMovesThePassagesToo() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        Annotation other = annotate(first, "Then she left.", "Abrupt");

        var plan = replaceService.plan(book, new Options("Anna met John", "Anna and Mary met old John", false, true, false, true, false),
                TaskProgress.NONE);
        replaceService.apply(book, plan, TaskProgress.NONE);

        assertThat(chatMessageService.find(first.getId()).getResponse()).startsWith("Anna and Mary met old John at the bar.");
        assertThat(passage(first, annotation)).isEqualTo("They talked for hours.");
        assertThat(passage(first, other)).isEqualTo("Then she left.");
    }

    @Test
    void theEditOfAnotherPartLeavesTheAnnotationsAlone() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");

        edit(second, "Something else entirely.");

        assertThat(passage(first, annotation)).isEqualTo("They talked for hours.");
    }

    @Test
    void relocateFindsThePassagesByTheirQuotesAfterTheTextWasGeneratedAgain() throws Exception {
        Annotation kept = annotate(first, "They talked for hours.", "Weak scene");
        Annotation gone = annotate(first, "Then she left.", "Abrupt");
        ChatMessage stored = chatMessageService.find(first.getId());
        stored.setResponse("A different opening. They talked for hours. And nothing else happened.");
        chatMessageService.save(stored);

        annotationService.relocate(book, first);

        assertThat(passage(first, kept)).isEqualTo("They talked for hours.");
        assertThat(stored(gone).getStatus()).isEqualTo(AnnotationStatus.ORPHANED);
    }

    // ------------------------------------------------------------------------------------------------ merge, branch

    @Test
    void mergingAPartMovesItsAnnotationsToThePreviousOne() throws Exception {
        Annotation inFirst = annotate(first, "They talked for hours.", "Weak scene");
        Annotation inSecond = annotate(second, "John woke up late", "Why late?");
        Annotation whole = annotationService.create(book, second, null, null, "Whole part", AnnotationSource.AUTHOR, null, null);

        ChatMessage merged = chatMessageService.mergeIntoPrevious(book, second, null, PartMerge.Join.SPACE, Set.of(), Set.of());

        assertThat(merged.getId()).isEqualTo(first.getId());
        assertThat(passage(first, inFirst)).isEqualTo("They talked for hours.");
        assertThat(passage(first, inSecond)).isEqualTo("John woke up late");
        assertThat(stored(whole).getPart().getId()).isEqualTo(first.getId());
        assertThat(stored(whole).isDeleted()).isFalse();
        assertThat(annotationService.listForPart(book, first)).hasSize(3);
    }

    @Test
    void aBranchHasACopyOfTheAnnotations() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        annotationService.setStatus(book, annotation, AnnotationStatus.RESOLVED);

        ChatMessage branch = chatMessageService.branch(book, first);

        List<Annotation> copies = annotationService.listForPart(book, branch);
        assertThat(copies).hasSize(1);
        Annotation copy = copies.getFirst();
        assertThat(copy.getId()).isNotEqualTo(annotation.getId());
        assertThat(copy.getUuid()).isNotEqualTo(annotation.getUuid());
        assertThat(copy.getText()).isEqualTo("Weak scene");
        assertThat(copy.getStatus()).isEqualTo(AnnotationStatus.RESOLVED);
        assertThat(passage(branch, copy)).isEqualTo("They talked for hours.");

        // each follows the text of its own part
        edit(branch, "Prologue. " + FIRST);
        assertThat(passage(branch, copy)).isEqualTo("They talked for hours.");
        assertThat(stored(annotation).getStart()).isEqualTo(FIRST.indexOf("They"));
    }

    // ------------------------------------------------------------------------------------------------ deletes

    @Test
    void deletingAPartDeletesItsAnnotations() throws Exception {
        Annotation inFirst = annotate(first, "They talked for hours.", "Weak scene");
        Annotation inSecond = annotate(second, "John woke up late", "Why late?");

        chatMessageService.delete(chatMessageService.find(second.getId()), false);

        assertThat(stored(inSecond).isDeleted()).isTrue();
        assertThat(stored(inFirst).isDeleted()).isFalse();
        assertThat(annotationService.listForManuscript(book)).extracting(Annotation::getId).containsExactly(inFirst.getId());
    }

    @Test
    void deletingTheBookDeletesTheAnnotationsAndAHardDeleteRemovesThem() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");

        manuscriptService.delete(manuscriptService.find(book.getId()), false);
        assertThat(stored(annotation).isDeleted()).isTrue();

        chatMessageService.hardDeleteForManuscript(manuscriptService.find(book.getId()));
        assertThat(annotationService.find(annotation.getId())).isNull();
    }

    @Test
    void removeDeletesOneAnnotation() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        Annotation other = annotate(first, "Then she left.", "Abrupt");

        annotationService.remove(book, annotation);

        assertThat(annotationService.listForPart(book, first)).extracting(Annotation::getId).containsExactly(other.getId());
    }

    // ------------------------------------------------------------------------------------------------ owners

    @Test
    void anotherUserCanNotSeeOrChangeThem() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        User stranger = createUser();
        loginAs(stranger);

        assertThatThrownBy(() -> annotationService.listForManuscript(book)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotationService.create(book, first, 0, 4, "x", AnnotationSource.AUTHOR, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotationService.setStatus(book, annotation, AnnotationStatus.RESOLVED))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotationService.remove(book, annotation)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anAnnotationOfAnotherBookIsRefused() throws Exception {
        Manuscript other = new Manuscript();
        other.setName(uniqueName("other"));
        other = manuscriptService.save(other);
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");

        Manuscript otherBook = other;
        assertThatThrownBy(() -> annotationService.setStatus(otherBook, annotation, AnnotationStatus.RESOLVED))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotationService.create(otherBook, first, 0, 4, "x", AnnotationSource.AUTHOR, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------------------------------------ backup

    @Test
    void aBackupHasTheAnnotationsAndARestoredBookGetsThemBack() throws Exception {
        Annotation open = annotate(first, "They talked for hours.", "Weak scene");
        annotationService.update(book, open, null, "pacing", true);
        Annotation resolved = annotate(second, "John woke up late", "Why late?");
        annotationService.setStatus(book, resolved, AnnotationStatus.RESOLVED);
        Annotation whole = annotationService.create(book, first, null, null, "Whole part", AnnotationSource.AGENT, null, "agent 1");
        edit(first, "Prologue. " + FIRST);

        JsonObject backup = manuscriptService.createBackup(book);
        Manuscript restored = manuscriptService.cloneFromBackup(backup);

        ChatMessage restoredFirst = partByResponse(restored, "Prologue. " + FIRST);
        ChatMessage restoredSecond = partByResponse(restored, SECOND);
        List<Annotation> inFirst = annotationService.listForPart(restored, restoredFirst);
        assertThat(inFirst).hasSize(2);
        Annotation wholeCopy = inFirst.getFirst();
        assertThat(wholeCopy.hasRange()).isFalse();
        assertThat(wholeCopy.getText()).isEqualTo("Whole part");
        assertThat(wholeCopy.getSource()).isEqualTo(AnnotationSource.AGENT);
        assertThat(wholeCopy.getLabel()).isEqualTo("agent 1");
        assertThat(wholeCopy.getUuid()).isNotEqualTo(whole.getUuid());
        Annotation openCopy = inFirst.getLast();
        assertThat(openCopy.getText()).isEqualTo("Weak scene");
        assertThat(openCopy.getType()).isEqualTo("pacing");
        assertThat(openCopy.isSendToPrompt()).isTrue();
        assertThat(openCopy.getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(passage(restoredFirst, openCopy)).isEqualTo("They talked for hours.");
        assertThat(openCopy.getQuote()).isEqualTo("They talked for hours.");

        List<Annotation> inSecond = annotationService.listForPart(restored, restoredSecond);
        assertThat(inSecond).hasSize(1);
        assertThat(inSecond.getFirst().getStatus()).isEqualTo(AnnotationStatus.RESOLVED);
        assertThat(passage(restoredSecond, inSecond.getFirst())).isEqualTo("John woke up late");

        // the book that was backed up is as it was
        assertThat(annotationService.listForManuscript(book)).hasSize(3);
    }

    @Test
    void cloningABookClonesItsAnnotations() throws Exception {
        annotate(first, "They talked for hours.", "Weak scene");

        Manuscript clone = backupService.cloneManuscript(book, "Clone", false);

        ChatMessage clonedFirst = partByResponse(clone, FIRST);
        assertThat(annotationService.listForPart(clone, clonedFirst)).hasSize(1);
        assertThat(annotationService.listForManuscript(book)).hasSize(1);
    }

    @Test
    void restoringABackupOverTheBookReplacesTheAnnotations() throws Exception {
        annotate(first, "They talked for hours.", "Weak scene");
        JsonObject backup = manuscriptService.createBackup(book);
        annotate(first, "Then she left.", "Added after the backup");

        manuscriptService.restoreBackup(book, true, backup);

        List<Annotation> all = annotationService.listForManuscript(book);
        assertThat(all).extracting(Annotation::getText).containsExactly("Weak scene");
    }

    private ChatMessage partByResponse(Manuscript manuscript, String response) throws Exception {
        return chatMessageService.getAllMessages(manuscript).stream()
                .filter(message -> response.equals(message.getResponse()))
                .findFirst().orElseThrow(() -> new AssertionError("no part with the text " + response));
    }
}
