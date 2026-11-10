package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService.Insertion;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Where an import into a book puts its parts: after the last part of the story, or before or after a part the author picks from
 * the active branch of the story. A book without parts has only the end, then nothing is shown.
 */
final class InsertionPicker extends VerticalLayout {

    private enum Place {
        END(L.LABEL_AT_THE_END_OF_THE_STORY), BEFORE(L.LABEL_BEFORE_A_PART), AFTER(L.LABEL_AFTER_A_PART);

        private final L label;

        Place(L label) {
            this.label = label;
        }
    }

    private final Localization loc;
    private final List<ChatMessage> branch;
    private final RadioButtonGroup<Place> place = new RadioButtonGroup<>();
    private final ComboBox<Integer> part = new ComboBox<>();
    private Runnable onChange = () -> {
    };

    /**
     * @param branch the parts of the active branch of the story, the first part first
     */
    InsertionPicker(Localization loc, List<ChatMessage> branch) {
        this.loc = loc;
        this.branch = List.copyOf(branch);

        place.setLabel(loc.getValue(L.LABEL_IMPORT_PLACE));
        place.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        place.setItems(Place.values());
        place.setItemLabelGenerator(item -> loc.getValue(item.label));
        place.setValue(Place.END);
        place.addValueChangeListener(_ -> {
            part.setVisible(place.getValue() != Place.END);
            onChange.run();
        });

        part.setLabel(loc.getValue(L.LABEL_PART));
        part.setItems(IntStream.range(0, this.branch.size()).boxed().toList());
        part.setItemLabelGenerator(this::partLabel);
        part.setAllowCustomValue(false);
        part.setWidthFull();
        part.setVisible(false);
        part.addValueChangeListener(_ -> onChange.run());

        Span help = new Span(loc.getValue(L.HELP_IMPORT_PLACE));
        help.addClassName(CssStyles.HINT_TEXT);

        add(place, part, help);
        setPadding(false);
        setSpacing(false);
        setVisible(!this.branch.isEmpty());
    }

    private String partLabel(Integer index) {
        String number = String.format(loc.getValue(L.LABEL_ANNOTATION_PART_NUMBER), index + 1);
        String text = StringUtils.abbreviate(StringUtils.normalizeSpace(branch.get(index).getResponse()), 60);
        return text.isEmpty() ? number : number + ": " + text;
    }

    /**
     * What the picker does when the choice changes.
     */
    void onChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Sets the choice, as the author does (for the tests).
     *
     * @param kind  {@code END}, {@code BEFORE} or {@code AFTER}
     * @param index the number of the part in the branch (the first is 0), null for none
     */
    void choose(String kind, Integer index) {
        place.setValue(Place.valueOf(kind));
        part.setValue(index);
    }

    /**
     * @return the place the author chose, null while a part is still to be picked
     */
    Insertion insertion() {
        Place chosen = place.getValue();
        if (chosen == null || chosen == Place.END || branch.isEmpty()) {
            return Insertion.END;
        }
        Integer index = part.getValue();
        if (index == null) {
            return null;
        }
        if (chosen == Place.AFTER) {
            return Insertion.after(branch.get(index));
        }
        // before a part is after the one above it: the branch lists the parts in order, the first part has none above
        return index == 0 ? Insertion.START : Insertion.after(branch.get(index - 1));
    }
}
