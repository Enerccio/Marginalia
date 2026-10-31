package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.arcoutline.model.OutlineNode;
import com.github.enerccio.marginalia.extensions.arcoutline.model.OutlineState;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import static com.github.enerccio.marginalia.extensions.arcoutline.ArcOutlineLocalization.T;

/**
 * Creates or edits an arc or a chapter. The fields are applied to the node when it is saved.
 */
public class OutlineNodeDialog extends Dialog {

    private final OutlineNode node;
    private final ArcOutlineView.StoryTarget storyTarget;
    private final Runnable onSaved;

    private final TextField title = new TextField(T.localized("Title"));
    private final TextArea description = new TextArea(T.localized("Description"));
    private final TextArea note = new TextArea(T.localized("Note (private)"));
    private final Select<OutlineState> state = new Select<>();
    private final IntegerField part = new IntegerField(T.localized("Linked part"));

    /**
     * @param header what the dialog says, like "New arc"
     * @param node   the arc or the chapter to edit, or a new one that the owner adds to the outline in
     *               {@code onSaved}
     * @param storyTarget the story the arc or chapter is in, null for none (the part can be typed, not taken from the story)
     * @param onSaved called after the node was changed so the owner stores it and refreshes what it shows
     */
    public OutlineNodeDialog(String header, OutlineNode node, ArcOutlineView.StoryTarget storyTarget, Runnable onSaved) {
        this.node = node;
        this.storyTarget = storyTarget;
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

        state.setLabel(T.localized("State"));
        state.setItems(OutlineState.values());
        state.setItemLabelGenerator(s -> T.localized(s.getLabel()));
        state.setValue(node.getState());
        state.setWidthFull();

        part.setMin(1);
        part.setStepButtonsVisible(true);
        part.setClearButtonVisible(true);
        part.setValue(node.getPart());
        part.setTooltipText(T.localized("The place of the part in the story, as the outline numbers it"));
        part.setWidth("14em");
        HorizontalLayout link = new HorizontalLayout(part);
        link.setWidthFull();
        link.setAlignItems(FlexComponent.Alignment.END);
        if (storyTarget != null) {
            Button linkVisible = new Button(Solid.LINK.create(), _ -> storyTarget.visiblePart(order -> {
                if (order == null) {
                    Notification.warning(T.localized("No part of the story is visible"));
                } else {
                    part.setValue(order);
                }
            }));
            linkVisible.setTooltipText(T.localized("Link the part that is the most visible in the story"));
            linkVisible.setAriaLabel(T.localized("Link the part that is the most visible in the story"));
            Button goTo = new Button(Solid.LOCATION_CROSSHAIRS.create(), _ -> goToPart());
            goTo.setTooltipText(T.localized("Go to the linked part"));
            goTo.setAriaLabel(T.localized("Go to the linked part"));
            link.add(linkVisible, goTo);
        }

        VerticalLayout content = new VerticalLayout(title, description, note, link, state);
        content.setPadding(false);
        add(content);

        Button save = new Button(T.localized("Save"), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button(T.localized("Cancel"), _ -> close());
        getFooter().add(cancel, save);
    }

    private void goToPart() {
        Integer order = part.getValue();
        if (order == null || order < 1) {
            Notification.warning(T.localized("No part is linked"));
        } else if (!storyTarget.showPart(order)) {
            Notification.warning(T.localized("The active branch has no part %d").formatted(order));
        }
    }

    private void save() {
        if (StringUtils.isBlank(title.getValue())) {
            title.setInvalid(true);
            title.setErrorMessage(T.localized("The title is required"));
            return;
        }
        node.setTitle(title.getValue().trim());
        node.setDescription(StringUtils.defaultString(description.getValue()));
        node.setNote(StringUtils.defaultString(note.getValue()));
        node.setPart(part.getValue() != null && part.getValue() >= 1 ? part.getValue() : null);
        // an unchanged state is not set again: finishing an arc would finish chapters that were reopened
        if (state.getValue() != null && state.getValue() != node.getState()) {
            node.setState(state.getValue());
        }
        onSaved.run();
        close();
    }
}
