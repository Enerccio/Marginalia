package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.*;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.*;

/**
 * Who speaks the language in a book: the default (everybody except the listed characters, or only the listed ones) and the notes of the
 * characters (gender, voice). It edits the {@link Speakers} it is given and tells {@code onChange} after every change, so the caller stores them.
 */
public class SpeakersEditor extends VerticalLayout {

    public SpeakersEditor(Speakers speakers, Runnable onChange) {
        this(speakers, null, onChange);
    }

    /**
     * @param understanding how the plugin inserts who does not follow the language into the prompt of the story, edited here too; null for none
     */
    public SpeakersEditor(Speakers speakers, UnderstandingSettings understanding, Runnable onChange) {
        setPadding(false);
        setSizeFull();
        setMinWidth("0");

        Span hint = new Span(TC.localized(ONLY_THE_DEFAULT_THE_TEXT_OF_THE_PART));
        hint.addClassName(CommonStyles.HINT);
        RadioButtonGroup<SpeakersMode> mode = new RadioButtonGroup<>();
        mode.setItems(SpeakersMode.values());
        mode.setItemLabelGenerator(value -> value == SpeakersMode.EVERYONE_EXCEPT ? TC.localized(EVERYBODY_SPEAKS_IT_EXCEPT) : TC.localized(ONLY_THE_LISTED_SPEAK_IT));
        mode.setValue(speakers.getMode());
        mode.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                speakers.setMode(event.getValue() == null ? SpeakersMode.EVERYONE_EXCEPT : event.getValue());
                onChange.run();
            }
        });
        CharactersList characters = new CharactersList(speakers.getCharacters(), onChange);
        add(hint, mode);
        if (understanding != null) {
            Checkbox inPrompt = new Checkbox(TC.localized(TELL_THE_STORY_MODEL_WHO_DOES_NOT_FOLLOW_IT));
            inPrompt.setValue(understanding.isInPrompt());
            IntegerField depth = new IntegerField(TC.localized(MESSAGES_FROM_THE_END));
            depth.setMin(0);
            depth.setStepButtonsVisible(true);
            depth.setValue(understanding.getDepth());
            depth.setEnabled(understanding.isInPrompt());
            inPrompt.addValueChangeListener(event -> {
                if (event.isFromClient()) {
                    understanding.setInPrompt(event.getValue());
                    depth.setEnabled(event.getValue());
                    onChange.run();
                }
            });
            depth.addValueChangeListener(event -> {
                if (event.isFromClient()) {
                    understanding.setDepth(event.getValue() == null ? 1 : event.getValue());
                    onChange.run();
                }
            });
            add(inPrompt, depth);
        }
        add(characters);
        setFlexGrow(1, characters);
    }

    private static final class CharactersList extends ListEditor<CharacterNote> {

        CharactersList(List<CharacterNote> characters, Runnable onChange) {
            super(characters, onChange);
            build();
        }

        @Override
        protected void addColumns(Grid<CharacterNote> grid) {
            grid.addColumn(CharacterNote::getName).setHeader(TC.localized(NAME)).setFlexGrow(1);
            grid.addColumn(note -> String.join(", ", note.getAliases())).setHeader(TC.localized(ALSO_CALLED)).setFlexGrow(1);
            grid.addColumn(CharacterNote::getGender).setHeader(TC.localized(GENDER)).setFlexGrow(0).setAutoWidth(true);
            grid.addColumn(CharacterNote::getVoice).setHeader(TC.localized(VOICE)).setFlexGrow(3);
            grid.addColumn(note -> note.isListed() ? TC.localized(YES_TEXT) : "").setHeader(TC.localized(LISTED)).setFlexGrow(0).setAutoWidth(true);
            grid.addColumn(note -> note.getLevel() == null ? "" : levelLabel(note.getLevel())).setHeader(TC.localized(UNDERSTANDING)).setFlexGrow(0).setAutoWidth(true);
        }

        @Override
        protected CharacterNote create() {
            return new CharacterNote();
        }

        @Override
        protected void edit(CharacterNote note, boolean isNew, Runnable done) {
            TextField title = new TextField(TC.localized(NAME));
            title.setWidthFull();
            title.setValue(note.getName());
            TextField aliases = new TextField(TC.localized(ALSO_CALLED));
            aliases.setWidthFull();
            aliases.setHelperText(TC.localized(SEPARATED_BY_COMMAS));
            aliases.setValue(String.join(", ", note.getAliases()));
            TextField gender = new TextField(TC.localized(GENDER));
            gender.setWidthFull();
            gender.setValue(note.getGender());
            TextArea voice = new TextArea(TC.localized(VOICE));
            voice.setWidthFull();
            voice.setHelperText(TC.localized(HOW_THE_CHARACTER_SPEAKS_IT));
            voice.setValue(note.getVoice());
            Checkbox listed = new Checkbox(TC.localized(LISTED_IN_THE_RULE_ABOVE));
            listed.setValue(note.isListed());
            Select<UnderstandingLevel> level = new Select<>();
            level.setLabel(TC.localized(UNDERSTANDING));
            level.setWidthFull();
            level.setItems(UnderstandingLevel.values());
            level.setEmptySelectionAllowed(true);
            level.setEmptySelectionCaption(TC.localized(UNDERSTANDING_NOT_SAID));
            level.setItemLabelGenerator(SpeakersEditor::levelLabel);
            level.setHelperText(TC.localized(UNDERSTANDING_HELP));
            level.setValue(note.getLevel());
            new EditDialog(isNew ? TC.localized(NEW_CHARACTER) : TC.localized(EDIT_CHARACTER), new VerticalLayout(title, aliases, gender, voice, listed, level), () -> {
                if (StringUtils.isBlank(title.getValue())) {
                    return TC.localized(THE_NAME_IS_EMPTY);
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
                note.setLevel(level.getValue());
                return null;
            }, done).open();
        }

        @Override
        protected String labelOf(CharacterNote note) {
            return note.getName();
        }
    }

    private static String levelLabel(UnderstandingLevel level) {
        // the Select asks for the label of the empty choice too (null)
        if (level == null) {
            return "";
        }
        return switch (level) {
            case NONE -> TC.localized(UNDERSTANDS_NOTHING);
            case FEW_WORDS -> TC.localized(KNOWS_A_FEW_WORDS);
            case FOLLOWS_MOST -> TC.localized(FOLLOWS_MOST_OF_IT);
            case FLUENT -> TC.localized(FOLLOWS_EVERYTHING);
        };
    }
}
