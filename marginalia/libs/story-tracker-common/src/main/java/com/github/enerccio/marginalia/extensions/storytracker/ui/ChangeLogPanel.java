package com.github.enerccio.marginalia.extensions.storytracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.storytracker.log.LogEntry;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.storytracker.ui.StoryTrackerLocalization.*;

/**
 * What automatic runs applied without asking, newest first, each with a button that takes it back (the log itself is not editable).
 * Only the entries that can still be reverted have an enabled button. The tracker says what an entry reads like and does the revert.
 */
public final class ChangeLogPanel {

    /** How many entries are shown, the newest. */
    public static final int SHOWN = 30;

    private ChangeLogPanel() {
    }

    /**
     * @param log       the log, oldest first
     * @param describe  the line of an entry ("Fiora, Carrying (part 12): + lantern")
     * @param canRevert whether an entry can still be reverted
     * @param revert    takes an entry back (and refreshes the view)
     * @param opened    whether the panel starts opened
     * @param onToggle  told when the user opens or closes it
     * @return {@code null} when the log is empty
     */
    public static <E extends LogEntry> Component create(List<E> log, Function<E, String> describe, Predicate<E> canRevert,
                                                        Consumer<E> revert, boolean opened, Consumer<Boolean> onToggle) {
        if (log.isEmpty()) {
            return null;
        }
        VerticalLayout rows = new VerticalLayout();
        rows.setPadding(false);
        rows.setSpacing(false);
        for (int n = log.size() - 1; n >= 0 && log.size() - n <= SHOWN; n--) {
            E entry = log.get(n);
            Div text = new Div(new Span(describe.apply(entry)));
            text.addClassName(StoryTrackerStyles.LOG_TEXT);
            Div row = new Div(text);
            row.addClassName(StoryTrackerStyles.LOG_ROW);
            if (entry.isReverted()) {
                Span done = new Span(T.localized(REVERTED));
                done.addClassName(StoryTrackerStyles.STALE);
                row.add(done);
            } else {
                Button button = new Button(Solid.ROTATE_LEFT.create(), _ -> revert.accept(entry));
                button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
                button.setAriaLabel(T.localized(REVERT_THIS_CHANGE));
                button.setTooltipText(T.localized(REVERT_THIS_CHANGE));
                button.setEnabled(canRevert.test(entry));
                row.add(button);
            }
            rows.add(row);
        }
        Details details = new Details(T.localized(APPLIED_AUTOMATICALLY).formatted(log.size()), rows);
        details.setOpened(opened);
        details.addOpenedChangeListener(e -> onToggle.accept(e.isOpened()));
        return details;
    }
}
