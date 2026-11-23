package com.github.enerccio.marginalia.extensions.statetracker.ui;

import com.github.enerccio.marginalia.extensions.statetracker.extract.ClockChange;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateChange;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateExtractionService.Proposal;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun;
import com.github.enerccio.marginalia.extensions.storytracker.ui.ProgressPanel;
import com.github.enerccio.marginalia.extensions.storytracker.ui.ReviewDialog;
import com.github.enerccio.marginalia.extensions.storytracker.ui.StoryTrackerStyles;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * The progress panel and the review of the State Tracker: the library's screens with what the tracker says about a run and how a
 * change of the state looks (the category, the value that ends, the new value, editable before it is applied).
 */
public final class StateRunScreens {

    /** What the panel says about a run of the State Tracker. */
    public static final ProgressPanel.Texts<Proposal> TEXTS = new ProgressPanel.Texts<>() {
        @Override
        public String done(TrackerRun<Proposal> run, TrackerRun.Status<Proposal> status) {
            return T.localized(DONE_PARTS_CHANGES_APPLIED).formatted(status.partsRead(), status.added());
        }

        @Override
        public String stopped(TrackerRun.Status<Proposal> status) {
            return T.localized(STOPPED_PARTS_CHANGES_APPLIED).formatted(status.partsRead(), status.added());
        }

        @Override
        public Dialog review(int run, Proposal proposal, Runnable apply, Runnable cancel) {
            return StateRunScreens.review(run, proposal, apply, cancel);
        }
    };

    private StateRunScreens() {
    }

    /** The progress panel of a run. */
    public static ProgressPanel<Proposal> panel(TrackerRun<Proposal> run) {
        return new ProgressPanel<>(run, TEXTS);
    }

    /**
     * The review of a proposal: a table of changes grouped by tracked character.
     *
     * @param run the number of the run, for the title
     */
    public static Dialog review(int run, Proposal proposal, Runnable onApply, Runnable onCancel) {
        // the story time first (its own group), then the characters
        List<ReviewDialog.Row> rows = new java.util.ArrayList<>(proposal.clock().stream().map(ClockRow::new).toList());
        rows.addAll(proposal.changes().stream().map(Row::new).toList());
        String title = proposal.tidy() ? T.localized(REVIEW_THE_TIDY) : T.localized(REVIEW_THE_CHANGES_OF_RUN).formatted(run);
        String unchecked = T.localized(proposal.tidy() ? AN_UNCHECKED_TIDY_CHANGE_KEEPS_THE_VALUE : AN_UNCHECKED_CHANGE_IS_NOT_APPLIED);
        return new ReviewDialog(title, proposal.note(), rows, proposal.reasoning(), unchecked, proposal.left(), proposal.tidy(), onApply,
                onCancel);
    }

    /** The name of an action as the review shows it. */
    static String label(StateChange.Action action) {
        return switch (action) {
            case SET -> T.localized(SET);
            case CLEAR -> T.localized(CLEAR);
            case ADD -> T.localized(ADD);
            case REMOVE -> T.localized(REMOVE);
            case REPLACE -> T.localized(REPLACE);
        };
    }

    /** What a part says about the time of the story, as a row of the review (the group "Story time"). */
    private record ClockRow(ClockChange change) implements ReviewDialog.Row {

        @Override
        public String group() {
            return "\u0000clock";
        }

        @Override
        public String groupTitle() {
            return T.localized(STORY_TIME);
        }

        @Override
        public boolean isApply() {
            return change.isApply();
        }

        @Override
        public void setApply(boolean apply) {
            change.setApply(apply);
        }

        @Override
        public String label() {
            return T.localized(switch (change.getMark().getKind()) {
                case ADVANCE -> TIME_PASSES;
                case DAY, SET -> DAY;
                case DATE -> DATE;
                case FLASHBACK -> FLASHBACK;
            });
        }

        @Override
        public boolean negative() {
            return false;
        }

        @Override
        public Component body() {
            return new Div(new Span(ClockText.of(change.getMark())));
        }

        @Override
        public String evidence() {
            return change.getMark().getEvidence();
        }

        @Override
        public int messageNumber() {
            return change.getPartNumber();
        }

        @Override
        public boolean evidenceFound() {
            return change.isEvidenceFound();
        }
    }

    /** A change of the state as a row of the review. */
    private record Row(StateChange change) implements ReviewDialog.Row {

        @Override
        public String group() {
            return change.getCharacterId();
        }

        @Override
        public String groupTitle() {
            return change.getCharacterName();
        }

        @Override
        public boolean isApply() {
            return change.isApply();
        }

        @Override
        public void setApply(boolean apply) {
            change.setApply(apply);
        }

        @Override
        public String label() {
            return StateRunScreens.label(change.getAction());
        }

        @Override
        public boolean negative() {
            return change.getAction() == StateChange.Action.CLEAR || change.getAction() == StateChange.Action.REMOVE;
        }

        @Override
        public Component body() {
            Div box = new Div();
            Div category = new Div(new Span(change.getCategoryName()));
            category.addClassName(StoryTrackerStyles.NAME);
            box.add(category);
            if (change.getItemText() != null) {
                // the value that ends: replaced, cleared or removed
                Div old = new Div(new Span(change.getItemText()));
                old.addClassName(StoryTrackerStyles.OLD_ITEM);
                box.add(old);
            }
            if (change.getAction() == StateChange.Action.SET || change.getAction() == StateChange.Action.ADD
                    || change.getAction() == StateChange.Action.REPLACE) {
                // the new value can be corrected before it is applied
                TextField value = new TextField();
                value.setWidthFull();
                value.setValue(change.getValue() == null ? "" : change.getValue());
                value.setAriaLabel(T.localized(VALUE));
                value.addValueChangeListener(e -> change.setValue(e.getValue()));
                box.add(value);
            }
            return box;
        }

        @Override
        public String evidence() {
            return change.getEvidence();
        }

        @Override
        public int messageNumber() {
            return change.getPartNumber();
        }

        @Override
        public boolean evidenceFound() {
            return change.isEvidenceFound();
        }
    }
}
