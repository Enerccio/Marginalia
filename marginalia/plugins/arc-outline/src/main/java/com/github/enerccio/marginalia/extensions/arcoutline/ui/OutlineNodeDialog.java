package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.github.enerccio.marginalia.extensions.arcoutline.model.OutlineNode;
import com.github.enerccio.marginalia.extensions.arcoutline.model.OutlineState;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

/**
 * Creates or edits an arc or a chapter. The fields are applied to the node when it is saved.
 */
public class OutlineNodeDialog extends Dialog {

    private final OutlineNode node;
    private final Runnable onSaved;

    private final TextField title = new TextField("Title");
    private final TextArea description = new TextArea("Description");
    private final TextArea note = new TextArea("Note (private)");
    private final Select<OutlineState> state = new Select<>();

    /**
     * @param header what the dialog says, like "New arc"
     * @param node   the arc or the chapter to edit, or a new one that the owner adds to the outline in
     *               {@code onSaved}
     * @param onSaved called after the node was changed so the owner stores it and refreshes what it shows
     */
    public OutlineNodeDialog(String header, OutlineNode node, Runnable onSaved) {
        this.node = node;
        this.onSaved = onSaved;

        setHeaderTitle(header);
        setWidth("560px");
        setMaxWidth("95vw");
        setMaxHeight("90vh");
        setCloseOnOutsideClick(false);

        title.setRequiredIndicatorVisible(true);
        title.setWidthFull();
        DialogFocus.onOpen(this, title);
        title.setValue(StringUtils.defaultString(node.getTitle()));

        description.setWidthFull();
        description.setMinHeight("120px");
        description.setValue(StringUtils.defaultString(node.getDescription()));

        note.setWidthFull();
        note.setMinHeight("100px");
        note.setValue(StringUtils.defaultString(node.getNote()));

        state.setLabel("State");
        state.setItems(OutlineState.values());
        state.setItemLabelGenerator(OutlineState::getLabel);
        state.setValue(node.getState());
        state.setWidthFull();

        VerticalLayout content = new VerticalLayout(title, description, note, state);
        content.setPadding(false);
        add(content);

        Button save = new Button("Save", _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", _ -> close());
        getFooter().add(cancel, save);
    }

    private void save() {
        if (StringUtils.isBlank(title.getValue())) {
            title.setInvalid(true);
            title.setErrorMessage("The title is required");
            return;
        }
        node.setTitle(title.getValue().trim());
        node.setDescription(StringUtils.defaultString(description.getValue()));
        node.setNote(StringUtils.defaultString(note.getValue()));
        // an unchanged state is not set again: finishing an arc would finish chapters that were reopened
        if (state.getValue() != null && state.getValue() != node.getState()) {
            node.setState(state.getValue());
        }
        onSaved.run();
        close();
    }
}
