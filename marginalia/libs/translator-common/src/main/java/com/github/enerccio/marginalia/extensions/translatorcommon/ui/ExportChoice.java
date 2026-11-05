package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.ExportMode;
import com.vaadin.flow.component.select.Select;

import java.util.List;
import java.util.function.Function;

/**
 * The field of the export dialog that says what the export holds for the translated speech. The texts are the plugin's: an invented language and
 * a real one do not call the text of the story the same.
 */
public final class ExportChoice {

    private ExportChoice() {
    }

    /**
     * @param label  the label of the field
     * @param labels the text of each choice
     * @return the field, set to the text as it is
     */
    public static Select<ExportMode> field(String label, Function<ExportMode, String> labels) {
        Select<ExportMode> select = new Select<>();
        select.setLabel(label);
        select.setItems(List.of(ExportMode.values()));
        select.setItemLabelGenerator(labels::apply);
        select.setValue(ExportMode.TRANSLATED);
        return select;
    }
}
