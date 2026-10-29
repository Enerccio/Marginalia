package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.FolderService.Details;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PromptField;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.widgets.InheritedLanguageCombo;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The settings dialog of a folder: the fields with what they inherit and where it comes from, and the saving. (The
 * refusal while an agent works on a book is the service's, see {@code FolderSettingsTest}.)
 */
class FolderDialogTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps only a weak reference
    private UI ui;
    private Folder scifi;
    private Folder expanse;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        scifi = folderService.create("Sci-fi", null);
        expanse = folderService.create("Expanse", scifi);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private FolderDialog dialog(Folder folder, AtomicReference<Folder> saved) {
        FolderDialog dialog = new FolderDialog(folder, saved::set);
        assertThat(dialog.create()).isTrue();
        return dialog;
    }

    private TextArea area(FolderDialog dialog, L label) {
        return ComponentTree.all(dialog, TextArea.class).filter(a -> loc.getValue(label).equals(a.getLabel())).findFirst().orElseThrow();
    }

    private TextField line(FolderDialog dialog, L label) {
        return ComponentTree.all(dialog, TextField.class).filter(a -> loc.getValue(label).equals(a.getLabel())).findFirst().orElseThrow();
    }

    @Test
    void theFieldsShowTheValuesOfTheFolderAndTheNameInTheTitle() throws Exception {
        folderService.update(scifi, new Details("Sci-fi", "Space stories", null, Map.of(PromptField.STYLE, "Terse")));

        FolderDialog dialog = dialog(scifi, new AtomicReference<>());

        assertThat(dialog.getHeaderTitle()).contains("Sci-fi");
        assertThat(line(dialog, L.LABEL_NAME).getValue()).isEqualTo("Sci-fi");
        assertThat(area(dialog, L.LABEL_DESCRIPTION).getValue()).isEqualTo("Space stories");
        assertThat(area(dialog, L.LABEL_STYLE).getValue()).isEqualTo("Terse");
        assertThat(area(dialog, L.LABEL_USER_PROMPT).getValue()).isEmpty();
    }

    @Test
    void anEmptyFieldShowsWhatItInheritsAndWhereFrom() throws Exception {
        folderService.update(scifi, new Details("Sci-fi", null, null, Map.of(PromptField.STYLE, "Terse")));
        UserSetting user = settingService.getOrCreate(UserSetting.class);
        user.setDefaultTense("Past, from the user");
        settingService.save(user);

        FolderDialog top = dialog(scifi, new AtomicReference<>());
        FolderDialog below = dialog(expanse, new AtomicReference<>());

        TextArea style = area(below, L.LABEL_STYLE);
        assertThat(style.getPlaceholder()).isEqualTo("Terse");
        assertThat(style.getHelperText()).isEqualTo("Inherited from the folder \"Sci-fi\"");
        TextField tense = line(below, L.LABEL_TENSE);
        assertThat(tense.getPlaceholder()).isEqualTo("Past, from the user");
        assertThat(tense.getHelperText()).isEqualTo("Inherited from your settings");
        assertThat(area(below, L.LABEL_USER_PROMPT).getHelperText()).isEqualTo("Built-in default");

        assertThat(area(top, L.LABEL_STYLE).getHelperText()).as("a field that is set says nothing").isNull();
        assertThat(line(top, L.LABEL_TENSE).getHelperText()).isEqualTo("Inherited from your settings");
    }

    @Test
    void saveWritesTheFieldsAndTellsTheCaller() throws Exception {
        AtomicReference<Folder> saved = new AtomicReference<>();
        FolderDialog dialog = dialog(expanse, saved);

        line(dialog, L.LABEL_NAME).setValue("The Expanse");
        area(dialog, L.LABEL_DESCRIPTION).setValue("Leviathan Wakes and so on");
        area(dialog, L.LABEL_STYLE).setValue("Gritty");
        dialog.save();

        assertThat(saved.get()).isNotNull();
        Folder stored = folderService.find(expanse.getId());
        assertThat(stored.getName()).isEqualTo("The Expanse");
        assertThat(stored.getDescription()).isEqualTo("Leviathan Wakes and so on");
        assertThat(stored.getStyle()).isEqualTo("Gritty");
        assertThat(stored.getPov()).isNull();
    }

    private InheritedLanguageCombo languageBox(FolderDialog dialog) {
        return ComponentTree.all(dialog, InheritedLanguageCombo.class).findFirst().orElseThrow();
    }

    @Test
    void theLanguageIsInheritedUntilTheFolderSetsOne() throws Exception {
        folderService.update(scifi, new Details("Sci-fi", null, Language.GERMAN, Map.of()));

        InheritedLanguageCombo below = languageBox(dialog(expanse, new AtomicReference<>()));
        assertThat(below.getValue()).isNull();
        assertThat(below.getPlaceholder()).isEqualTo(Language.GERMAN.getDisplayName());
        assertThat(below.getHelperText()).isEqualTo("Inherited from the folder \"Sci-fi\"");

        InheritedLanguageCombo top = languageBox(dialog(scifi, new AtomicReference<>()));
        assertThat(top.getValue()).isEqualTo(Language.GERMAN);
        assertThat(top.getHelperText()).as("a language that is set says nothing").isNull();

        FolderDialog setter = dialog(expanse, new AtomicReference<>());
        languageBox(setter).setValue(Language.CZECH);
        setter.save();
        assertThat(folderService.find(expanse.getId()).getLanguage()).isEqualTo(Language.CZECH);

        FolderDialog clearer = dialog(expanse, new AtomicReference<>());
        languageBox(clearer).clear();
        clearer.save();
        assertThat(folderService.find(expanse.getId()).getLanguage()).isNull();
    }

    @Test
    void clearingAFieldMakesTheFolderInheritAgain() throws Exception {
        folderService.update(expanse, new Details("Expanse", null, null, Map.of(PromptField.STYLE, "Gritty")));
        Manuscript book = new Manuscript();
        book.setName("Leviathan");
        book.setFolder(expanse);
        book = manuscriptService.save(book);
        assertThat(manuscriptService.getStyle(book)).isEqualTo("Gritty");

        FolderDialog dialog = dialog(expanse, new AtomicReference<>());
        area(dialog, L.LABEL_STYLE).setValue("");
        dialog.save();

        assertThat(folderService.find(expanse.getId()).getStyle()).isNull();
        assertThat(manuscriptService.getStyle(book)).isNotEqualTo("Gritty");
    }

    @Test
    void aTakenNameKeepsTheDialogOpenAndWritesNothing() throws Exception {
        folderService.create("Fantasy", null);
        AtomicReference<Folder> saved = new AtomicReference<>();
        FolderDialog dialog = dialog(scifi, saved);

        line(dialog, L.LABEL_NAME).setValue("Fantasy");
        area(dialog, L.LABEL_STYLE).setValue("Terse");
        dialog.save();

        assertThat(saved.get()).isNull();
        assertThat(folderService.find(scifi.getId()).getName()).isEqualTo("Sci-fi");
        assertThat(folderService.find(scifi.getId()).getStyle()).isNull();
    }

    @Test
    void aBlankNameIsNotSaved() throws Exception {
        AtomicReference<Folder> saved = new AtomicReference<>();
        FolderDialog dialog = dialog(scifi, saved);

        line(dialog, L.LABEL_NAME).setValue(" ");
        dialog.save();

        assertThat(saved.get()).isNull();
        assertThat(line(dialog, L.LABEL_NAME).isInvalid()).isTrue();
        assertThat(folderService.find(scifi.getId()).getName()).isEqualTo("Sci-fi");
    }

    @Test
    void aTemplateThatDoesNotParseIsNotSaved() throws Exception {
        AtomicReference<Folder> saved = new AtomicReference<>();
        FolderDialog dialog = dialog(scifi, saved);

        area(dialog, L.LABEL_MASTER_TEMPLATE).setValue("{{#if}} broken");
        dialog.save();

        assertThat(saved.get()).isNull();
        assertThat(folderService.find(scifi.getId()).getTemplate()).isNull();
    }

    @Test
    void aFolderThatIsGoneIsNotShown() throws Exception {
        Folder gone = folderService.create("Gone", null);
        folderService.delete(gone, false);

        assertThat(new FolderDialog(gone, _ -> { }).create()).isFalse();
    }
}
