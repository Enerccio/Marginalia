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

import java.util.List;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * Shows a run while it works. It is <b>not modal</b> and the user can <b>drag</b> it by its title: the story can be read and
 * edited while the model works. Closing it hides it, the run goes on. When the proposal is ready the panel says so and has a
 * <b>Review changes</b> button: the review dialog opens only when the user presses it.
 */
public class ProgressPanel extends Dialog {

    private final ExtractionRun run;
    /** At most one update waits at a time: the run reports faster than the page can show, and the newest status is read when it runs. */
    private final Runnable statusUpdate = UiAccess.coalesced(this, () -> show(run.getStatus()));
    private final Runnable liveUpdate = UiAccess.coalesced(this, this::showLive);
    private final Consumer<Status> listener = _ -> statusUpdate.run();
    /** What the page shows of the thoughts and the log, to rebuild them only when they changed. */
    private ExtractionRun.Thought shownLastThought;
    private int shownThoughts = -1;
    private int shownLog = -1;
    /** The live boxes show the end of what the model writes: a long reasoning would be sent whole to the browser every time. */
    private static final int LIVE_CHARS = 6000;
    /** The page shows the latest thoughts only, each cut to its end: all of them, whole, make the browser slow down after a few runs. */
    private static final int SHOWN_THOUGHTS = 8;
    private static final int THOUGHT_CHARS = 12000;

    private final Span message = new Span();
    private final ProgressBar bar = new ProgressBar();
    private final VerticalLayout log = new VerticalLayout();
    private final Details logDetails = new Details(T.localized(LOG), log);
    private final VerticalLayout thoughtRows = new VerticalLayout();
    private final Details thoughtDetails = new Details(T.localized(WHAT_THE_MODEL_THOUGHT), thoughtRows);
    private final Div liveReasoning = new Div();
    private final Div liveAnswer = new Div();
    private final Details liveDetails = new Details(T.localized(THE_MODEL_IS_WRITING), new VerticalLayout(liveReasoning, liveAnswer));
    private final VerticalLayout content = new VerticalLayout();
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
        thoughtRows.setPadding(false);
        thoughtRows.setSpacing(false);
        thoughtDetails.setVisible(false);
        liveReasoning.addClassName(KnowledgeTrackerStyles.REASONING);
        liveAnswer.addClassName(KnowledgeTrackerStyles.REASONING);
        liveDetails.setOpened(true);
        liveDetails.setVisible(false);
        content.add(message, bar, liveDetails, logDetails, thoughtDetails);
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
        run.addLiveListener(liveUpdate);
        show(run.getStatus());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        run.removeListener(listener);
        run.removeLiveListener(liveUpdate);
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
        if (shownLog != status.log().size()) {
            shownLog = status.log().size();
            log.removeAll();
            status.log().forEach(line -> {
                Div row = new Div(new Span(line));
                row.addClassName(KnowledgeTrackerStyles.COUNTS);
                log.add(row);
            });
        }
        logDetails.setVisible(!status.log().isEmpty());

        // what the model thought and answered, one block per request: readable also when it proposed nothing and no review opens
        List<ExtractionRun.Thought> thoughts = run.getThoughts();
        // the run keeps only the last 30, so the newest one tells whether the list changed, not only its size
        ExtractionRun.Thought newest = thoughts.isEmpty() ? null : thoughts.getLast();
        if (shownThoughts != thoughts.size() || shownLastThought != newest) {
          shownThoughts = thoughts.size();
          shownLastThought = newest;
          thoughtRows.removeAll();
          for (ExtractionRun.Thought thought : thoughts.subList(Math.max(0, thoughts.size() - SHOWN_THOUGHTS), thoughts.size())) {
            Div title = new Div(new Span(T.localized(thought.tidy() ? TIDY_REQUEST_N_CHANGES : REQUEST_N_CHANGES)
                    .formatted(thought.run(), thought.changes())));
            title.addClassName(KnowledgeTrackerStyles.NAME);
            Div reasoning = new Div(new Span(thought.reasoning().isBlank() ? T.localized(THE_MODEL_SENT_NO_REASONING) : cut(thought.reasoning(), THOUGHT_CHARS)));
            reasoning.addClassName(KnowledgeTrackerStyles.REASONING);
            Div answer = new Div(new Span(thought.answer() == null ? "" : cut(thought.answer().strip(), THOUGHT_CHARS)));
            answer.addClassName(KnowledgeTrackerStyles.REASONING);
            thoughtRows.add(title, reasoning, new Details(T.localized(WHAT_THE_MODEL_ANSWERED), answer));
          }
        }
        thoughtDetails.setVisible(thoughtRows.getComponentCount() > 0);

        showLive();
    }

    /** What the model writes while it writes it: only while a request goes on. Cheap, called a few times a second. */
    private void showLive() {
        String[] live = run.getLive();
        boolean writing = run.getStatus().phase() == Phase.ASKING && !(live[0].isEmpty() && live[1].isEmpty());
        liveDetails.setVisible(writing);
        content.setWidth(writing ? "36em" : null);
        if (writing) {
            liveReasoning.setText(tail(live[0]));
            liveAnswer.setText(tail(live[1]));
            liveAnswer.setVisible(!live[1].isEmpty());
            // the newest words are the ones to see: the boxes keep scrolled to the end
            liveReasoning.getElement().executeJs("this.scrollTop = this.scrollHeight");
            liveAnswer.getElement().executeJs("this.scrollTop = this.scrollHeight");
        }
    }

    private static String tail(String text) {
        return cut(text, LIVE_CHARS);
    }

    private static String cut(String text, int max) {
        return text.length() > max ? "…" + text.substring(text.length() - max) : text;
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
