package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;

import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * <b>Who has met whom</b>: on the left the tracked characters, on the right the other tracked characters with a check box
 * each, ticked when the character picked on the left has met them. Ticking records the meeting by hand, unticking forgets
 * it; every change is stored at once. With <b>Mutual</b> ticked a change is made on both sides, so the other character
 * has met the picked one too (or forgets it); it applies to the changes made while it is ticked, not to what is set.
 */
public class MetCharactersDialog extends Dialog {

    private final Supplier<KnowledgeState> state;
    private final Runnable onChanged;

    private final ListBox<TrackedCharacter> characters = new ListBox<>();
    private final Checkbox mutual = new Checkbox(T.localized(MUTUAL_BOTH_HAVE_MET_EACH_OTHER));
    private final Span hint = new Span();
    private final Div others = new Div();
    private boolean changed;

    /**
     * @param state     the current state of the book (read again on every change, the view may have reloaded it)
     * @param onChanged called after a meeting was added or removed, the owner stores the state
     * @param onClosed  called when the dialog is closed and something changed, the owner shows the new state
     */
    public MetCharactersDialog(Supplier<KnowledgeState> state, Runnable onChanged, Runnable onClosed) {
        this.state = state;
        this.onChanged = onChanged;

        setHeaderTitle(T.localized(WHO_HAS_MET_WHOM));
        setWidth("640px");
        setMaxWidth("95vw");
        addClassName(KnowledgeTrackerStyles.MET_DIALOG);

        characters.addClassName(KnowledgeTrackerStyles.MET_DIALOG_CHARACTERS);
        characters.setItemLabelGenerator(TrackedCharacter::getName);
        characters.setItems(state.get().getCharacters());
        characters.addValueChangeListener(e -> showOthers(e.getValue()));

        hint.addClassName(KnowledgeTrackerStyles.MET_DIALOG_HINT);
        Div right = new Div(mutual, hint, others);
        right.addClassName(KnowledgeTrackerStyles.MET_DIALOG_OTHERS);
        Div content = new Div(characters, right);
        content.addClassName(KnowledgeTrackerStyles.MET_DIALOG_CONTENT);
        add(content);

        Button close = new Button(T.localized(CLOSE), _ -> close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(close);
        DialogFocus.onOpen(this, close);

        addOpenedChangeListener(e -> {
            if (!e.isOpened() && changed) {
                onClosed.run();
            }
        });

        if (!state.get().getCharacters().isEmpty()) {
            characters.setValue(state.get().getCharacters().getFirst());
        }
    }

    private void showOthers(TrackedCharacter selected) {
        others.removeAll();
        if (selected == null) {
            hint.setText("");
            return;
        }
        hint.setText(T.localized(TICK_THE_CHARACTERS_THIS_ONE_HAS_MET).formatted(selected.getName()));
        for (TrackedCharacter other : state.get().getCharacters()) {
            if (other == selected) {
                continue;
            }
            Checkbox box = new Checkbox(other.getName(), selected.hasMet(other));
            box.addClassName(KnowledgeTrackerStyles.MET_DIALOG_ROW);
            box.addValueChangeListener(e -> {
                if (!e.isFromClient()) {
                    return;
                }
                // the view may have read the state again since the dialog opened: change the characters it stores now
                KnowledgeState current = state.get();
                TrackedCharacter who = current.findCharacter(selected.getId());
                TrackedCharacter whom = current.findCharacter(other.getId());
                if (who == null || whom == null) {
                    return;
                }
                boolean changedHere = who.setMet(whom, e.getValue());
                if (mutual.getValue()) {
                    // both know each other: the same change on the other side (it may be so there already)
                    changedHere |= whom.setMet(who, e.getValue());
                }
                if (changedHere) {
                    changed = true;
                    onChanged.run();
                }
            });
            others.add(box);
        }
    }
}
