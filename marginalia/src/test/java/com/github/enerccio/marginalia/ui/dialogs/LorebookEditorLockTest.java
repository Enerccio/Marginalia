package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.ui.components.LorebookEntryEditor;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.BaseJsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The lorebook editor while an agent works on a book that uses the lorebook: read only with the name of the agent, a save
 * that was refused says so, and the editing is back (with the entries as the run left them) when the run lets go.
 */
class LorebookEditorLockTest extends AgentTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private Lorebook lorebook;
    private Agent agent;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        lorebook = new Lorebook();
        lorebook.setName(uniqueName("lore"));
        lorebook = lorebookService.save(lorebook);
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("Harry");
        entry.setPayload("A wizard.");
        entryService.save(entry);

        Manuscript book = manuscript();
        book.setLorebook(lorebook);
        agent = agentService.save(newAgent(manuscriptService.save(book)));
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private LorebookEditorDialog dialog() {
        LorebookEditorDialog dialog = new LorebookEditorDialog(lorebook, null).create();
        dialog.open();
        return dialog;
    }

    private static LorebookEntryEditor openHarry(LorebookEditorDialog dialog) {
        LorebookEntry harry = dialog.getGrid().getDataProvider().fetch(new Query<>()).findFirst().orElseThrow();
        dialog.openEntry(harry, false);
        return dialog.getTabs().values().iterator().next();
    }

    private static void typed(LorebookEntryEditor editor, String text) {
        com.github.enerccio.marginalia.ui.widgets.MarkdownEditor content = editor.getChildren()
                .flatMap(LorebookEditorLockTest::all)
                .filter(com.github.enerccio.marginalia.ui.widgets.MarkdownEditor.class::isInstance)
                .map(com.github.enerccio.marginalia.ui.widgets.MarkdownEditor.class::cast).findFirst().orElseThrow();
        content.getElement().setPropertyJson("value", (BaseJsonNode) JsonMapper.builder().build().valueToTree(text));
    }

    private static java.util.stream.Stream<com.vaadin.flow.component.Component> all(com.vaadin.flow.component.Component root) {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(root), root.getChildren().flatMap(LorebookEditorLockTest::all));
    }

    private String payload() throws Exception {
        return entryService.getEntriesForLorebook(lorebook.getId()).getFirst().getPayload();
    }

    @Test
    void anEditorOpenedDuringARunIsReadOnly() throws Exception {
        agentRunService.start(agent);

        LorebookEditorDialog dialog = dialog();
        LorebookEntryEditor editor = openHarry(dialog);

        assertThat(dialog.getLockBar().isVisible()).isTrue();
        assertThat(((Span) dialog.getLockBar().getComponentAt(0)).getText()).contains(agent.getName());
        assertThat(editor.isReadOnly()).isTrue();
    }

    @Test
    void aSaveRefusedByARunLocksTheEditorAndTheEndOfTheRunFreesIt() throws Exception {
        LorebookEditorDialog dialog = dialog();
        LorebookEntryEditor editor = openHarry(dialog);
        assertThat(dialog.getLockBar().isVisible()).isFalse();
        AgentRun run = agentRunService.start(agent);

        typed(editor, "A wizard, rewritten while the agent works.");

        assertThat(payload()).isEqualTo("A wizard.");
        assertThat(dialog.getLockBar().isVisible()).isTrue();
        assertThat(editor.isReadOnly()).isTrue();
        assertThat(dialog.checkLock()).isTrue();

        agentRunService.pause(run);

        assertThat(dialog.checkLock()).isFalse();
        assertThat(dialog.getLockBar().isVisible()).isFalse();
        LorebookEntryEditor again = dialog.getTabs().values().iterator().next();
        assertThat(again.isReadOnly()).isFalse();
        typed(again, "A wizard of the north.");
        assertThat(payload()).isEqualTo("A wizard of the north.");
    }
}
