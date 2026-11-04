package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.CharacterNote;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Speakers;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.SpeakersMode;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * Who speaks the language in a book: the default (everybody except the listed characters, or only the listed ones) and the notes of the
 * characters (gender, voice). It edits the {@link Speakers} it is given and tells {@code onChange} after every change, so the caller stores them.
 */
public class SpeakersEditor extends VerticalLayout {

    public SpeakersEditor(Speakers speakers, Runnable onChange) {
        setPadding(false);
        setSizeFull();
        setMinWidth("0");

        Span hint = new Span(T.localized(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART));
        hint.addClassName(ConlangStyles.HINT);
        RadioButtonGroup<SpeakersMode> mode = new RadioButtonGroup<>();
        mode.setItems(SpeakersMode.values());
        mode.setItemLabelGenerator(value -> value == SpeakersMode.EVERYONE_EXCEPT ? T.localized(EVERYBODY_SPEAKS_IT_EXCEPT) : T.localized(ONLY_THE_LISTED_SPEAK_IT));
        mode.setValue(speakers.getMode());
        mode.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                speakers.setMode(event.getValue() == null ? SpeakersMode.EVERYONE_EXCEPT : event.getValue());
                onChange.run();
            }
        });
        CharactersList characters = new CharactersList(speakers.getCharacters(), onChange);
        add(hint, mode, characters);
        setFlexGrow(1, characters);
    }

    private static final class CharactersList extends ListEditor<CharacterNote> {

        CharactersList(List<CharacterNote> characters, Runnable onChange) {
            super(characters, onChange);
            build();
        }

        @Override
        protected void addColumns(Grid<CharacterNote> grid) {
            grid.addColumn(CharacterNote::getName).setHeader(T.localized(NAME)).setFlexGrow(1);
            grid.addColumn(note -> String.join(", ", note.getAliases())).setHeader(T.localized(ALSO_CALLED)).setFlexGrow(1);
            grid.addColumn(CharacterNote::getGender).setHeader(T.localized(GENDER)).setFlexGrow(0).setAutoWidth(true);
            grid.addColumn(CharacterNote::getVoice).setHeader(T.localized(VOICE)).setFlexGrow(3);
            grid.addColumn(note -> note.isListed() ? T.localized(YES_TEXT) : "").setHeader(T.localized(LISTED)).setFlexGrow(0).setAutoWidth(true);
        }

        @Override
        protected CharacterNote create() {
            return new CharacterNote();
        }

        @Override
        protected void edit(CharacterNote note, boolean isNew, Runnable done) {
            TextField title = new TextField(T.localized(NAME));
            title.setWidthFull();
            title.setValue(note.getName());
            TextField aliases = new TextField(T.localized(ALSO_CALLED));
            aliases.setWidthFull();
            aliases.setHelperText(T.localized(SEPARATED_BY_COMMAS));
            aliases.setValue(String.join(", ", note.getAliases()));
            TextField gender = new TextField(T.localized(GENDER));
            gender.setWidthFull();
            gender.setValue(note.getGender());
            TextArea voice = new TextArea(T.localized(VOICE));
            voice.setWidthFull();
            voice.setHelperText(T.localized(HOW_THE_CHARACTER_SPEAKS_IT));
            voice.setValue(note.getVoice());
            Checkbox listed = new Checkbox(T.localized(LISTED_IN_THE_RULE_ABOVE));
            listed.setValue(note.isListed());
            new EditDialog(isNew ? T.localized(NEW_CHARACTER) : T.localized(EDIT_CHARACTER), new VerticalLayout(title, aliases, gender, voice, listed), () -> {
                if (StringUtils.isBlank(title.getValue())) {
                    return T.localized(THE_NAME_IS_EMPTY);
                }
                note.setName(title.getValue().strip());
                List<String> parsed = new ArrayList<>();
                for (String alias : aliases.getValue().split(",")) {
                    if (!alias.isBlank()) {
                        parsed.add(alias.strip());
                    }
                }
                note.setAliases(parsed);
                note.setGender(gender.getValue().strip());
                note.setVoice(voice.getValue().strip());
                note.setListed(listed.getValue());
                return null;
            }, done).open();
        }

        @Override
        protected String labelOf(CharacterNote note) {
            return note.getName();
        }
    }
}
