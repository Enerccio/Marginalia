package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.LockedException;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PhraseWatchService;
import com.github.enerccio.marginalia.extensions.InheritedSettingValues;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.M_L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.Consumer;

/**
 * The settings of a folder: its name, description and tags, and the text settings its books inherit
 * ({@link PromptSettingsForm}), each with the value it inherits itself as the placeholder. Unlike the dialog of a book it
 * saves with a button, because the settings change what every book below the folder uses, which is refused while an agent
 * works on one of them (the user is told which book and pauses the run first; the tags are saved as they are picked, as in
 * the dialog of a book).
 */
@Configurable
public class FolderDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private PhraseWatchService phraseWatchService;

    private final Folder folder;
    private final Consumer<Folder> done;

    private TextField nameField;
    private TextArea descriptionField;
    private InheritedLanguageCombo languageCombo;
    private HistoryLimitField historyLimitField;
    private PromptSettingsForm form;
    private ExtensionSettingsForm extensionForm;
    private WatchedPhrasesEditor phrasesEditor;

    /**
     * @param done called with the saved folder after the dialog saved it and closed
     */
    public FolderDialog(Folder folder, Consumer<Folder> done) {
        this.folder = folder;
        this.done = done;
    }

    /**
     * Builds the dialog from the folder as it is stored now and opens it; tells the user instead when the folder is gone.
     */
    public void show() {
        if (create()) {
            open();
        }
    }

    boolean create() {
        Folder stored;
        try {
            stored = folderService.find(folder.getId());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return false;
        }
        if (stored == null || stored.isDeleted()) {
            Notification.warning(loc.getValue(M_L.ERROR_FOLDER_GONE));
            return false;
        }

        setHeaderTitle(loc.getValue(L.LABEL_FOLDER_SETTINGS) + ": " + stored.getName());
        setWidth("900px");
        setMaxWidth("96%");
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        nameField = new TextField(loc.getValue(L.LABEL_NAME));
        nameField.setWidthFull();
        nameField.setValue(StringUtils.defaultString(stored.getName()));

        descriptionField = new TextArea(loc.getValue(L.LABEL_DESCRIPTION));
        descriptionField.setWidthFull();
        ResizableTextArea.install(loc, descriptionField, "120px");
        descriptionField.setValue(StringUtils.defaultString(stored.getDescription()));

        TagMultiComboBox tags = new TagMultiComboBox(loc.getValue(M_L.LABEL_TAGS));
        tags.setWidthFull();
        tags.setForEntity(stored);

        languageCombo = new InheritedLanguageCombo(loc.getValue(L.LABEL_LANGUAGE));
        languageCombo.load(stored.getLanguage(), stored.getParent());

        historyLimitField = new HistoryLimitField(loc.getValue(M_L.LABEL_HISTORY_MAX_VERSIONS));
        try {
            historyLimitField.load(stored.getHistoryMaxVersions(), manuscriptService.resolveInheritedHistoryLimit(stored.getParent()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        Span help = new Span(loc.getValue(M_L.HELP_FOLDER_SETTINGS));

        form = new PromptSettingsForm("200px", null).create();
        form.load(field -> field.ofFolder(stored), stored.getParent());

        extensionForm = new ExtensionSettingsForm("120px", null).create();
        extensionForm.load(key -> InheritedSettingValues.get(stored, key), stored.getParent());

        phrasesEditor = new WatchedPhrasesEditor(loc);
        try {
            phrasesEditor.setTemplates(phraseWatchService.getTemplates());
            phrasesEditor.setValue(stored.getWatchedPhrases());
            phrasesEditor.setInherited(phraseWatchService.resolveInherited(stored.getParent()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        Details phrases = new Details(loc.getValue(M_L.LABEL_PHRASE_WATCH), phrasesEditor);
        phrases.setWidthFull();
        phrases.setOpened(!stored.getWatchedPhrases().isEmpty());

        VerticalLayout content = new VerticalLayout(nameField, descriptionField, tags, help, languageCombo, historyLimitField, form.formLayout());
        if (!extensionForm.isEmpty()) {
            content.add(extensionForm.layout());
        }
        content.add(phrases);
        content.setPadding(false);
        add(content);

        Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        Button save = new Button(loc.getValue(L.LABEL_SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout footer = new HorizontalLayout(close, save);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);
        return true;
    }

    /**
     * Writes the dialog to the folder and closes it; what the service refuses is told to the user and the dialog stays.
     */
    void save() {
        if (StringUtils.isBlank(nameField.getValue())) {
            nameField.setInvalid(true);
            nameField.setErrorMessage(loc.getValue(M_L.ERROR_NAME_CANT_BE_EMPTY));
            return;
        }
        if (!form.validate() || !phrasesEditor.validate()) {
            return;
        }
        try {
            Folder saved = folderService.update(folder, new FolderService.Details(nameField.getValue(), descriptionField.getValue(), languageCombo.getValue(), form.values(), historyLimitField.getValue(), extensionForm.values()));
            if (saved != null) {
                saved = phraseWatchService.setForFolder(saved, phrasesEditor.getValue());
            }
            if (saved == null) {
                Notification.warning(loc.getValue(M_L.ERROR_FOLDER_GONE));
                return;
            }
            close();
            done.accept(saved);
        } catch (LockedException e) {
            UIUtils.internalServerError(loc, e);
        } catch (IllegalArgumentException e) {
            Notification.warning(loc.getValue(M_L.ERROR_FOLDER_NAME_INVALID));
        } catch (IllegalStateException e) {
            Notification.warning(loc.getValue(M_L.ERROR_FOLDER_NAME_TAKEN));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
