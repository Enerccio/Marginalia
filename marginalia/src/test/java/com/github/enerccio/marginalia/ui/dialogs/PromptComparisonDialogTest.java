package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.components.PromptComparison;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessagePromptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The comparison of the prompt of a part with an earlier one: the part to compare with, the table of the messages and the
 * texts at the first difference.
 */
class PromptComparisonDialogTest extends MarginaliaTestBase {

    private static final String SYSTEM = "{\"role\":\"SYSTEM\",\"content\":\"%s\"}";
    private static final String USER = "{\"role\":\"USER\",\"content\":\"%s\"}";
    private static final String ASSISTANT = "{\"role\":\"ASSISTANT\",\"content\":\"%s\"}";

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private Localization loc;

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
        ui = null;
    }

    private ChatMessage part(ChatMessage parent, String text, String prompt) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        ChatMessage saved = parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
        if (prompt != null) {
            messagePromptService.store(saved, prompt, null);
        }
        return saved;
    }

    private static String prompt(String... messages) {
        return "[" + String.join(",", messages) + "]";
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(PromptComparisonDialogTest::all));
    }

    private PromptComparisonDialog open(ChatMessage part) throws Exception {
        PromptComparisonDialog dialog = new PromptComparisonDialog(part);
        dialog.create();
        return dialog;
    }

    @SuppressWarnings("unchecked")
    private static Grid<PromptComparison.Row> grid(PromptComparisonDialog dialog) {
        return (Grid<PromptComparison.Row>) all(dialog).filter(Grid.class::isInstance).findFirst().orElseThrow();
    }

    private static List<String> texts(PromptComparisonDialog dialog) {
        return all(dialog).filter(TextArea.class::isInstance).map(TextArea.class::cast).map(TextArea::getValue).toList();
    }

    @Test
    void comparesWithTheNearestEarlierPartAndMarksTheFirstDifference() throws Exception {
        String first = prompt(SYSTEM.formatted("lore A"), USER.formatted("write the storm"));
        String second = prompt(SYSTEM.formatted("lore B"), USER.formatted("[ Generate story. ]"),
                ASSISTANT.formatted("The storm broke."), USER.formatted("write the morning"));
        ChatMessage a = part(null, "The storm broke.", first);
        ChatMessage b = part(a, "Morning came.", second);

        PromptComparisonDialog dialog = open(b);

        List<PromptComparison.Row> rows = grid(dialog).getGenericDataView().getItems().toList();
        assertThat(rows).hasSize(4);
        assertThat(rows.get(0).status()).isEqualTo(PromptComparison.Status.CHANGED);
        assertThat(rows.get(0).cacheKept()).isFalse();
        // the changed system message is selected and shown from before the difference
        assertThat(grid(dialog).getSelectedItems()).containsExactly(rows.get(0));
        assertThat(texts(dialog)).containsExactly("lore A", "lore B");
        assertThat(all(dialog).filter(Div.class::isInstance).map(Div.class::cast).map(Div::getText))
                .contains(loc.getValue(L.MSG_PROMPT_COMPARE_SYSTEM_CHANGED));
    }

    @Test
    void aPartWithoutAStoredPromptSaysSo() throws Exception {
        ChatMessage a = part(null, "Typed by the author.", null);

        PromptComparisonDialog dialog = open(a);

        assertThat(all(dialog).filter(Div.class::isInstance).map(Div.class::cast).map(Div::getText))
                .contains(loc.getValue(L.MSG_PROMPT_COMPARE_NO_PROMPT));
        assertThat(all(dialog).anyMatch(Grid.class::isInstance)).isFalse();
    }

    @Test
    void nothingToCompareWithSaysSo() throws Exception {
        ChatMessage a = part(null, "Only part.", prompt(SYSTEM.formatted("lore"), USER.formatted("go")));

        PromptComparisonDialog dialog = open(a);

        assertThat(all(dialog).filter(Div.class::isInstance).map(Div.class::cast).map(Div::getText))
                .contains(loc.getValue(L.MSG_PROMPT_COMPARE_NO_CANDIDATE));
    }
}
