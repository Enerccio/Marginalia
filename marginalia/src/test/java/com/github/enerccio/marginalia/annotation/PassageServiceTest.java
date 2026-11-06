package com.github.enerccio.marginalia.annotation;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Passages of an extension's kind (rows of the table of the annotations that are not editorial notes): they are anchored to the
 * text the way the notes are, and are not in the lists of the notes.
 */
class PassageServiceTest extends MarginaliaTestBase {

    private static final String KIND = "test-kind";
    private static final String TEXT = "Anna met John at the bar. \"Hello there,\" said John. Then she left.";

    @Autowired
    private PassageService passageService;

    @Autowired
    private PassageKinds passageKinds;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript book;
    private ChatMessage part;
    private PassageKind kind;

    @BeforeEach
    void createStory() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        ChatMessage message = new ChatMessage();
        message.setResponse(TEXT);
        part = chatMessageService.createRoot(book, message);
        kind = () -> KIND;
        passageKinds.register(kind);
    }

    @AfterEach
    void unregister() {
        passageKinds.unregister(kind);
    }

    private AnchoredPassage passage(String quoted) throws Exception {
        int start = TEXT.indexOf(quoted);
        return passageService.create(book, part, KIND, start, start + quoted.length());
    }

    private String text(AnchoredPassage passage) throws Exception {
        AnchoredPassage loaded = passageService.find(passage.getId());
        assertThat(loaded.hasRange()).isTrue();
        return chatMessageService.find(part.getId()).getResponse().substring(loaded.getStart(), loaded.getEnd());
    }

    @Test
    void aPassageHasItsKindItsRangeAndTheDataOfTheExtension() throws Exception {
        AnchoredPassage created = passage("\"Hello there,\"");
        created.getAttributes().addProperty("source", "Greetings");
        passageService.save(created);

        AnchoredPassage loaded = passageService.find(created.getId());
        assertThat(loaded).isNotInstanceOf(Annotation.class);
        assertThat(loaded.getPassageKind()).isEqualTo(KIND);
        assertThat(loaded.getQuote()).isEqualTo("\"Hello there,\"");
        assertThat(loaded.getContextBefore()).endsWith("bar. ");
        assertThat(loaded.getAttributes().get("source").getAsString()).isEqualTo("Greetings");
        assertThat(text(created)).isEqualTo("\"Hello there,\"");
    }

    @Test
    void aKindIsRequiredAndAPassageThatIsNotInTheTextIsRefused() {
        assertThatThrownBy(() -> passageService.create(book, part, " ", 0, 4)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> passageService.create(book, part, KIND, 0, TEXT.length() + 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aQuoteIsAnchoredAndAQuoteThatIsNotThereIsNotLocated() throws Exception {
        AnchoredPassage found = passageService.createFromQuote(book, part, KIND, "Then she left.", null, null);
        AnchoredPassage lost = passageService.createFromQuote(book, part, KIND, "never written", null, null);

        assertThat(text(found)).isEqualTo("Then she left.");
        assertThat(lost.hasRange()).isFalse();
        assertThat(lost.isNotLocated()).isTrue();
    }

    @Test
    void passagesAreNotInTheListsOfTheNotesAndTheNotesAreNotInTheirs() throws Exception {
        AnchoredPassage passage = passage("Then she left.");
        Annotation note = annotationService.create(book, part, 0, 4, "Anna?", AnnotationSource.AUTHOR, null, null);

        assertThat(annotationService.listForPart(book, part)).extracting(Annotation::getId).containsExactly(note.getId());
        assertThat(annotationService.listForManuscript(book)).extracting(Annotation::getId).containsExactly(note.getId());
        assertThat(passageService.listForPart(book, part, KIND)).extracting(AnchoredPassage::getId).containsExactly(passage.getId());
        assertThat(passageService.listForManuscript(book, null)).extracting(AnchoredPassage::getId).containsExactly(passage.getId());
        assertThat(passageService.listForManuscript(book, "another-kind")).isEmpty();
    }

    @Test
    void thePassagesOfSeveralPartsAreReadInOneCall() throws Exception {
        AnchoredPassage first = passage("Then she left.");
        ChatMessage branch = chatMessageService.branch(book, part);
        passageService.create(book, part, "another-kind", 0, 4);

        List<AnchoredPassage> read = passageService.listForParts(List.of(part.getId(), branch.getId()), KIND);

        assertThat(read).hasSize(2);
        assertThat(read).extracting(AnchoredPassage::getPartId).containsExactlyInAnyOrder(part.getId(), branch.getId());
        assertThat(read).extracting(AnchoredPassage::getId).contains(first.getId());
        assertThat(passageService.listForParts(List.of(part.getId()), null)).hasSize(2);
        assertThat(passageService.listForParts(List.of(), KIND)).isEmpty();
        assertThat(passageService.listForParts(null, KIND)).isEmpty();
    }

    @Test
    void anEditMovesPassagesOfAllKinds() throws Exception {
        AnchoredPassage passage = passage("\"Hello there,\"");
        Annotation note = annotationService.create(book, part, 0, 4, "Anna?", AnnotationSource.AUTHOR, null, null);

        chatMessageService.storeResponse(book, part, "Prologue. " + TEXT);

        assertThat(text(passage)).isEqualTo("\"Hello there,\"");
        assertThat(chatMessageService.find(part.getId()).getResponse().substring(annotationService.find(note.getId()).getStart())).startsWith("Anna");
    }

    @Test
    void aPassageThatWasDeletedIsOrphanedAndComesBackWithTheText() throws Exception {
        AnchoredPassage passage = passage("Then she left.");

        chatMessageService.storeResponse(book, part, "Anna met John at the bar.");

        AnchoredPassage orphaned = passageService.find(passage.getId());
        assertThat(orphaned.isOrphaned()).isTrue();
        assertThat(orphaned.hasRange()).isFalse();

        chatMessageService.storeResponse(book, part, TEXT);
        assertThat(passageService.find(passage.getId()).isOrphaned()).isFalse();
        assertThat(text(passage)).isEqualTo("Then she left.");
    }

    @Test
    void aBranchHasACopyOfThePassageWithItsData() throws Exception {
        AnchoredPassage passage = passage("\"Hello there,\"");
        passage.getAttributes().addProperty("source", "Greetings");
        passageService.save(passage);

        ChatMessage branch = chatMessageService.branch(book, part);

        var copies = passageService.listForPart(book, branch, KIND);
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getId()).isNotEqualTo(passage.getId());
        assertThat(copies.getFirst().getAttributes().get("source").getAsString()).isEqualTo("Greetings");
        assertThat(copies.getFirst().getQuote()).isEqualTo("\"Hello there,\"");
    }

    @Test
    void aBackupKeepsTheKindOfThePassages() throws Exception {
        AnchoredPassage passage = passage("\"Hello there,\"");
        passage.getAttributes().addProperty("source", "Greetings");
        passageService.save(passage);
        Annotation note = annotationService.create(book, part, 0, 4, "Anna?", AnnotationSource.AUTHOR, null, null);

        JsonObject backup = manuscriptService.createBackup(book);
        Manuscript restored = manuscriptService.cloneFromBackup(backup);

        ChatMessage restoredPart = chatMessageService.getAllMessages(restored).getFirst();
        var passages = passageService.listForPart(restored, restoredPart, KIND);
        assertThat(passages).hasSize(1);
        assertThat(passages.getFirst()).isNotInstanceOf(Annotation.class);
        assertThat(passages.getFirst().getUuid()).isNotEqualTo(passage.getUuid());
        assertThat(passages.getFirst().getAttributes().get("source").getAsString()).isEqualTo("Greetings");
        assertThat(annotationService.listForPart(restored, restoredPart)).extracting(Annotation::getText).containsExactly("Anna?");
        assertThat(note.getUuid()).isNotNull();
    }

    @Test
    void deletingThePartDeletesItsPassages() throws Exception {
        AnchoredPassage passage = passage("Then she left.");
        ChatMessage child = chatMessageService.addChild(part, new ChatMessage());
        assertThat(child).isNotNull();

        chatMessageService.delete(chatMessageService.find(part.getId()), false);

        assertThat(passageService.find(passage.getId()).isDeleted()).isTrue();
    }

    @Test
    void removeAndReattach() throws Exception {
        AnchoredPassage passage = passage("Then she left.");
        AnchoredPassage moved = passageService.reattach(book, passage, 0, 4);
        assertThat(text(moved)).isEqualTo("Anna");

        passageService.remove(book, moved);

        assertThat(passageService.listForPart(book, part, KIND)).isEmpty();
    }

    @Test
    void theRegistryKnowsTheKindUntilItIsUnregistered() {
        assertThat(passageKinds.get(KIND)).isSameAs(kind);
        passageKinds.unregister(kind);
        assertThat(passageKinds.get(KIND)).isNull();
        assertThat(passageKinds.all()).doesNotContain(kind);
    }

    @Test
    void aKindIsToldWhatAReplaceChangedInsideItsPassage() throws Exception {
        List<String> told = new java.util.ArrayList<>();
        PassageKind watching = new PassageKind() {
            @Override
            public String id() {
                return "watching-kind";
            }

            @Override
            public boolean textReplaced(AnchoredPassage passage, String before, String after) {
                told.add(before + " -> " + after);
                passage.getAttributes().addProperty("now", after);
                return true;
            }
        };
        passageKinds.register(watching);
        try {
            int start = TEXT.indexOf("\"Hello there,\"");
            AnchoredPassage speech = passageService.create(book, part, "watching-kind", start, start + "\"Hello there,\"".length());
            AnchoredPassage other = passage("Then she left.");
            String replaced = TEXT.replace("Hello there", "Good evening");

            passageService.shiftReplaced(book, part, TEXT, replaced);

            assertThat(told).containsExactly("\"Hello there,\" -> \"Good evening,\"");
            assertThat(passageService.find(speech.getId()).getAttributes().get("now").getAsString()).isEqualTo("\"Good evening,\"");
            // the passage of a kind that does not care and a replace outside every passage
            passageService.shiftReplaced(book, part, replaced, replaced.replace("bar", "pub"));
            assertThat(told).hasSize(1);
            assertThat(passageService.find(other.getId()).getAttributes().has("now")).isFalse();
        } finally {
            passageKinds.unregister(watching);
        }
    }
}
