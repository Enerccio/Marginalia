package com.github.enerccio.marginalia.extensions.storytracker.ui;

import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun.Phase;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun.Status;
import com.github.enerccio.marginalia.ui.widgets.FloatingWindow;
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

import static com.github.enerccio.marginalia.extensions.storytracker.ui.StoryTrackerLocalization.*;

/**
 * Shows a run while it works. It is <b>not modal</b> and the user can <b>drag</b> it by its title: the story can be read and edited
 * while the model works. It is the run's entry in the panel of the runs ({@link FloatingWindow}): closing it while the model works
 * folds it into that panel, where it opens again; the run goes on. When the proposal is ready the panel says so and has a <b>Review
 * changes</b> button: the review dialog opens only when the user presses it.
 *
 * @param <P> the proposal of the tracker
 */
public class ProgressPanel<P extends TrackerRun.Proposal> extends Dialog {

    /** What the tracker says about its runs. */
    public interface Texts<P extends TrackerRun.Proposal> {
        /** The message of a run that is done. */
        String done(TrackerRun<P> run, Status<P> status);

        /** The message of a run that was stopped. */
        String stopped(Status<P> status);

        /** The review of the proposal of the run, with what Apply and Cancel do. */
        Dialog review(int run, P proposal, Runnable apply, Runnable cancel);
    }

    private final TrackerRun<P> run;
    private final Texts<P> texts;
    /** At most one update waits at a time: the run reports faster than the page can show, and the newest status is read when it runs. */
    private final Runnable statusUpdate;
    private final Runnable liveUpdate;
    private final Consumer<Status<P>> listener;
    /** What the page shows of the thoughts and the log, to rebuild them only when they changed. */
    private TrackerRun.Thought shownLastThought;
    private int shownThoughts = -1;
    private int shownLog = -1;
    /** The live boxes show the end of what the model writes: a long reasoning would be sent whole to the browser every time. */
    private static final int LIVE_CHARS = 6000;
    /** The page shows the latest thoughts only, each cut to its end. */
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
    // the panel is the run's entry in the panel of the runs: closed while the model works it folds into it, and opens again from it
    private final FloatingWindow floating;

    public ProgressPanel(TrackerRun<P> run, Texts<P> texts) {
        this.run = run;
        this.texts = texts;
        this.statusUpdate = UiAccess.coalesced(this, () -> show(run.getStatus()));
        this.liveUpdate = UiAccess.coalesced(this, this::showLive);
        this.listener = _ -> statusUpdate.run();
        setHeaderTitle(T.localized(PROCESSING_THE_STORY));
        // not modal (the floating window below makes it so): the page stays usable; draggable by the title
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
        liveReasoning.addClassName(StoryTrackerStyles.REASONING);
        liveAnswer.addClassName(StoryTrackerStyles.REASONING);
        liveDetails.setOpened(true);
        liveDetails.setVisible(false);
        content.add(message, bar, liveDetails, logDetails, thoughtDetails);
        content.setPadding(false);
        content.addClassName(StoryTrackerStyles.PROGRESS);
        add(content);

        cancel.addClickListener(_ -> run.cancel());
        review.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        review.addClickListener(_ -> openReview());
        close.addClickListener(_ -> close());
        getFooter().add(cancel, review, close);
        // no Stop of the window: its Cancel and the panel's Stop stop the run, closing the window never does
        floating = new FloatingWindow(this, run.getRegistryRun() == null ? "story-tracker" : run.getRegistryRun().getSource(),
                () -> T.localized(PROCESSING_THE_STORY), null).book(run.getManuscript());
        floating.follow(run.getRegistryRun());
        setResizable(false);
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

    private void show(Status<P> status) {
        Phase phase = status.phase();
        bar.setVisible(!phase.isFinished() && phase != Phase.REVIEW);
        // while the model works a close folds the panel into the panel of the runs
        floating.setRunning(!phase.isFinished() && phase != Phase.REVIEW);
        cancel.setVisible(!phase.isFinished());
        review.setVisible(phase == Phase.REVIEW);
        close.setVisible(true);
        message.setText(switch (phase) {
            case PREPARING -> T.localized(PREPARING);
            case ASKING -> T.localized(ASKING_THE_MODEL_RUN).formatted(status.run());
            case REVIEW -> T.localized(WAITING_FOR_YOUR_REVIEW).formatted(status.proposal() == null ? 0 : status.proposal().changeCount());
            case APPLYING -> T.localized(APPLYING_THE_CHANGES);
            case DONE -> texts.done(run, status);
            case FAILED -> T.localized(IT_FAILED).formatted(status.message());
            case CANCELLED -> texts.stopped(status);
        });
        if (shownLog != status.log().size()) {
            shownLog = status.log().size();
            log.removeAll();
            status.log().forEach(line -> {
                Div row = new Div(new Span(line));
                row.addClassName(StoryTrackerStyles.COUNTS);
                log.add(row);
            });
        }
        logDetails.setVisible(!status.log().isEmpty());

        // what the model thought and answered, one block per request: readable also when it proposed nothing and no review opens
        List<TrackerRun.Thought> thoughts = run.getThoughts();
        // the run keeps only the last 30, so the newest one tells whether the list changed, not only its size
        TrackerRun.Thought newest = thoughts.isEmpty() ? null : thoughts.getLast();
        if (shownThoughts != thoughts.size() || shownLastThought != newest) {
            shownThoughts = thoughts.size();
            shownLastThought = newest;
            thoughtRows.removeAll();
            for (TrackerRun.Thought thought : thoughts.subList(Math.max(0, thoughts.size() - SHOWN_THOUGHTS), thoughts.size())) {
                Div title = new Div(new Span(T.localized(thought.tidy() ? TIDY_REQUEST_N_CHANGES : REQUEST_N_CHANGES)
                        .formatted(thought.run(), thought.changes())));
                title.addClassName(StoryTrackerStyles.NAME);
                Div reasoning = new Div(new Span(thought.reasoning().isBlank() ? T.localized(THE_MODEL_SENT_NO_REASONING)
                        : cut(thought.reasoning(), THOUGHT_CHARS)));
                reasoning.addClassName(StoryTrackerStyles.REASONING);
                Div answer = new Div(new Span(thought.answer() == null ? "" : cut(thought.answer().strip(), THOUGHT_CHARS)));
                answer.addClassName(StoryTrackerStyles.REASONING);
                thoughtRows.add(title, reasoning, new Details(T.localized(WHAT_THE_MODEL_ANSWERED), answer));
            }
        }
        thoughtDetails.setVisible(thoughtRows.getComponentCount() > 0);

        showLive();
    }

    /** What the model writes while it writes it: only while a request goes on. Cheap, called about once a second. */
    private void showLive() {
        String[] live = run.getLive();
        boolean writing = run.getStatus().phase() == Phase.ASKING && !(live[0].isEmpty() && live[1].isEmpty());
        liveDetails.setVisible(writing);
        content.setWidth(writing ? "36em" : null);
        if (writing) {
            liveReasoning.setText(cut(live[0], LIVE_CHARS));
            liveAnswer.setText(cut(live[1], LIVE_CHARS));
            liveAnswer.setVisible(!live[1].isEmpty());
            // the newest words are the ones to see: the boxes keep scrolled to the end
            liveReasoning.getElement().executeJs("this.scrollTop = this.scrollHeight");
            liveAnswer.getElement().executeJs("this.scrollTop = this.scrollHeight");
        }
    }

    private static String cut(String text, int max) {
        return text.length() > max ? "…" + text.substring(text.length() - max) : text;
    }

    /** Opens the review of the proposal that waits; the user pressed the button. */
    public void openReview() {
        Status<P> status = run.getStatus();
        if (status.phase() != Phase.REVIEW || status.proposal() == null) {
            return;
        }
        texts.review(status.run(), status.proposal(), run::apply, run::cancel).open();
    }
}
