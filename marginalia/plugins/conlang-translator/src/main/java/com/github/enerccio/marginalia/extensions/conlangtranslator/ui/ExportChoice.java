package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.ExportMode;
import com.vaadin.flow.component.select.Select;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The field of the export dialog that says what the export holds for the translated speech.
 */
public final class ExportChoice {

    private ExportChoice() {
    }

    /**
     * @return the field, set to the text as it is
     */
    public static Select<ExportMode> field() {
        Select<ExportMode> select = new Select<>();
        select.setLabel(T.localized(TRANSLATED_SPEECH));
        select.setItems(List.of(ExportMode.values()));
        select.setItemLabelGenerator(ExportChoice::label);
        select.setValue(ExportMode.CONLANG);
        return select;
    }

    static String label(ExportMode mode) {
        return switch (mode) {
            case CONLANG -> T.localized(THE_CONLANG_AS_IT_IS);
            case SOURCE -> T.localized(ENGLISH_ONLY);
            case CONLANG_NOTES -> T.localized(THE_CONLANG_THE_ENGLISH_IN_FOOTNOTES);
            case SOURCE_NOTES -> T.localized(THE_ENGLISH_THE_CONLANG_IN_FOOTNOTES);
        };
    }
}
