package com.github.enerccio.marginalia.extensions.statetracker.ui;

import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.statetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * Starts tracking a character or edits one: the name, the aliases and the categories tracked for it. The categories offered are
 * the book's and those of the user that the book has no copy of yet; a picked user category is copied into the book by the
 * caller.
 */
public class TrackedCharacterDialog extends Dialog {

    /** What the user saved. */
    @FunctionalInterface
    public interface Saved {
        /**
         * @param categories the picked categories in the order offered: the book's own objects, or user categories to copy
         */
        void accept(String name, List<String> aliases, List<Category> categories);
    }

    private final TextField name = new TextField(T.localized(NAME));
    private final TextField aliases = new TextField(T.localized(ALIASES));
    private final CheckboxGroup<Category> categories = new CheckboxGroup<>(T.localized(TRACKED_CATEGORIES));
    private final StateBook state;
    private final TrackedCharacter existing;
    private final List<Category> offered = new ArrayList<>();
    private final Saved onSave;

    public TrackedCharacterDialog(StateBook state, List<Category> userCategories, TrackedCharacter existing, Saved onSave) {
        this.state = state;
        this.existing = existing;
        this.onSave = onSave;

        setHeaderTitle(existing == null ? T.localized(NEW_TRACKED_CHARACTER) : T.localized(EDIT_TRACKED_CHARACTER));
        setWidth("520px");
        setMaxWidth("95vw");

        name.setWidthFull();
        name.setValue(existing == null ? "" : existing.getName());
        aliases.setWidthFull();
        aliases.setHelperText(T.localized(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN));
        aliases.setValue(existing == null ? "" : String.join(", ", existing.getAliases()));

        offered.addAll(state.getCategories());
        for (Category user : userCategories) {
            if (state.copyOf(user.getId()) == null) {
                offered.add(user);
            }
        }
        categories.setItems(offered);
        categories.setItemLabelGenerator(c -> state.findCategory(c.getId()) != null ? c.getName()
                : T.localized(FROM_MY_CATEGORIES).formatted(c.getName()));
        categories.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        categories.setHelperText(T.localized(WHAT_THE_PLUGIN_KEEPS_FOR_THIS_CHARACTER));
        if (existing != null) {
            Set<Category> chosen = new LinkedHashSet<>(state.categoriesOf(existing));
            categories.setValue(chosen);
        }

        VerticalLayout content = new VerticalLayout(name, aliases, categories);
        content.setPadding(false);
        add(content);
        DialogFocus.onOpen(this, name);

        Button save = new Button(T.localized(SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(T.localized(CANCEL), _ -> close()), save);
        DialogShortcuts.submitWith(save, name, aliases);
    }

    private void save() {
        String value = StringUtils.strip(name.getValue());
        if (StringUtils.isEmpty(value)) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized(GIVE_THE_CHARACTER_A_NAME));
            return;
        }
        List<String> aliasList = StringUtils.isBlank(aliases.getValue()) ? List.of() : PresentCharacters.parse(aliases.getValue(), null);
        TrackedCharacter other = state.findClash(existing, value, aliasList);
        if (other != null) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized(THE_NAME_OR_AN_ALIAS_IS_USED_BY).formatted(other.getName()));
            return;
        }
        Set<Category> picked = categories.getValue();
        List<Category> ordered = offered.stream().filter(picked::contains).toList();
        close();
        onSave.accept(value, aliasList, ordered);
    }
}
