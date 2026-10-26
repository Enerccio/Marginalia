package com.github.enerccio.marginalia.extensions.charactertracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.charactertracker.model.CharacterBook;
import com.github.enerccio.marginalia.extensions.charactertracker.model.StoryCharacter;
import com.github.enerccio.marginalia.extensions.charactertracker.service.CharacterTrackerService;
import com.github.enerccio.marginalia.ui.dialogs.story.NewTurnForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.charactertracker.CharacterTrackerLocalization.T;

/**
 * A button at the end of the POV character and the present characters of the new turn form that lists the characters
 * of the book; a click on a name puts it into the field (the POV character is replaced, the name is added to the
 * present characters). The fields stay free text, so a name can still be typed.
 */
public final class TurnCharacterPicker {

    private TurnCharacterPicker() {
    }

    /**
     * Adds the buttons to the fields. The characters are read when the form is built and again when a list is opened.
     */
    public static void attach(NewTurnForm form, TextField pov, TextArea present, Manuscript manuscript,
                              CharacterTrackerService service) {
        pov.setSuffixComponent(createButton(manuscript, service, T.localized("Pick the POV character"),
                name -> form.setPovCharacter(name)));
        present.setSuffixComponent(createButton(manuscript, service, T.localized("Add a present character"),
                name -> form.setPresentCharacters(withName(form.toTurnInput().presentCharacters(), name))));
    }

    /**
     * Removes the buttons, when the extension is unloaded.
     */
    public static void detach(TextField pov, TextArea present) {
        pov.setSuffixComponent(null);
        present.setSuffixComponent(null);
    }

    private static Button createButton(Manuscript manuscript, CharacterTrackerService service, String tooltip,
                                       Consumer<String> onPick) {
        Button button = new Button(Solid.USER_PLUS.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        button.setTooltipText(tooltip);
        button.getElement().setAttribute("aria-label", tooltip);

        ContextMenu menu = new ContextMenu();
        menu.setTarget(button);
        menu.setOpenOnClick(true);
        Runnable fill = () -> {
            menu.removeAll();
            boolean any = false;
            for (StoryCharacter character : book(manuscript, service).sortedCharacters()) {
                if (StringUtils.isNotBlank(character.getName())) {
                    String name = character.getName().strip();
                    menu.addItem(name, _ -> onPick.accept(name));
                    any = true;
                }
            }
            if (!any) {
                menu.addItem(T.localized("There are no characters yet")).setEnabled(false);
            }
        };
        fill.run();
        // the characters may have been added or renamed since, the list is read again when it opens
        menu.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                fill.run();
            }
        });
        return button;
    }

    private static CharacterBook book(Manuscript manuscript, CharacterTrackerService service) {
        try {
            // the story view keeps the book it was opened with, the characters may have been saved since
            return service.load(manuscript);
        } catch (Exception e) {
            return service.read(manuscript);
        }
    }

    /**
     * The present characters with the name added at the end, unchanged when it is there already.
     */
    static String withName(String present, String name) {
        String current = StringUtils.defaultString(present);
        for (String part : current.split("[,;\\n]")) {
            if (part.strip().equalsIgnoreCase(name)) {
                return current;
            }
        }
        String base = StringUtils.stripEnd(current, " \t\r\n,;");
        return base.isEmpty() ? name : base + ", " + name;
    }
}
