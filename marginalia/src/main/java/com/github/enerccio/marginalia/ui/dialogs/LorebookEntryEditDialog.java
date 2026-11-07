package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Edits the text of one lorebook entry without leaving the place it was opened from (the lorebook tab of the story editor):
 * the name, whether it is used, its filter and the way the filter is looked for, the content and the note. The rest of the
 * settings (tags, order, the activation settings) are edited in the lorebook editor. Saves when the user presses Save.
 */
@Configurable
public class LorebookEntryEditDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private final LorebookEntry entry;
    private final Runnable onSaved;

    private TextField nameField;
    private Checkbox enabledBox;
    private ComboBox<FilteringMode> filteringModeBox;
    private TextField filteringField;
    private TextArea payloadField;
    private TextArea commentField;

    /**
     * @param entry   the entry to edit; it changes only when the user saves
     * @param onSaved told after the entry was saved, may be null
     */
    public LorebookEntryEditDialog(LorebookEntry entry, Runnable onSaved) {
        this.entry = entry;
        this.onSaved = onSaved;
    }

    public LorebookEntryEditDialog create() {
        setHeaderTitle(loc.getValue(L.LABEL_EDIT_ENTRY));
        setWidth("700px");
        setMaxWidth("96%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        nameField = new TextField(loc.getValue(L.LABEL_ENTRY_NAME));
        nameField.setWidthFull();
        nameField.setValue(StringUtils.defaultString(entry.getName()));

        enabledBox = new Checkbox(loc.getValue(L.LABEL_ENABLED), entry.isEnabled());

        filteringModeBox = new ComboBox<>(loc.getValue(L.LABEL_FILTERING_MODE));
        filteringModeBox.setItems(FilteringMode.values());
        filteringModeBox.setItemLabelGenerator(mode -> loc.getValue(loc.getFilteringMode(mode)));
        filteringModeBox.setAllowCustomValue(false);
        filteringModeBox.setValue(entry.getFilteringMode());
        filteringModeBox.setWidthFull();

        filteringField = new TextField(loc.getValue(L.LABEL_FILTERING));
        filteringField.setWidthFull();
        filteringField.setValue(StringUtils.defaultString(entry.getFiltering()));

        payloadField = new TextArea(loc.getValue(L.LABEL_CONTENT));
        payloadField.setWidthFull();
        payloadField.setMinHeight("200px");
        payloadField.setValue(StringUtils.defaultString(entry.getPayload()));

        commentField = new TextArea(loc.getValue(L.LABEL_NOTE));
        commentField.setWidthFull();
        commentField.setValue(StringUtils.defaultString(entry.getComment()));

        VerticalLayout content = new VerticalLayout(nameField, enabledBox, filteringModeBox, filteringField, payloadField, commentField);
        content.setPadding(false);
        add(content);

        Button save = new Button(loc.getValue(L.LABEL_SAVE), event -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        getFooter().add(cancel, save);

        DialogFocus.onOpen(this, nameField);
        DialogShortcuts.submitWith(save, nameField, filteringField, payloadField, commentField);
        return this;
    }

    /**
     * Puts the fields into the entry and saves it; the dialog stays open when that fails.
     */
    void save() {
        if (StringUtils.isBlank(nameField.getValue())) {
            nameField.setInvalid(true);
            nameField.setErrorMessage(loc.getValue(L.ERROR_NAME_CANT_BE_EMPTY));
            return;
        }
        try {
            entry.setName(nameField.getValue());
            entry.setEnabled(enabledBox.getValue());
            entry.setFilteringMode(filteringModeBox.getValue() == null ? entry.getFilteringMode() : filteringModeBox.getValue());
            entry.setFiltering(filteringField.getValue());
            entry.setPayload(payloadField.getValue());
            entry.setComment(commentField.getValue());
            lorebookEntryService.save(entry);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        close();
        if (onSaved != null) {
            onSaved.run();
        }
    }

    // for the tests of the dialog

    TextField getNameField() {
        return nameField;
    }

    TextArea getPayloadField() {
        return payloadField;
    }

    TextField getFilteringField() {
        return filteringField;
    }

    Checkbox getEnabledBox() {
        return enabledBox;
    }
}
