package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Presets;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Script;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.CommonStyles;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.EditDialog;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;

/**
 * The window that edits a real language: its name, the language as the model names it, the script, the honorifics, the reading, and the three
 * short texts (politeness, names, other rules). A new language begins as one of the {@link Presets}; choosing another preset fills the fields in again.
 * The language is changed only when <i>OK</i> is pressed and the fields are right.
 */
public class LanguageEditor extends EditDialog {

    private final TextField name = new TextField(T.localized(NAME));
    private final TextField tongue = new TextField(T.localized(THE_LANGUAGE_AS_THE_MODEL_NAMES_IT));
    private final Select<Script> script = new Select<>();
    private final Checkbox honorifics = new Checkbox(T.localized(WRITE_THE_HONORIFICS_OF_NAMES));
    private final Checkbox reading = new Checkbox(T.localized(ALSO_WRITE_HOW_IT_IS_READ));
    private final TextArea politeness = new TextArea(T.localized(POLITENESS_AND_SPEECH_LEVEL));
    private final TextArea names = new TextArea(T.localized(NAMES_OF_PEOPLE_AND_PLACES));
    private final TextArea rules = new TextArea(T.localized(OTHER_RULES));

    /**
     * @param language  the language to edit; a new one is changed by the preset the user chooses
     * @param isNew     whether the language is new (the presets are offered)
     * @param nameTaken whether the library has another language with the name
     * @param saved     run after the language was written from the fields
     */
    public LanguageEditor(RealLanguage language, boolean isNew, Predicate<String> nameTaken, Runnable saved) {
        super(isNew ? T.localized(NEW_LANGUAGE) : T.localized(EDIT_THE_LANGUAGE), saved);
        name.setWidthFull();
        tongue.setWidthFull();
        tongue.setHelperText(T.localized(THE_ANSWER_NAMES_THE_LANGUAGE_SO));
        script.setLabel(T.localized(SCRIPT));
        script.setWidthFull();
        script.setItems(Script.values());
        script.setItemLabelGenerator(LanguageEditor::label);
        for (TextArea area : new TextArea[]{politeness, names, rules}) {
            area.setWidthFull();
            area.setMinHeight("6rem");
            area.setMaxHeight("16rem");
        }
        politeness.setHelperText(T.localized(HOW_THE_LEVEL_OF_SPEECH_IS_CHOSEN));
        names.setHelperText(T.localized(HOW_NAMES_ARE_WRITTEN));
        rules.setHelperText(T.localized(ANYTHING_ELSE_THE_TRANSLATOR_HOLDS_TO));

        if (isNew) {
            Select<Presets.Preset> preset = new Select<>();
            preset.setLabel(T.localized(START_FROM));
            preset.setWidthFull();
            preset.setItems(Presets.all());
            preset.setItemLabelGenerator(Presets.Preset::label);
            preset.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    RealLanguage made = Presets.make(event.getValue().id());
                    show(made);
                }
            });
            body().add(preset);
            preset.setValue(Presets.all().getFirst());
        } else {
            show(language);
        }
        Span hint = new Span(T.localized(THERE_IS_NO_LEXICON_THE_MODEL_KNOWS_THE_LANGUAGE));
        hint.addClassName(CommonStyles.HINT);
        body().add(hint, name, tongue, script, honorifics, reading, politeness, names, rules);

        setValidator(() -> {
            if (StringUtils.isBlank(name.getValue())) {
                return T.localized(THE_NAME_IS_EMPTY);
            }
            if (nameTaken.test(name.getValue().strip())) {
                return T.localized(THERE_IS_A_LANGUAGE_WITH_THIS_NAME);
            }
            language.setName(name.getValue().strip());
            language.setTongue(tongue.getValue().strip());
            language.setScript(script.getValue() == null ? Script.NATIVE : script.getValue());
            language.setHonorifics(honorifics.getValue());
            language.setReading(reading.getValue());
            language.setPoliteness(politeness.getValue().strip());
            language.setNames(names.getValue().strip());
            language.setRules(rules.getValue().strip());
            return null;
        });
    }

    private void show(RealLanguage language) {
        name.setValue(language.getName());
        tongue.setValue(language.getTongue().equals(language.getName()) ? "" : language.getTongue());
        script.setValue(language.getScript());
        honorifics.setValue(language.isHonorifics());
        reading.setValue(language.isReading());
        politeness.setValue(language.getPoliteness());
        names.setValue(language.getNames());
        rules.setValue(language.getRules());
    }

    static String label(Script script) {
        return switch (script) {
            case KANA_KANJI -> T.localized(KANA_AND_KANJI);
            case KANA -> T.localized(KANA_ONLY);
            case ROMAJI -> T.localized(ROMAJI_LATIN_LETTERS);
            case NATIVE -> T.localized(ANOTHER_SCRIPT_NOT_CHECKED);
        };
    }
}
