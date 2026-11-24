package com.github.enerccio.marginalia.extensions.statetracker.ui;

import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.CategoryKind;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * Makes a category or edits one: name, kind, the instruction for the model, strict, "none" when empty. Used for the user's
 * categories and for the book's.
 */
public class CategoryDialog extends Dialog {

    private final TextField name = new TextField(T.localized(NAME));
    private final Select<CategoryKind> kind = new Select<>();
    private final TextArea instruction = new TextArea(T.localized(INSTRUCTION_FOR_THE_MODEL));
    private final Checkbox strict = new Checkbox(T.localized(ONLY_WHAT_THE_TEXT_SAYS));
    private final Checkbox showWhenEmpty = new Checkbox(T.localized(SAY_NONE_WHEN_EMPTY));
    private final Predicate<String> nameTaken;
    private final Consumer<Category> onSave;

    /**
     * @param existing   the category to edit, {@code null} for a new one
     * @param kindLocked the kind can't change (characters have values of the category)
     * @param nameTaken  whether another category has the name already
     * @param onSave     gets a category with the definition the user saved (a new id; the caller copies what it needs)
     */
    public CategoryDialog(Category existing, boolean kindLocked, Predicate<String> nameTaken, Consumer<Category> onSave) {
        this.nameTaken = nameTaken;
        this.onSave = onSave;

        setHeaderTitle(existing == null ? T.localized(NEW_CATEGORY) : T.localized(EDIT_CATEGORY));
        setWidth("560px");
        setMaxWidth("95vw");

        name.setWidthFull();
        name.setValue(existing == null ? "" : existing.getName());

        kind.setLabel(T.localized(KIND));
        kind.setItems(CategoryKind.values());
        kind.setItemLabelGenerator(CategoryDialog::kindLabel);
        kind.setValue(existing == null ? CategoryKind.SINGLE : existing.getKind());
        kind.setHelperText(kindLocked ? T.localized(THE_KIND_CANT_CHANGE) : T.localized(KIND_HELP));
        kind.setEnabled(!kindLocked);
        kind.setWidthFull();

        instruction.setWidthFull();
        instruction.setMinHeight("120px");
        instruction.setHelperText(T.localized(WHAT_BELONGS_IN_THE_CATEGORY));
        instruction.setValue(existing == null ? "" : existing.getInstruction());

        strict.setValue(existing == null || existing.isStrict());
        strict.setTooltipText(T.localized(ON_THE_VALUE_CHANGES_ONLY_WHEN));
        showWhenEmpty.setValue(existing != null && existing.isShowWhenEmpty());
        showWhenEmpty.setTooltipText(T.localized(THE_PROMPT_SAYS_NONE));

        VerticalLayout content = new VerticalLayout(name, kind, instruction, strict, showWhenEmpty);
        content.setPadding(false);
        add(content);
        DialogFocus.onOpen(this, name);

        Button save = new Button(T.localized(SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(T.localized(CANCEL), _ -> close()), save);
        DialogShortcuts.submitWith(save, name, instruction);
    }

    /** The name of a kind as the user reads it. */
    public static String kindLabel(CategoryKind kind) {
        return switch (kind) {
            case SINGLE -> T.localized(SINGLE_VALUE);
            case LIST -> T.localized(LIST);
            case DESCRIPTION -> T.localized(DESCRIPTION);
        };
    }

    private void save() {
        String value = StringUtils.strip(name.getValue());
        if (StringUtils.isEmpty(value)) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized(GIVE_THE_CATEGORY_A_NAME));
            return;
        }
        if (nameTaken.test(value)) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized(A_CATEGORY_IS_CALLED_SO_ALREADY).formatted(value));
            return;
        }
        Category result = Category.create(value, kind.getValue() == null ? CategoryKind.SINGLE : kind.getValue(),
                StringUtils.strip(instruction.getValue()));
        result.setStrict(strict.getValue());
        result.setShowWhenEmpty(showWhenEmpty.getValue());
        close();
        onSave.accept(result);
    }
}
