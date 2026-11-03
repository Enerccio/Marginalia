package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.github.enerccio.marginalia.extensions.knowledgetracker.service.ExtractionRun;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.ExtractionRun.Phase;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.ExtractionRun.Status;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * Shows a run while it works. It is <b>not modal</b> and the user can <b>drag</b> it by its title: the story can be read and
 * edited while the model works. Closing it hides it, the run goes on. When the proposal is ready the panel says so and has a
 * <b>Review changes</b> button: the review dialog opens only when the user presses it.
 */
public class ProgressPanel extends Dialog {

    private final ExtractionRun run;
    private final Consumer<Status> listener = status -> UiAccess.run(this, () -> show(status));

    private final Span message = new Span();
    private final ProgressBar bar = new ProgressBar();
    private final VerticalLayout log = new VerticalLayout();
    private final Details logDetails = new Details(T.localized(LOG), log);
    private final Button cancel = new Button(T.localized(CANCEL));
    private final Button review = new Button(T.localized(REVIEW_CHANGES));
    private final Button close = new Button(T.localized(CLOSE));

    public ProgressPanel(ExtractionRun run) {
        this.run = run;
        setHeaderTitle(T.localized(PROCESSING_THE_STORY));
        // not modal: no overlay, the page stays usable; draggable by the title
        setModal(false);
        setDraggable(true);
        setResizable(false);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        // it floats at the side, over the sidebar, not over the middle of the story
        setLeft("24px");
        setTop("160px");

        bar.setIndeterminate(true);
        log.setPadding(false);
        log.setSpacing(false);
        logDetails.setVisible(false);
        VerticalLayout content = new VerticalLayout(message, bar, logDetails);
        content.setPadding(false);
        content.addClassName(KnowledgeTrackerStyles.PROGRESS);
        add(content);

        cancel.addClickListener(_ -> run.cancel());
        review.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        review.addClickListener(_ -> openReview());
        close.addClickListener(_ -> close());
        getFooter().add(cancel, review, close);
        show(run.getStatus());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        run.addListener(listener);
        show(run.getStatus());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        run.removeListener(listener);
        super.onDetach(detachEvent);
    }

    private void show(Status status) {
        Phase phase = status.phase();
        bar.setVisible(!phase.isFinished() && phase != Phase.REVIEW);
        cancel.setVisible(!phase.isFinished());
        review.setVisible(phase == Phase.REVIEW);
        close.setVisible(true);
        message.setText(switch (phase) {
            case PREPARING -> T.localized(PREPARING);
            case ASKING -> T.localized(ASKING_THE_MODEL_RUN).formatted(status.run());
            case REVIEW -> T.localized(WAITING_FOR_YOUR_REVIEW).formatted(status.proposal() == null ? 0 : status.proposal().changes().changes().size());
            case APPLYING -> T.localized(APPLYING_THE_CHANGES);
            case DONE -> run.isTidy() ? (status.added() + status.retracted() == 0 ? T.localized(NOTHING_TO_TIDY)
                    : T.localized(TIDY_DONE).formatted(status.retracted(), status.added()))
                    : T.localized(DONE_PARTS_ADDED_RETRACTED).formatted(status.partsRead(), status.added(), status.retracted());
            case FAILED -> T.localized(IT_FAILED).formatted(status.message());
            case CANCELLED -> T.localized(STOPPED_PARTS_ADDED_RETRACTED).formatted(status.partsRead(), status.added(), status.retracted());
        });
        log.removeAll();
        status.log().forEach(line -> {
            Div row = new Div(new Span(line));
            row.addClassName(KnowledgeTrackerStyles.COUNTS);
            log.add(row);
        });
        logDetails.setVisible(!status.log().isEmpty());
    }

    /** Opens the review of the proposal that waits; the user pressed the button. */
    public void openReview() {
        Status status = run.getStatus();
        if (status.phase() != Phase.REVIEW || status.proposal() == null) {
            return;
        }
        new ReviewDialog(status.run(), status.proposal(), run::apply, run::cancel).open();
    }
}
