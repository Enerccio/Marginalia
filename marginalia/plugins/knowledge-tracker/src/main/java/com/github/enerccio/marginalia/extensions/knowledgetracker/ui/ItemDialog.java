package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiConsumer;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * Adds an item (something a character knows or does not know) or edits one.
 */
public class ItemDialog extends Dialog {

    private final Select<ItemKind> kind = new Select<>();
    private final TextArea text = new TextArea(T.localized(TEXT));
    private final BiConsumer<String, ItemKind> onSave;

    /**
     * @param existing the item to edit, {@code null} for a new one
     * @param onSave   gets the text and the kind when the user saves
     */
    public ItemDialog(KnowledgeItem existing, BiConsumer<String, ItemKind> onSave) {
        this.onSave = onSave;

        setHeaderTitle(existing == null ? T.localized(NEW_ITEM) : T.localized(EDIT_ITEM));
        setWidth("520px");
        setMaxWidth("95vw");

        kind.setLabel(T.localized(KIND));
        kind.setItems(ItemKind.KNOWS, ItemKind.DOES_NOT_KNOW);
        kind.setItemLabelGenerator(k -> k == ItemKind.KNOWS ? T.localized(KNOWS) : T.localized(DOES_NOT_KNOW));
        kind.setValue(existing == null ? ItemKind.KNOWS : existing.getKind());
        kind.setWidthFull();

        text.setWidthFull();
        text.setMinHeight("120px");
        text.setHelperText(T.localized(WHAT_THE_CHARACTER_KNOWS_OR_DOES_NOT_KNOW));
        text.setValue(existing == null ? "" : StringUtils.defaultString(existing.getText()));
        DialogFocus.onOpen(this, text);

        VerticalLayout content = new VerticalLayout(kind, text);
        content.setPadding(false);
        add(content);

        Button save = new Button(T.localized(SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(T.localized(CANCEL), _ -> close()), save);
    }

    private void save() {
        String value = StringUtils.strip(text.getValue());
        if (StringUtils.isEmpty(value)) {
            text.setInvalid(true);
            text.setErrorMessage(T.localized(WRITE_SOMETHING));
            return;
        }
        close();
        onSave.accept(value, kind.getValue() == null ? ItemKind.KNOWS : kind.getValue());
    }
}
