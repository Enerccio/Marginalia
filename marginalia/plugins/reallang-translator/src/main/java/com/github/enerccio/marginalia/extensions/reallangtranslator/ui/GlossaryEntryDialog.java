package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryEntry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryKind;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.EditDialog;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;

/**
 * The window that edits one entry of the glossary: the word as the story writes it, how the language writes it (or that it stays as it is), how it is
 * read and a note for the translator. The entry is changed only when <i>OK</i> is pressed and the fields are right.
 */
public class GlossaryEntryDialog extends EditDialog {

    private final TextField source = new TextField(T.localized(WORD_IN_THE_STORY));
    private final Select<GlossaryKind> kind = new Select<>();
    private final TextField written = new TextField(T.localized(HOW_IT_IS_WRITTEN));
    private final TextField reading = new TextField(T.localized(HOW_IT_IS_READ));
    private final TextArea note = new TextArea(T.localized(A_FEW_WORDS_FOR_THE_TRANSLATOR));
    private final Checkbox enabled = new Checkbox(T.localized(THE_ENTRY_APPLIES));

    /**
     * @param entry the entry to edit; written from the fields on OK
     * @param isNew whether the entry is new
     * @param done  run after the entry was written from the fields
     */
    public GlossaryEntryDialog(GlossaryEntry entry, boolean isNew, Runnable done) {
        super(isNew ? T.localized(NEW_GLOSSARY_ENTRY) : T.localized(EDIT_THE_GLOSSARY_ENTRY), done);
        for (TextField field : new TextField[]{source, written, reading}) {
            field.setWidthFull();
        }
        note.setWidthFull();
        note.setMinHeight("5rem");
        kind.setLabel(T.localized(IN_THE_TRANSLATION));
        kind.setWidthFull();
        kind.setItems(GlossaryKind.values());
        kind.setItemLabelGenerator(value -> value == GlossaryKind.KEEP ? T.localized(KEEP_IT_AS_IT_IS) : T.localized(WRITTEN_AS_BELOW));
        kind.addValueChangeListener(event -> {
            boolean keep = event.getValue() == GlossaryKind.KEEP;
            written.setEnabled(!keep);
            reading.setEnabled(!keep);
        });
        source.setValue(entry.getSource());
        kind.setValue(entry.getKind());
        written.setValue(entry.getWritten());
        reading.setValue(entry.getReading());
        note.setValue(entry.getNote());
        enabled.setValue(entry.isEnabled());
        body().add(source, kind, written, reading, note, enabled);
        setValidator(() -> {
            if (StringUtils.isBlank(source.getValue())) {
                return T.localized(THE_WORD_IS_EMPTY);
            }
            if (kind.getValue() != GlossaryKind.KEEP && StringUtils.isBlank(written.getValue())) {
                return T.localized(WRITE_HOW_THE_WORD_IS_WRITTEN_OR_KEEP_IT);
            }
            entry.setSource(source.getValue().strip());
            entry.setKind(kind.getValue() == null ? GlossaryKind.WRITTEN : kind.getValue());
            entry.setWritten(entry.getKind() == GlossaryKind.KEEP ? "" : written.getValue().strip());
            entry.setReading(entry.getKind() == GlossaryKind.KEEP ? "" : reading.getValue().strip());
            entry.setNote(note.getValue().strip());
            entry.setEnabled(enabled.getValue());
            entry.setProposed(false);
            return null;
        });
    }
}
