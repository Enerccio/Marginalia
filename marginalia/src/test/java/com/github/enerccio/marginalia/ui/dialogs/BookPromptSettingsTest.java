package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.FolderService.Details;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PromptField;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.widgets.InheritedLanguageCombo;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The prompts tab of the book windows (desktop and phone): an empty field shows what the book inherits from its folders
 * and where from, and a field the user changes is saved on the book, a cleared one is inherited again.
 */
class BookPromptSettingsTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps only a weak reference
    private UI ui;
    private Folder scifi;
    private Folder expanse;
    private Manuscript book;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        scifi = folderService.create("Sci-fi", null);
        expanse = folderService.create("Expanse", scifi);
        folderService.update(scifi, new Details("Sci-fi", null, null, Map.of(PromptField.STYLE, "Terse")));
        Manuscript manuscript = new Manuscript();
        manuscript.setName("Leviathan");
        manuscript.setFolder(expanse);
        book = manuscriptService.save(manuscript);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private TextArea area(Component dialog, L label) {
        return contents(dialog).flatMap(c -> ComponentTree.all(c, TextArea.class))
                .filter(a -> loc.getValue(label).equals(a.getLabel())).findFirst().orElseThrow();
    }

    /**
     * The window and the content of every tab of its sheets (a sheet keeps the content of the tab that is not selected
     * out of its children).
     */
    private static Stream<Component> contents(Component dialog) {
        Stream<Component> tabs = ComponentTree.all(dialog, TabSheet.class)
                .flatMap(sheet -> sheet.getChildren().filter(c -> c instanceof Tabs).flatMap(c -> c.getChildren())
                        .filter(Tab.class::isInstance).map(tab -> sheet.getComponent((Tab) tab)).filter(Objects::nonNull));
        return Stream.concat(Stream.of(dialog), tabs);
    }

    /**
     * The user types into the field: the value changes from the client, which saves.
     */
    private static void type(TextArea field, String text) {
        String old = field.getValue();
        field.setValue(text);
        ComponentUtil.fireEvent(field, new ComponentValueChangeEvent<>(field, field, old, true));
    }

    /**
     * A tab of the sheet is in the component tree only while it is selected.
     */
    private ManuscriptDialog desktopDialog() throws Exception {
        ManuscriptDialog dialog = new ManuscriptDialog(book);
        dialog.create();
        dialog.open();
        dialog.restoreState(JsonParser.parseString("{\"tab\":\"prompt\"}").getAsJsonObject());
        return dialog;
    }

    private void showsTheInheritedValue(Component dialog) {
        TextArea style = area(dialog, L.LABEL_STYLE);
        assertThat(style.getValue()).isEmpty();
        assertThat(style.getPlaceholder()).isEqualTo("Terse");
        assertThat(style.getHelperText()).isEqualTo("Inherited from the folder \"Sci-fi\"");
        assertThat(area(dialog, L.LABEL_USER_PROMPT).getHelperText()).isEqualTo("Built-in default");
    }

    @Test
    void theDesktopWindowShowsWhereAnEmptyFieldIsInheritedFrom() throws Exception {
        ManuscriptDialog dialog = desktopDialog();

        showsTheInheritedValue(dialog);
    }

    @Test
    void thePhoneWindowShowsWhereAnEmptyFieldIsInheritedFrom() throws Exception {
        PhoneManuscriptDialog dialog = new PhoneManuscriptDialog(book);
        dialog.create();
        dialog.open();

        showsTheInheritedValue(dialog);
    }

    @Test
    void aFieldTheUserSetsIsSavedOnTheBookAndAClearedOneIsInheritedAgain() throws Exception {
        ManuscriptDialog dialog = desktopDialog();

        type(area(dialog, L.LABEL_STYLE), "Lyrical");

        Manuscript stored = manuscriptService.find(book.getId());
        assertThat(stored.getStyle()).isEqualTo("Lyrical");
        assertThat(manuscriptService.getStyle(stored)).isEqualTo("Lyrical");
        assertThat(folderService.find(scifi.getId()).getStyle()).as("the folder is not touched").isEqualTo("Terse");
        assertThat(area(dialog, L.LABEL_STYLE).getHelperText()).as("a field that is set says nothing").isNull();

        type(area(dialog, L.LABEL_STYLE), "");

        stored = manuscriptService.find(book.getId());
        assertThat(stored.getStyle()).isNull();
        assertThat(manuscriptService.getStyle(stored)).isEqualTo("Terse");
        assertThat(area(dialog, L.LABEL_STYLE).getHelperText()).isEqualTo("Inherited from the folder \"Sci-fi\"");
    }

    private InheritedLanguageCombo languageBox(Component dialog) {
        return contents(dialog).flatMap(c -> ComponentTree.all(c, InheritedLanguageCombo.class)).findFirst().orElseThrow();
    }

    private static void pick(InheritedLanguageCombo combo, Language language) {
        Language old = combo.getValue();
        combo.setValue(language);
        ComponentUtil.fireEvent(combo, new ComponentValueChangeEvent<>(combo, combo, old, true));
    }

    private void theLanguageIsInheritedUntilTheBookSetsOne(Component dialog) throws Exception {
        InheritedLanguageCombo combo = languageBox(dialog);
        assertThat(combo.getValue()).isNull();
        assertThat(combo.getPlaceholder()).isEqualTo(Language.GERMAN.getDisplayName());
        assertThat(combo.getHelperText()).isEqualTo("Inherited from the folder \"Sci-fi\"");

        pick(combo, Language.CZECH);
        assertThat(manuscriptService.find(book.getId()).getOwnLanguage()).isEqualTo(Language.CZECH);
        assertThat(manuscriptService.getLanguage(manuscriptService.find(book.getId()))).isEqualTo(Language.CZECH);
        assertThat(combo.getHelperText()).isNull();

        pick(combo, null);
        assertThat(manuscriptService.find(book.getId()).getOwnLanguage()).as("empty is inherit, not English").isNull();
        assertThat(manuscriptService.getLanguage(manuscriptService.find(book.getId()))).isEqualTo(Language.GERMAN);
        assertThat(combo.getHelperText()).isEqualTo("Inherited from the folder \"Sci-fi\"");
    }

    @Test
    void theDesktopLanguageIsInheritedUntilTheBookSetsOne() throws Exception {
        folderService.update(scifi, new Details("Sci-fi", null, Language.GERMAN, Map.of(PromptField.STYLE, "Terse")));
        theLanguageIsInheritedUntilTheBookSetsOne(desktopDialog());
    }

    @Test
    void thePhoneLanguageIsInheritedUntilTheBookSetsOne() throws Exception {
        folderService.update(scifi, new Details("Sci-fi", null, Language.GERMAN, Map.of(PromptField.STYLE, "Terse")));
        PhoneManuscriptDialog dialog = new PhoneManuscriptDialog(book);
        dialog.create();
        dialog.open();
        theLanguageIsInheritedUntilTheBookSetsOne(dialog);
    }

    @Test
    void thePhoneWindowSavesAFieldToo() throws Exception {
        PhoneManuscriptDialog dialog = new PhoneManuscriptDialog(book);
        dialog.create();
        dialog.open();

        type(area(dialog, L.LABEL_USER_PROMPT), "Continue the scene");

        assertThat(manuscriptService.find(book.getId()).getUserPrompt()).isEqualTo("Continue the scene");
    }
}
