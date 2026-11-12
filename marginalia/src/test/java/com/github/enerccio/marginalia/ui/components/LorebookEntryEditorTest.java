package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.widgets.MarkdownEditor;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.BaseJsonNode;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An entry in a tab of the lorebook editor: the fields show it and every change the user makes is saved at once.
 */
class LorebookEntryEditorTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private LorebookEntry entry;
    private final AtomicInteger changed = new AtomicInteger();

    @BeforeEach
    void currentUI() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Lorebook lorebook = new Lorebook();
        lorebook.setName("World");
        lorebook.setSubbooks(new ArrayList<>());
        lorebook = lorebookService.save(lorebook);
        entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("Varro");
        entry.setPayload("A captain.");
        entry.setComment("Dies in book two.");
        entry.setOrder(50);
        entry = lorebookEntryService.save(entry);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private LorebookEntryEditor editor() {
        return new LorebookEntryEditor(entry, new LorebookTokens(), changed::incrementAndGet).create();
    }

    /**
     * A change the user made in the browser.
     */
    private static <T> void typed(AbstractField<?, T> field, T value) {
        T old = field.getValue();
        field.setValue(value);
        ComponentUtil.fireEvent(field, new ComponentValueChangeEvent<>(field, field, old, true));
    }

    /**
     * The React editor sends the text as the state "value" of the element.
     */
    private static void typed(MarkdownEditor editor, String text) {
        editor.getElement().setPropertyJson("value", (BaseJsonNode) JsonMapper.builder().build().valueToTree(text));
    }

    private LorebookEntry saved() throws Exception {
        return lorebookEntryService.find(entry.getId());
    }

    @Test
    void theFieldsShowTheEntry() {
        LorebookEntryEditor editor = editor();

        assertThat(editor.getNameField().getValue()).isEqualTo("Varro");
        assertThat(editor.getOrderField().getValue()).isEqualTo(50);
        assertThat(editor.getEnabledBox().getValue()).isTrue();
        assertThat(editor.getContentEditor().getValue()).isEqualTo("A captain.");
        assertThat(editor.getNoteField().getValue()).isEqualTo("Dies in book two.");
        assertThat(editor.getTokensSpan().getText()).startsWith("~");
        assertThat(editor.getProblemSpan().getText()).isEmpty();
    }

    @Test
    void theNameTheOrderAndEnabledAreSavedAtOnce() throws Exception {
        LorebookEntryEditor editor = editor();

        typed(editor.getNameField(), "Captain Varro");
        typed(editor.getOrderField(), 7);
        typed(editor.getEnabledBox(), false);

        assertThat(saved().getName()).isEqualTo("Captain Varro");
        assertThat(saved().getOrder()).isEqualTo(7);
        assertThat(saved().isEnabled()).isFalse();
        // the list and the totals are told
        assertThat(changed).hasValue(3);
    }

    @Test
    void anEmptyNameIsNotSaved() throws Exception {
        LorebookEntryEditor editor = editor();

        typed(editor.getNameField(), " ");

        assertThat(editor.getNameField().isInvalid()).isTrue();
        assertThat(saved().getName()).isEqualTo("Varro");
    }

    @Test
    void theContentIsSavedAndCountedWhenTheEditorSendsIt() throws Exception {
        LorebookEntryEditor editor = editor();
        String before = editor.getTokensSpan().getText();

        typed(editor.getContentEditor(), "A captain of the watch, who keeps the gates of the old city and never sleeps.");

        assertThat(saved().getPayload()).startsWith("A captain of the watch");
        assertThat(editor.getTokensSpan().getText()).isNotEqualTo(before);
        assertThat(changed).hasValue(1);
    }

    @Test
    void aBrokenTemplateIsSavedAndShownInTheFooter() throws Exception {
        LorebookEntryEditor editor = editor();

        typed(editor.getContentEditor(), "A captain {{#if");

        assertThat(saved().getPayload()).isEqualTo("A captain {{#if");
        assertThat(editor.getProblemSpan().getText()).isNotEmpty();
    }

    @Test
    void theValueSetByTheServerIsNotAChangeOfTheUser() {
        LorebookEntryEditor editor = editor();

        editor.getContentEditor().setValue("Set by code.");

        assertThat(changed).hasValue(0);
    }

    @Test
    void theNoteIsShownOnlyWhenAskedFor() throws Exception {
        LorebookEntryEditor editor = editor();
        assertThat(editor.getNoteField().isVisible()).isFalse();

        editor.getShowNoteBox().setValue(true);
        assertThat(editor.getNoteField().isVisible()).isTrue();

        typed(editor.getNoteField(), "Lives.");
        assertThat(saved().getComment()).isEqualTo("Lives.");
    }

    @Test
    void reloadingShowsTheEntryFromTheDatabaseAndKeepsTheNoteShown() throws Exception {
        LorebookEntryEditor editor = editor();
        editor.getShowNoteBox().setValue(true);
        LorebookEntry other = lorebookEntryService.find(entry.getId());
        other.setPayload("Replaced elsewhere.");
        lorebookEntryService.save(other);

        editor.reload(lorebookEntryService.find(entry.getId()));

        assertThat(editor.getContentEditor().getValue()).isEqualTo("Replaced elsewhere.");
        assertThat(editor.getNoteField().isVisible()).isTrue();
        assertThat(editor.getShowNoteBox().getValue()).isTrue();
    }

    private LorebookEntry changeElsewhere(java.util.function.Consumer<LorebookEntry> change) throws Exception {
        LorebookEntry other = lorebookEntryService.find(entry.getId());
        change.accept(other);
        return lorebookEntryService.save(other);
    }

    private static com.vaadin.flow.component.button.Button button(LorebookEntryEditor editor, int index) {
        return (com.vaadin.flow.component.button.Button) editor.getConflictBar().getComponentAt(index);
    }

    @Test
    void aSaveKeepsWhatWasChangedElsewhereInTheOtherFields() throws Exception {
        LorebookEntryEditor editor = editor();
        changeElsewhere(other -> {
            other.setName("Captain Varro");
            other.setFiltering("Varro");
        });

        typed(editor.getContentEditor(), "A captain of the watch.");

        assertThat(saved().getPayload()).isEqualTo("A captain of the watch.");
        assertThat(saved().getName()).isEqualTo("Captain Varro");
        assertThat(saved().getFiltering()).isEqualTo("Varro");
        // the tab shows the entry as it is now, and the object of the tab (shared with the list) is up to date
        assertThat(editor.getNameField().getValue()).isEqualTo("Captain Varro");
        assertThat(entry.getName()).isEqualTo("Captain Varro");
        assertThat(editor.getEntry()).isSameAs(entry);
    }

    @Test
    void aTextChangedElsewhereIsNotOverwrittenUntilTheUserSaysSo() throws Exception {
        LorebookEntryEditor editor = editor();
        changeElsewhere(other -> other.setPayload("Theirs."));

        typed(editor.getContentEditor(), "Mine.");

        assertThat(saved().getPayload()).isEqualTo("Theirs.");
        assertThat(editor.getConflictBar().isVisible()).isTrue();
        assertThat(changed).hasValue(0);

        button(editor, 1).click();

        assertThat(saved().getPayload()).isEqualTo("Mine.");
        assertThat(editor.getConflictBar().isVisible()).isFalse();
        assertThat(editor.getContentEditor().getValue()).isEqualTo("Mine.");
    }

    @Test
    void theSavedTextCanBeLoadedInstead() throws Exception {
        LorebookEntryEditor editor = editor();
        changeElsewhere(other -> other.setComment("Their note."));
        editor.getShowNoteBox().setValue(true);

        typed(editor.getNoteField(), "My note.");
        assertThat(editor.getConflictBar().isVisible()).isTrue();

        button(editor, 2).click();

        assertThat(saved().getComment()).isEqualTo("Their note.");
        assertThat(editor.getNoteField().getValue()).isEqualTo("Their note.");
        assertThat(editor.getConflictBar().isVisible()).isFalse();
    }

    @Test
    void anEntryDeletedElsewhereIsReported() throws Exception {
        LorebookEntryEditor editor = editor();
        AtomicInteger gone = new AtomicInteger();
        editor.setOnGone(gone::incrementAndGet);
        lorebookEntryService.delete(lorebookEntryService.find(entry.getId()), false);

        typed(editor.getNameField(), "Captain Varro");

        assertThat(gone).hasValue(1);
    }

    @Test
    void aReadOnlyEditorSavesNothing() throws Exception {
        LorebookEntryEditor editor = editor();
        editor.setReadOnly(true);

        typed(editor.getContentEditor(), "Changed.");
        typed(editor.getNameField(), "Changed");

        assertThat(saved().getPayload()).isEqualTo("A captain.");
        assertThat(saved().getName()).isEqualTo("Varro");
    }

    @Test
    void aChangeMadeElsewhereIsShownInTheTab() throws Exception {
        LorebookEntryEditor editor = editor();
        changeElsewhere(other -> {
            other.setName("Captain Varro");
            other.setPayload("Theirs.");
        });

        editor.changedElsewhere();

        assertThat(editor.getNameField().getValue()).isEqualTo("Captain Varro");
        assertThat(editor.getContentEditor().getValue()).isEqualTo("Theirs.");
        assertThat(entry.getPayload()).isEqualTo("Theirs.");
        assertThat(changed).hasValue(1);
    }

    @Test
    void theTabsOwnSaveIsNoChange() throws Exception {
        LorebookEntryEditor editor = editor();
        typed(editor.getNameField(), "Captain Varro");
        changed.set(0);

        editor.changedElsewhere();

        assertThat(changed).hasValue(0);
    }

    @Test
    void aTextThatWaitsForTheUserStaysOverChangesElsewhereAndOtherSaves() throws Exception {
        LorebookEntryEditor editor = editor();
        changeElsewhere(other -> other.setPayload("Theirs."));
        typed(editor.getContentEditor(), "Mine.");
        assertThat(editor.getConflictBar().isVisible()).isTrue();

        changeElsewhere(other -> other.setFiltering("Varro"));
        editor.changedElsewhere();
        typed(editor.getNameField(), "Captain Varro");

        // the name is saved, the text waits and is not built over
        assertThat(saved().getName()).isEqualTo("Captain Varro");
        assertThat(saved().getPayload()).isEqualTo("Theirs.");
        assertThat(editor.getConflictBar().isVisible()).isTrue();
        assertThat(editor.getContentEditor().getValue()).isEqualTo("Mine.");

        button(editor, 1).click();

        assertThat(saved().getPayload()).isEqualTo("Mine.");
        assertThat(saved().getFiltering()).isEqualTo("Varro");
        assertThat(saved().getName()).isEqualTo("Captain Varro");
        assertThat(editor.getConflictBar().isVisible()).isFalse();
    }

    @Test
    void aChangeElsewhereUpdatesTheFieldsInPlace() throws Exception {
        LorebookEntryEditor editor = editor();
        var nameField = editor.getNameField();
        var contentEditor = editor.getContentEditor();
        changeElsewhere(other -> {
            other.setName("Captain Varro");
            other.setPayload("Theirs.");
            other.setFiltering("Varro");
        });

        editor.changedElsewhere();

        // the same components show the new values: the tab is not built again
        assertThat(editor.getNameField()).isSameAs(nameField);
        assertThat(editor.getContentEditor()).isSameAs(contentEditor);
        assertThat(nameField.getValue()).isEqualTo("Captain Varro");
        assertThat(contentEditor.getValue()).isEqualTo("Theirs.");
        assertThat(editor.getEntry()).isSameAs(entry);
    }
}
