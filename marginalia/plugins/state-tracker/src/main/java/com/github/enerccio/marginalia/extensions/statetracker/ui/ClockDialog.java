package com.github.enerccio.marginalia.extensions.statetracker.ui;

import com.github.enerccio.marginalia.extensions.statetracker.model.ClockMark;
import com.github.enerccio.marginalia.extensions.statetracker.model.StoryTime;
import com.github.enerccio.marginalia.extensions.statetracker.model.TimeOfDay;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * Sets the story time after a part by hand: a fixed point (the day, the time of day, the date) that a reading of the part does not
 * replace and that the clock counts on from. Opens with the time the clock has there.
 */
public class ClockDialog extends Dialog {

    private final IntegerField day = new IntegerField(T.localized(DAY));
    private final Select<TimeOfDay> timeOfDay = new Select<>();
    private final DatePicker date = new DatePicker(T.localized(DATE));
    private final String part;
    private final Consumer<ClockMark> onSave;

    /**
     * @param position the position of the part, for the title
     * @param current  the time after the part now
     * @param fixed    whether the part has a fixed point (then it can be removed)
     * @param onSave   the new fixed point, or {@code null} to remove it
     */
    public ClockDialog(String part, int position, StoryTime current, boolean fixed, Consumer<ClockMark> onSave) {
        this.part = part;
        this.onSave = onSave;
        setHeaderTitle(T.localized(STORY_TIME_AFTER_PART_N).formatted(position));

        day.setMin(1);
        day.setStepButtonsVisible(true);
        day.setValue(current.day());
        day.setWidthFull();
        timeOfDay.setLabel(T.localized(TIME_OF_DAY));
        timeOfDay.setItems(TimeOfDay.values());
        timeOfDay.setEmptySelectionAllowed(true);
        timeOfDay.setItemLabelGenerator(t -> t == null ? "" : ClockText.of(t));
        timeOfDay.setValue(current.timeOfDay());
        timeOfDay.setWidthFull();
        date.setValue(current.date());
        date.setClearButtonVisible(true);
        date.setHelperText(T.localized(OPTIONAL_A_CALENDAR_DATE));
        date.setWidthFull();
        Div help = new Div(new Span(T.localized(A_TIME_SET_HERE_IS_KEPT)));
        help.addClassName(StateTrackerStyles.MUTED);

        VerticalLayout content = new VerticalLayout(help, day, timeOfDay, date);
        content.setPadding(false);
        add(content);
        DialogFocus.onOpen(this, day);

        Button save = new Button(T.localized(SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button(T.localized(CANCEL), _ -> close());
        if (fixed) {
            Button remove = new Button(T.localized(REMOVE_THE_FIXED_POINT), _ -> {
                close();
                onSave.accept(null);
            });
            remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            getFooter().add(remove);
        }
        getFooter().add(cancel, save);
        DialogShortcuts.submitWith(save, day);
    }

    private void save() {
        if (day.getValue() == null || day.getValue() < 1) {
            day.setInvalid(true);
            return;
        }
        close();
        onSave.accept(new ClockMark(part, ClockMark.Kind.SET).day(day.getValue()).timeOfDay(timeOfDay.getValue())
                .date(date.getValue() == null ? null : date.getValue().toString()));
    }
}
