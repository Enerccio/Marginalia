package com.github.enerccio.marginalia.extensions.storytracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.Checkpoint;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.TrackerCheckpoints.Placed;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.storytracker.ui.StoryTrackerLocalization.*;

/**
 * The checkpoints of the tracker in the book whose part is on the active branch, newest first, each with a button that returns the
 * tracker to that point after a confirmation: what was done after it, by runs or by hand, is undone (and kept as a checkpoint of its
 * own), and the next run reads the parts after it again.
 */
public final class CheckpointsPanel {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private CheckpointsPanel() {
    }

    /**
     * @param checkpoints the checkpoints on the active branch, newest first
     * @param splitCounts whether the tracker tells added from retracted things; else {@link Checkpoint#added()} counts all the changes
     * @param enabled     whether a restore can be asked for now (no run goes on)
     * @param restore     returns the tracker to the checkpoint (and refreshes the view); called after the user confirmed
     * @param opened      whether the panel starts opened
     * @param onToggle    told when the user opens or closes it
     * @return {@code null} when there is no checkpoint
     */
    public static Component create(List<Placed> checkpoints, boolean splitCounts, boolean enabled, Consumer<Checkpoint> restore, boolean opened,
                                   Consumer<Boolean> onToggle) {
        if (checkpoints.isEmpty()) {
            return null;
        }
        VerticalLayout rows = new VerticalLayout();
        rows.setPadding(false);
        rows.setSpacing(false);
        for (Placed placed : checkpoints) {
            Div text = new Div(new Span(describe(placed, splitCounts)));
            text.addClassName(StoryTrackerStyles.LOG_TEXT);
            Div row = new Div(text);
            row.addClassName(StoryTrackerStyles.LOG_ROW);
            String confirm = placed.checkpoint().isStart() ? T.localized(RETURN_TO_THE_START_CONFIRM)
                    : T.localized(RETURN_TO_PART_N_CONFIRM).formatted(placed.position());
            Button button = new Button(Solid.CLOCK_ROTATE_LEFT.create(), _ -> ConfirmDialog.show(confirm,
                    () -> restore.accept(placed.checkpoint())));
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
            button.setAriaLabel(T.localized(RETURN_TO_THIS_POINT));
            button.setTooltipText(T.localized(RETURN_TO_THIS_POINT));
            button.setEnabled(enabled);
            row.add(button);
            rows.add(row);
        }
        Details details = new Details(T.localized(CHECKPOINTS_N).formatted(checkpoints.size()), rows);
        details.setOpened(opened);
        details.addOpenedChangeListener(e -> onToggle.accept(e.isOpened()));
        return details;
    }

    /**
     * What a restore of the checkpoint tells the author: where the tracker is now, and that what it had is kept.
     *
     * @param position the position of the part of the checkpoint on the branch (0 for the start)
     */
    public static String restoredMessage(Checkpoint checkpoint, int position) {
        String where = checkpoint.isStart() ? T.localized(THE_TRACKER_IS_BACK_AT_THE_START)
                : T.localized(THE_TRACKER_IS_BACK_AT_PART_N).formatted(position);
        return where + ". " + T.localized(THE_STATE_BEFORE_IS_KEPT_AS_A_CHECKPOINT);
    }

    /** "Part 12 · 2026-11-18 10:42 · before a run: 3 added, 1 retracted" */
    static String describe(Placed placed, boolean splitCounts) {
        Checkpoint checkpoint = placed.checkpoint();
        String kind = switch (checkpoint.kind()) {
            case RUN -> T.localized(checkpoint.before() ? BEFORE_A_RUN : AFTER_A_RUN);
            case TIDY -> T.localized(checkpoint.before() ? BEFORE_A_TIDY : AFTER_A_TIDY);
            case READ -> T.localized(checkpoint.before() ? BEFORE_A_READING_WITHOUT_THE_MODEL : AFTER_A_READING_WITHOUT_THE_MODEL);
            case RESTORE -> T.localized(BEFORE_A_RETURN_TO_A_CHECKPOINT);
        };
        boolean counted = checkpoint.kind() == Checkpoint.Kind.RUN || checkpoint.kind() == Checkpoint.Kind.TIDY;
        String counts = !counted ? ""
                : ": " + (splitCounts ? T.localized(N_ADDED_N_RETRACTED).formatted(checkpoint.added(), checkpoint.retracted())
                : T.localized(N_CHANGES).formatted(checkpoint.added()));
        String where = checkpoint.isStart() ? T.localized(CHECKPOINT_START_OF_THE_BOOK)
                : T.localized(CHECKPOINT_PART_N).formatted(placed.position());
        return where + " · " + TIME.format(Instant.ofEpochMilli(checkpoint.time()).atZone(ZoneId.systemDefault())) + " · " + kind + counts;
    }
}
