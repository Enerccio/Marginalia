package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.MetPerson;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.PresentCharacters;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * Starts tracking a character or edits one: the name, the other names it is written as and the people it has met.
 */
public class TrackedCharacterDialog extends Dialog {

    private final KnowledgeState state;
    private final TrackedCharacter character;
    private final boolean isNew;
    private final Runnable onSaved;

    private final TextField name = new TextField(T.localized(NAME));
    private final TextField aliases = new TextField(T.localized(ALIASES));
    private final TextArea met = new TextArea(T.localized(MET_PEOPLE));

    /**
     * @param existing the character to edit, {@code null} to track a new one
     * @param onSaved  called after the character was changed (and added to the state), the owner stores the state
     */
    public TrackedCharacterDialog(KnowledgeState state, TrackedCharacter existing, Runnable onSaved) {
        this.state = state;
        this.onSaved = onSaved;
        this.isNew = existing == null;
        this.character = isNew ? new TrackedCharacter() : existing;

        setHeaderTitle(isNew ? T.localized(NEW_TRACKED_CHARACTER) : T.localized(EDIT_TRACKED_CHARACTER));
        setWidth("480px");
        setMaxWidth("95vw");

        name.setRequiredIndicatorVisible(true);
        name.setWidthFull();
        name.setValue(StringUtils.defaultString(character.getName()));
        DialogFocus.onOpen(this, name);

        aliases.setWidthFull();
        aliases.setHelperText(T.localized(OTHER_NAMES_THE_CHARACTER_IS_WRITTEN));
        aliases.setValue(String.join(", ", character.getAliases()));

        met.setWidthFull();
        met.setMinHeight("80px");
        met.setHelperText(T.localized(PEOPLE_THE_CHARACTER_HAS_MET));
        met.setValue(character.getMet().stream().map(MetPerson::getName).collect(Collectors.joining(", ")));

        VerticalLayout content = new VerticalLayout(name, aliases, met);
        content.setPadding(false);
        add(content);

        Button save = new Button(T.localized(SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(T.localized(CANCEL), _ -> close()), save);
    }

    private void save() {
        String newName = StringUtils.strip(name.getValue());
        if (StringUtils.isEmpty(newName)) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized(GIVE_THE_CHARACTER_A_NAME));
            return;
        }
        List<String> newAliases = PresentCharacters.parse(aliases.getValue(), null);
        TrackedCharacter used = state.findClash(isNew ? null : character, newName, newAliases);
        if (used != null) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized(THE_NAME_OR_AN_ALIAS_IS_USED_BY).formatted(used.getName()));
            return;
        }
        character.setName(newName);
        character.setAliases(newAliases);
        character.setMetNames(PresentCharacters.parse(met.getValue(), null));
        if (isNew) {
            state.getCharacters().add(character);
        }
        close();
        onSaved.run();
    }
}
