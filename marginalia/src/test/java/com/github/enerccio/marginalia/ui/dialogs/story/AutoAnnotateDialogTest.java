package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
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
 * The settings of the automatic annotation of a book: what the dialog offers, which boxes depend on which, what is stored in the book
 * and how the stored model is found again.
 */
class AutoAnnotateDialogTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private Manuscript book;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private Protocol createProtocol() throws Exception {
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setName(uniqueName("protocol"));
        protocol.setProtocolType(ProtocolType.CHAT_COMPLETION);
        protocol.setMaxTokens(4096);
        protocol.setReplyTokens(300);
        return protocolService.save(protocol);
    }

    @Test
    void aNewBookIsOffAndTheDependentBoxesAreOff() {
        AutoAnnotateDialog dialog = AutoAnnotateDialog.create(book, () -> {
        });

        assertThat(dialog.enabledField().getValue()).isFalse();
        assertThat(dialog.acceptField().getValue()).isFalse();
        assertThat(dialog.redraftField().isEnabled()).isFalse();
        assertThat(dialog.acceptRedraftField().isEnabled()).isFalse();
        assertThat(dialog.modelPicker().getAi()).isNull();
        assertThat(dialog.instructionsField().getValue()).isEmpty();
        assertThat(AutoAnnotate.isEnabled(book)).isFalse();
    }

    @Test
    void theRedraftNeedsTheAcceptAndTheAcceptOfTheRedraftNeedsTheRedraft() {
        AutoAnnotateDialog dialog = AutoAnnotateDialog.create(book, () -> {
        });

        dialog.acceptField().setValue(true);
        assertThat(dialog.redraftField().isEnabled()).isTrue();
        assertThat(dialog.acceptRedraftField().isEnabled()).isFalse();

        dialog.redraftField().setValue(true);
        assertThat(dialog.acceptRedraftField().isEnabled()).isTrue();
        dialog.acceptRedraftField().setValue(true);

        // taking the first away takes what depends on it away
        dialog.redraftField().setValue(false);
        assertThat(dialog.acceptRedraftField().getValue()).isFalse();
        assertThat(dialog.acceptRedraftField().isEnabled()).isFalse();

        dialog.redraftField().setValue(true);
        dialog.acceptRedraftField().setValue(true);
        dialog.acceptField().setValue(false);
        assertThat(dialog.redraftField().getValue()).isFalse();
        assertThat(dialog.redraftField().isEnabled()).isFalse();
        assertThat(dialog.acceptRedraftField().getValue()).isFalse();
    }

    @Test
    void saveStoresTheSettingsInTheBookAndTheDialogShowsThemAgain() throws Exception {
        OpenAICompatible ai = createAI();
        Protocol protocol = createProtocol();
        AtomicInteger saved = new AtomicInteger();
        AutoAnnotateDialog dialog = AutoAnnotateDialog.create(book, saved::incrementAndGet);
        dialog.open();
        dialog.modelPicker().setSelection(ai, protocol);
        dialog.instructionsField().setValue("flag continuity errors");
        dialog.enabledField().setValue(true);
        dialog.acceptField().setValue(true);
        dialog.redraftField().setValue(true);
        dialog.acceptRedraftField().setValue(true);

        dialog.saveForTest();

        Manuscript stored = manuscriptService.find(book.getId());
        assertThat(stored.getAutoAnnotate()).isTrue();
        assertThat(stored.getAutoAnnotateInstructions()).isEqualTo("flag continuity errors");
        assertThat(stored.getAutoAnnotateModel()).isEqualTo(ai.getUuid());
        assertThat(stored.getAutoAnnotateProtocol()).isEqualTo(protocol.getUuid());
        assertThat(stored.getAutoAnnotateAccept()).isTrue();
        assertThat(stored.getAutoAnnotateRedraft()).isTrue();
        assertThat(stored.getAutoAnnotateAcceptRedraft()).isTrue();
        assertThat(saved.get()).isEqualTo(1);
        assertThat(dialog.isOpened()).isFalse();

        AutoAnnotateDialog again = AutoAnnotateDialog.create(stored, () -> {
        });
        assertThat(again.enabledField().getValue()).isTrue();
        assertThat(again.modelPicker().getAi().getUuid()).isEqualTo(ai.getUuid());
        assertThat(again.modelPicker().getProtocol().getUuid()).isEqualTo(protocol.getUuid());
        assertThat(again.instructionsField().getValue()).isEqualTo("flag continuity errors");
        assertThat(again.acceptRedraftField().getValue()).isTrue();
    }

    @Test
    void aRedraftWithoutTheAcceptIsNotStored() throws Exception {
        AutoAnnotateDialog dialog = AutoAnnotateDialog.create(book, () -> {
        });
        dialog.enabledField().setValue(true);
        dialog.acceptField().setValue(false);
        dialog.redraftField().setValue(true);

        dialog.saveForTest();

        Manuscript stored = manuscriptService.find(book.getId());
        assertThat(stored.getAutoAnnotate()).isTrue();
        assertThat(stored.getAutoAnnotateAccept()).isFalse();
        assertThat(stored.getAutoAnnotateRedraft()).isFalse();
        assertThat(stored.getAutoAnnotateAcceptRedraft()).isFalse();
    }

    @Test
    void anEmptyModelIsTheModelOfTheBook() throws Exception {
        OpenAICompatible ai = createAI();
        AutoAnnotateDialog dialog = AutoAnnotateDialog.create(book, () -> {
        });
        dialog.modelPicker().setSelection(ai, null);
        dialog.saveForTest();
        assertThat(manuscriptService.find(book.getId()).getAutoAnnotateModel()).isEqualTo(ai.getUuid());

        AutoAnnotateDialog second = AutoAnnotateDialog.create(manuscriptService.find(book.getId()), () -> {
        });
        second.modelPicker().setSelection(null, null);
        second.saveForTest();

        Manuscript stored = manuscriptService.find(book.getId());
        assertThat(stored.getAutoAnnotateModel()).isNull();
        assertThat(stored.getAutoAnnotateProtocol()).isNull();
    }

    @Test
    void aModelThatIsGoneIsNotFoundAndTheBookModelIsUsed() throws Exception {
        OpenAICompatible ai = createAI();
        aiService.delete(ai, false);
        book.setAutoAnnotateModel(ai.getUuid());
        book.setAutoAnnotateProtocol("no-such-protocol");

        AutoAnnotateDialog dialog = AutoAnnotateDialog.create(book, () -> {
        });

        assertThat(dialog.modelPicker().getAi()).isNull();
        assertThat(dialog.modelPicker().getProtocol()).isNull();
        List<OpenAICompatible> none = List.of();
        OpenAICompatible missing = AutoAnnotate.byUuid(none, ai.getUuid());
        OpenAICompatible unnamed = AutoAnnotate.byUuid(List.of(ai), null);
        assertThat(missing).isNull();
        assertThat(unnamed).isNull();
    }
}
