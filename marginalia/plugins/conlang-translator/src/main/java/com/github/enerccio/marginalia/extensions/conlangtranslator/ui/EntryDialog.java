package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.translatorcommon.ui.EditDialog;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.ListEditor;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The window of one word of the lexicon: the form, its senses, the category, the kind, the register, the status, the rationale and the other
 * forms of the same concept. A word of the model that the author saves becomes the author's.
 */
public class EntryDialog extends EditDialog {

    private final TextField word = new TextField(T.localized(WORD));
    private final TextField script = new TextField(T.localized(IN_THE_SCRIPT));
    private final ComboBox<Category> category = new ComboBox<>(T.localized(CATEGORY));
    private final ComboBox<EntryKind> kind = new ComboBox<>(T.localized(KIND));
    private final ComboBox<Register> register = new ComboBox<>(T.localized(REGISTER));
    private final ComboBox<EntryStatus> status = new ComboBox<>(T.localized(STATUS));
    private final MultiSelectComboBox<LexiconEntry> variants = new MultiSelectComboBox<>(T.localized(OTHER_FORMS_OF_THE_SAME_CONCEPT));
    private final TextArea rationale = new TextArea(T.localized(WHY_THE_WORD_IS_AS_IT_IS));
    private final TextField rule = new TextField(T.localized(RULE_IT_FOLLOWS));
    private final TextArea notes = new TextArea(T.localized(NOTES));
    private final List<Sense> senses = new ArrayList<>();

    /**
     * @param entry    the word; written only when <i>OK</i> is pressed and the fields are right
     * @param isNew    whether it is not in the lexicon yet
     * @param language the language the word is in, for the categories, the registers and the other forms
     */
    public EntryDialog(LexiconEntry entry, boolean isNew, Language language, Runnable done) {
        super(isNew ? T.localized(NEW_WORD) : T.localized(EDIT_WORD), done);
        senses.addAll(entry.getSenses().stream().map(EntryDialog::copy).toList());
        setWidth("860px");

        word.setWidthFull();
        word.addClassName(ConlangStyles.WORD);
        script.setWidthFull();
        script.setHelperText(T.localized(EMPTY_WHEN_THE_LANGUAGE_HAS_NO_SCRIPT));

        category.setWidthFull();
        category.setItems(language.getCategories());
        category.setItemLabelGenerator(Category::getName);
        category.setClearButtonVisible(true);
        category.setValue(language.findCategory(entry.getCategory()));

        kind.setWidthFull();
        kind.setItems(EntryKind.values());
        kind.setItemLabelGenerator(value -> T.localized(kindLabel(value)));
        kind.setAllowCustomValue(false);

        register.setWidthFull();
        register.setItems(language.getRegisters());
        register.setItemLabelGenerator(Register::getName);
        register.setClearButtonVisible(true);
        register.setPlaceholder(T.localized(NEUTRAL));
        register.setValue(language.findRegister(entry.getRegister()));

        status.setWidthFull();
        status.setItems(EntryStatus.values());
        status.setItemLabelGenerator(value -> T.localized(statusLabel(value)));
        status.setAllowCustomValue(false);

        List<LexiconEntry> others = language.getLexicon().stream().filter(other -> !other.getId().equals(entry.getId())).toList();
        variants.setWidthFull();
        variants.setItems(others);
        variants.setItemLabelGenerator(EntryDialog::label);
        variants.setHelperText(T.localized(THE_FORMS_FOR_OTHER_REGISTERS));
        variants.select(others.stream().filter(other -> entry.getVariants().contains(other.getId())).toList());

        rationale.setWidthFull();
        rationale.setMinHeight("4rem");
        rule.setWidthFull();
        notes.setWidthFull();
        notes.setMinHeight("3rem");

        word.setValue(entry.getWord());
        script.setValue(entry.getScript());
        kind.setValue(entry.getKind());
        status.setValue(entry.getStatus());
        rationale.setValue(entry.getRationale());
        rule.setValue(entry.getRule());
        notes.setValue(entry.getNotes());

        Span origin = new Span(originText(entry));
        origin.addClassName(ConlangStyles.HINT);

        SenseList senseList = new SenseList(senses);
        senseList.setHeight("9rem");

        FormLayout main = new FormLayout(word, script, category, kind, register, status);
        main.setWidthFull();
        main.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("34rem", 2));

        body().add(main, new Span(T.localized(MEANINGS)), senseList, variants, rationale, rule, notes, origin);

        setValidator(() -> {
            if (StringUtils.isBlank(word.getValue())) {
                return T.localized(THE_WORD_IS_EMPTY);
            }
            if (senses.isEmpty()) {
                return T.localized(A_WORD_NEEDS_A_MEANING);
            }
            entry.setWord(word.getValue().strip());
            entry.setScript(script.getValue().strip());
            entry.setCategory(category.getValue() == null ? null : category.getValue().getId());
            entry.setKind(kind.getValue() == null ? EntryKind.ROOT : kind.getValue());
            entry.setRegister(register.getValue() == null ? null : register.getValue().getId());
            entry.setStatus(status.getValue() == null ? EntryStatus.NEW : status.getValue());
            entry.setVariants(variants.getValue().stream().map(LexiconEntry::getId).collect(Collectors.toCollection(ArrayList::new)));
            entry.setSenses(new ArrayList<>(senses));
            entry.setRationale(rationale.getValue().strip());
            entry.setRule(rule.getValue().strip());
            entry.setNotes(notes.getValue().strip());
            // a word of the model that the author saved is the author's now: the model does not change it any more
            entry.editedByAuthor();
            return null;
        });
    }

    private static Sense copy(Sense sense) {
        Sense copy = new Sense(sense.getGloss(), sense.getPartOfSpeech());
        copy.setNote(sense.getNote());
        return copy;
    }

    static String glosses(LexiconEntry entry) {
        return entry.getSenses().stream().map(Sense::getGloss).filter(StringUtils::isNotBlank).collect(Collectors.joining("; "));
    }

    static String label(LexiconEntry entry) {
        String gloss = glosses(entry);
        return gloss.isEmpty() ? entry.getWord() : entry.getWord() + " (" + gloss + ")";
    }

    static String originText(LexiconEntry entry) {
        Origin origin = entry.getOrigin();
        if (origin.getKind() == OriginKind.AUTHOR) {
            return T.localized(MADE_BY_THE_AUTHOR);
        }
        return T.localized(MADE_BY_THE_MODEL).formatted(StringUtils.defaultIfBlank(origin.getModel(), "?"));
    }

    static String kindLabel(EntryKind kind) {
        return switch (kind) {
            case ROOT -> KIND_ROOT;
            case COMPOUND -> KIND_COMPOUND;
            case AFFIX -> KIND_AFFIX;
            case PARTICLE -> KIND_PARTICLE;
            case NAME -> KIND_NAME;
            case NUMBER -> KIND_NUMBER;
            case PHRASE -> KIND_PHRASE;
            case OTHER -> KIND_OTHER;
        };
    }

    static String statusLabel(EntryStatus status) {
        return switch (status) {
            case NEW -> STATUS_NEW;
            case ACCEPTED -> STATUS_ACCEPTED;
            case LOCKED -> STATUS_LOCKED;
            case RETIRED -> STATUS_RETIRED;
        };
    }

    /** The senses of the word as a list with a window for one sense. */
    private static final class SenseList extends ListEditor<Sense> {

        SenseList(List<Sense> senses) {
            super(senses, () -> {
            });
            build();
        }

        @Override
        protected void addColumns(Grid<Sense> grid) {
            grid.addColumn(Sense::getGloss).setHeader(T.localized(MEANING)).setFlexGrow(3);
            grid.addColumn(Sense::getPartOfSpeech).setHeader(T.localized(PART_OF_SPEECH)).setFlexGrow(1);
            grid.addColumn(Sense::getNote).setHeader(T.localized(NOTE)).setFlexGrow(2);
        }

        @Override
        protected Sense create() {
            return new Sense();
        }

        @Override
        protected void edit(Sense sense, boolean isNew, Runnable done) {
            TextField gloss = new TextField(T.localized(MEANING));
            gloss.setWidthFull();
            gloss.setValue(sense.getGloss());
            TextField part = new TextField(T.localized(PART_OF_SPEECH));
            part.setWidthFull();
            part.setValue(sense.getPartOfSpeech());
            TextField note = new TextField(T.localized(NOTE));
            note.setWidthFull();
            note.setValue(sense.getNote());
            new EditDialog(isNew ? T.localized(NEW_MEANING) : T.localized(EDIT_MEANING), new VerticalLayout(gloss, part, note), () -> {
                if (StringUtils.isBlank(gloss.getValue())) {
                    return T.localized(THE_MEANING_IS_EMPTY);
                }
                sense.setGloss(gloss.getValue().strip());
                sense.setPartOfSpeech(part.getValue().strip());
                sense.setNote(note.getValue().strip());
                return null;
            }, done).open();
        }

        @Override
        protected String labelOf(Sense sense) {
            return sense.getGloss();
        }
    }
}
