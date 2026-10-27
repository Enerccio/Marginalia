package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotateService.Mode;
import com.github.enerccio.marginalia.domain.service.AnnotateService.Proposal;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The window that lets a model annotate a part: the notes it proposes are listed with a check box each, and only the chosen ones
 * become annotations. (Running the model is {@code AnnotateServiceTest}, it needs a UI with a session.)
 */
class AnnotateDialogTest extends MarginaliaTestBase {

    private static final String TEXT = "Anna met John at the bar. They talked for hours.";

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private AnnotationService annotationService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private Manuscript book;
    private ChatMessage part;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        ChatMessage message = new ChatMessage();
        message.setResponse(TEXT);
        part = chatMessageService.createRoot(book, message);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void startsWithTheAutomaticModeAndNothingToAdd() {
        AnnotateDialog dialog = AnnotateDialog.create(book, part, () -> {
        });

        assertThat(dialog.modeField().getValue()).isEqualTo(Mode.AUTO);
        assertThat(dialog.addButton().isEnabled()).isFalse();
        assertThat(dialog.runButton().getText()).isEqualTo("Annotate");
    }

    @Test
    void everyProposalIsChosenAtFirstAndTheChosenOnesAreAdded() throws Exception {
        AtomicInteger added = new AtomicInteger();
        AnnotateDialog dialog = AnnotateDialog.create(book, part, added::incrementAndGet);
        Proposal weak = new Proposal("They talked for hours.", "Weak scene", "pacing", 26, 48);
        Proposal skipped = new Proposal("Anna", "Name?", null, 0, 4);
        Proposal lost = new Proposal("A dragon", "Contradicts the lore", null, null, null);
        dialog.setModelNameForTest("mock-model");

        dialog.showProposalsForTest(List.of(weak, skipped, lost));

        assertThat(dialog.proposalChecks().values()).allMatch(check -> check.getValue());
        assertThat(dialog.addButton().isEnabled()).isTrue();
        dialog.proposalChecks().get(skipped).setValue(false);

        dialog.acceptForTest();

        List<Annotation> all = annotationService.listForPart(book, part);
        assertThat(all).extracting(Annotation::getText).containsExactlyInAnyOrder("Weak scene", "Contradicts the lore");
        assertThat(all).allMatch(annotation -> annotation.getSource() == AnnotationSource.AI && "mock-model".equals(annotation.getLabel()));
        assertThat(all).filteredOn(Annotation::isNotLocated).hasSize(1);
        assertThat(added.get()).isEqualTo(1);
    }

    @Test
    void nothingChosenIsNothingToAdd() throws Exception {
        AnnotateDialog dialog = AnnotateDialog.create(book, part, () -> {
        });
        Proposal only = new Proposal("Anna", "Name?", null, 0, 4);
        dialog.showProposalsForTest(List.of(only));

        dialog.proposalChecks().get(only).setValue(false);

        assertThat(dialog.addButton().isEnabled()).isFalse();
        dialog.acceptForTest();
        assertThat(annotationService.listForPart(book, part)).isEmpty();
    }

    @Test
    void noProposalsSaysSoAndOffersNothing() {
        AnnotateDialog dialog = AnnotateDialog.create(book, part, () -> {
        });

        dialog.showProposalsForTest(List.of());

        assertThat(dialog.proposalChecks()).isEmpty();
        assertThat(dialog.addButton().isEnabled()).isFalse();
    }

    /**
     * A story of three parts, the book at the last one.
     */
    private List<ChatMessage> story() throws Exception {
        ChatMessage second = new ChatMessage();
        second.setResponse("The harbour was empty.");
        second = chatMessageService.addChild(part, second);
        ChatMessage third = new ChatMessage();
        third.setResponse("Night fell over the water.");
        third = chatMessageService.addChild(second, third);
        Manuscript loaded = manuscriptService.find(book.getId());
        loaded.setActiveLeaf(third);
        book = manuscriptService.save(loaded);
        return List.of(part, second, third);
    }

    @Test
    void aSinglePartStoryOffersNoRange() {
        AnnotateDialog dialog = AnnotateDialog.create(book, part, () -> {
        });

        assertThat(dialog.batchField().isVisible()).isFalse();
        assertThat(dialog.selectedPartsForTest()).containsExactly(part);
    }

    @Test
    void theRangeStartsOnTheOpenedPartAndIsOnlyUsedWhenChosen() throws Exception {
        List<ChatMessage> parts = story();
        AnnotateDialog dialog = AnnotateDialog.create(book, parts.get(1), () -> {
        });

        assertThat(dialog.batchField().isVisible()).isTrue();
        assertThat(dialog.batchField().getValue()).isFalse();
        assertThat(dialog.selectedPartsForTest()).extracting(ChatMessage::getId).containsExactly(parts.get(1).getId());

        dialog.batchField().setValue(true);
        assertThat(dialog.fromField().getValue()).isEqualTo(1);
        assertThat(dialog.toField().getValue()).isEqualTo(1);
        dialog.fromField().setValue(0);
        dialog.toField().setValue(2);

        assertThat(dialog.selectedPartsForTest()).extracting(ChatMessage::getId)
                .containsExactly(parts.get(0).getId(), parts.get(1).getId(), parts.get(2).getId());
    }

    @Test
    void aRangeChosenBackwardsIsTheSameRange() throws Exception {
        List<ChatMessage> parts = story();
        AnnotateDialog dialog = AnnotateDialog.create(book, parts.get(2), () -> {
        });
        dialog.batchField().setValue(true);
        dialog.fromField().setValue(2);
        dialog.toField().setValue(1);

        assertThat(dialog.selectedPartsForTest()).extracting(ChatMessage::getId).containsExactly(parts.get(1).getId(), parts.get(2).getId());
    }

    @Test
    void proposalsOfAllPartsAreAddedToTheirOwnPart() throws Exception {
        List<ChatMessage> parts = story();
        AtomicInteger added = new AtomicInteger();
        AnnotateDialog dialog = AnnotateDialog.create(book, parts.get(0), added::incrementAndGet);
        // the same note of two parts is two proposals
        Proposal first = new Proposal("Anna", "Name?", null, 0, 4);
        Proposal second = new Proposal("The harbour", "Same as before", null, 0, 11);
        Proposal skipped = new Proposal("Night", "Cliche", "style", 0, 5);
        dialog.setModelNameForTest("mock-model");
        dialog.addProposalsForTest(parts.get(0), List.of(first));
        dialog.addProposalsForTest(parts.get(1), List.of(second));
        dialog.addProposalsForTest(parts.get(2), List.of(skipped));
        dialog.proposalChecks().get(skipped).setValue(false);

        dialog.acceptForTest();

        assertThat(annotationService.listForPart(book, parts.get(0))).extracting(Annotation::getText).containsExactly("Name?");
        assertThat(annotationService.listForPart(book, parts.get(1))).extracting(Annotation::getText).containsExactly("Same as before");
        assertThat(annotationService.listForPart(book, parts.get(2))).isEmpty();
        assertThat(added.get()).isEqualTo(1);
    }
}
