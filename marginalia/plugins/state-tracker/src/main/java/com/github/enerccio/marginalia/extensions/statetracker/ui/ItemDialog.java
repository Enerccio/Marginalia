package com.github.enerccio.marginalia.extensions.statetracker.ui;

import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateItem;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * Sets a value of a character (the category from the character's categories, the text, locked) or edits one.
 */
public class ItemDialog extends Dialog {

    /** What the user saved. */
    @FunctionalInterface
    public interface Saved {
        void accept(Category category, String text, boolean locked);
    }

    private final Select<Category> category = new Select<>();
    private final TextArea text = new TextArea(T.localized(VALUE));
    private final Checkbox locked = new Checkbox(T.localized(LOCKED));
    private final Predicate<Category> hasCurrent;
    private final boolean editing;
    private final Saved onSave;

    /**
     * @param categories the categories of the character
     * @param selected   the category offered first, may be {@code null}
     * @param existing   the value to edit (its category can't change), {@code null} for a new one
     * @param hasCurrent whether the character has a current value of a category (a new one then replaces it)
     */
    public ItemDialog(List<Category> categories, Category selected, StateItem existing, Predicate<Category> hasCurrent, Saved onSave) {
        this.hasCurrent = hasCurrent;
        this.editing = existing != null;
        this.onSave = onSave;

        setHeaderTitle(existing == null ? T.localized(NEW_VALUE) : T.localized(EDIT_VALUE));
        setWidth("520px");
        setMaxWidth("95vw");

        category.setLabel(T.localized(CATEGORY));
        category.setItems(categories);
        category.setItemLabelGenerator(Category::getName);
        category.setValue(selected != null ? selected : categories.isEmpty() ? null : categories.getFirst());
        category.setEnabled(existing == null);
        category.setWidthFull();
        category.addValueChangeListener(_ -> showHelp());

        text.setWidthFull();
        text.setMinHeight("100px");
        text.setValue(existing == null ? "" : existing.getText());
        locked.setValue(existing != null && existing.isLocked());
        locked.setTooltipText(T.localized(THE_TIDY_LEAVES_A_LOCKED_VALUE_ALONE));
        showHelp();
        DialogFocus.onOpen(this, text);

        VerticalLayout content = new VerticalLayout(category, text, locked);
        content.setPadding(false);
        add(content);

        Button save = new Button(T.localized(SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(T.localized(CANCEL), _ -> close()), save);
        DialogShortcuts.submitWith(save, text);
    }

    /** The instruction of the category is the best help for what to write; a new value of a single one replaces the current one. */
    private void showHelp() {
        Category chosen = category.getValue();
        String help = chosen == null ? "" : chosen.getInstruction();
        if (!editing && chosen != null && chosen.getKind().isSingleValued() && hasCurrent.test(chosen)) {
            help = (help.isBlank() ? "" : help + " ") + T.localized(THE_CURRENT_VALUE_GOES_TO_THE_HISTORY);
        }
        text.setHelperText(help);
    }

    private void save() {
        String value = StringUtils.strip(text.getValue());
        if (StringUtils.isEmpty(value)) {
            text.setInvalid(true);
            text.setErrorMessage(T.localized(WRITE_SOMETHING));
            return;
        }
        if (category.getValue() == null) {
            category.setInvalid(true);
            return;
        }
        close();
        onSave.accept(category.getValue(), value, locked.getValue());
    }
}
