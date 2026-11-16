package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.Change;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.ExtractionService.Proposal;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun;
import com.github.enerccio.marginalia.extensions.storytracker.ui.ProgressPanel;
import com.github.enerccio.marginalia.extensions.storytracker.ui.ReviewDialog;
import com.github.enerccio.marginalia.extensions.storytracker.ui.StoryTrackerStyles;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * The progress panel and the review of the Knowledge Tracker: the library's screens with what the tracker says about a run and how a
 * change of knowledge looks (retract an item, add one whose kind and text can be edited before it is applied).
 */
public final class KnowledgeRunScreens {

    /** What the panel says about a run of the Knowledge Tracker. */
    public static final ProgressPanel.Texts<Proposal> TEXTS = new ProgressPanel.Texts<>() {
        @Override
        public String done(TrackerRun<Proposal> run, TrackerRun.Status<Proposal> status) {
            if (run.isTidy()) {
                return status.added() + status.removed() == 0 ? T.localized(NOTHING_TO_TIDY)
                        : T.localized(TIDY_DONE).formatted(status.removed(), status.added());
            }
            return T.localized(DONE_PARTS_ADDED_RETRACTED).formatted(status.partsRead(), status.added(), status.removed());
        }

        @Override
        public String stopped(TrackerRun.Status<Proposal> status) {
            return T.localized(STOPPED_PARTS_ADDED_RETRACTED).formatted(status.partsRead(), status.added(), status.removed());
        }

        @Override
        public Dialog review(int run, Proposal proposal, Runnable apply, Runnable cancel) {
            return KnowledgeRunScreens.review(run, proposal, apply, cancel);
        }
    };

    private KnowledgeRunScreens() {
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
        List<Row> rows = proposal.changes().changes().stream().map(Row::new).toList();
        return new ReviewDialog(proposal.tidy() ? T.localized(REVIEW_THE_TIDY) : T.localized(REVIEW_THE_CHANGES_RUN).formatted(run),
                proposal.note(), rows, proposal.reasoning(),
                proposal.tidy() ? T.localized(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS) : T.localized(UNCHECKED_ADDITIONS_ARE_KEPT),
                proposal.changes().dropped(), proposal.tidy(), onApply, onCancel);
    }

    /** A change of knowledge as a row of the review. */
    private record Row(Change change) implements ReviewDialog.Row {

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
            return change.getType() == Change.Type.RETRACT ? T.localized(RETRACT) : T.localized(ADD);
        }

        @Override
        public boolean negative() {
            return change.getType() == Change.Type.RETRACT;
        }

        @Override
        public Component body() {
            if (change.getType() == Change.Type.ADD) {
                // an addition can be corrected before it is applied
                Select<ItemKind> kind = new Select<>();
                kind.setItems(ItemKind.KNOWS, ItemKind.DOES_NOT_KNOW);
                kind.setItemLabelGenerator(k -> k == ItemKind.KNOWS ? T.localized(KNOWS) : T.localized(DOES_NOT_KNOW));
                kind.setValue(change.getKind());
                kind.addClassName(KnowledgeTrackerStyles.REVIEW_KIND);
                kind.addValueChangeListener(e -> {
                    if (e.getValue() != null) {
                        change.setKind(e.getValue());
                    }
                });
                TextField text = new TextField();
                text.setWidthFull();
                text.setValue(change.getText());
                text.addValueChangeListener(e -> change.setText(e.getValue()));
                Div line = new Div(kind, text);
                line.addClassName(StoryTrackerStyles.REVIEW_ROW);
                return line;
            }
            Div old = new Div(new Span((change.getKind() == ItemKind.KNOWS ? T.localized(KNOWS) : T.localized(DOES_NOT_KNOW))
                    + ": " + change.getItemText()));
            old.addClassName(StoryTrackerStyles.OLD_ITEM);
            return old;
        }

        @Override
        public String evidence() {
            return change.getEvidence();
        }

        @Override
        public int messageNumber() {
            return change.isTidy() ? 0 : change.getMessageNumber();
        }

        @Override
        public boolean evidenceFound() {
            return change.isEvidenceFound();
        }
    }
}
