package com.github.enerccio.marginalia.ui.widgets;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.PageRuns;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.function.Supplier;

/**
 * Makes the window of a model run (annotate, redraft, a summary, the generators of the plugins) one the author can work beside: on
 * the desktop it is not modal, stands at the right and can be moved and resized; on the phone it stays modal. On both it can be
 * folded into the corner of the page ({@link com.github.enerccio.marginalia.ui.components.RunCorner}) and opened again from there,
 * while the model goes on.
 * <p>
 * <b>Close and Stop are two things.</b> The window's own Stop stops the model and the window stays with what arrived. Closing the
 * window while the model works ({@link #setRunning}) folds it into the corner instead: the run goes on in the queue of the model and
 * in the panel of the runs, where the window opens again (its answer is there when the model is done) or is stopped and closed. Closing
 * it when the model does not work ends it, and so does closing the book window it was opened from or the tab of its page (also
 * while it is folded), which stops the model. A page without a corner (no shell, as in the tests) has nowhere to fold to: there closing stops as before.
 * <p>
 * The window is in the {@link PageRuns} of its page (the corner) and, while the model works, a run of the {@link Runs}: its place in
 * the queue, what the model writes and a Stop in the panel of every page of the user. A window that holds parts ({@link #setParts},
 * {@link #holdParts}) has that run from its opening until it is closed for good, so the parts are held all the time it is there.
 * <pre>{@code
 * private final FloatingWindow floating = new FloatingWindow(this, "my-plugin", () -> T.localized(GENERATING), this::stop)
 *         .book(book);
 *
 * // when the request starts, on the thread of the page:
 * floating.setRunning(true);
 * try (Runs.Binding _ = floating.bind()) {
 *     token = service.generate(book, callbacks);        // the requests made here, and from their callbacks, report to the run
 * }
 * // in the end callbacks, in ui.access: floating.setRunning(false);
 * // a window that closes itself (at the end of its work, also when it is folded): floating.closeForGood();
 * }</pre>
 * A dialog subclass that overrides {@code close()} to do more calls {@link #closed()} after {@code super.close()}.
 */
@Configurable(preConstruction = true)
public final class FloatingWindow {

    /** The source of the runs of the story's windows (annotate, redraft). */
    public static final String SOURCE = "story-window";

    @Autowired
    private Localization loc;

    private final Dialog dialog;
    private final String source;
    private final Supplier<String> label;
    private final Runnable stop;
    private final boolean stoppable;
    private final Button minimise;
    // the book window (the dialog that was modal when this one was made), whose closing ends this one
    private final Component owner;

    private Registration ownerGone;
    // the page itself: a window folded in a tab that is closed (or whose heartbeats stop) ends with it, also without a book window
    private Registration pageGone;
    private PageRuns.Run run;
    private Runs.Run hold;
    // the run of the registry the window is a part of (what follows a generated part), instead of one of its own
    private Runs.Run phase;
    private Manuscript book;
    private List<ChatMessage> parts = List.of();
    private Runs.Mode mode = Runs.Mode.STRUCTURE;
    private boolean holdsParts;
    private boolean running;
    private boolean minimising;
    private boolean minimised;
    // the last close folded the window because the model works: closed() must not end it
    private boolean foldedByClose;
    private boolean forGood;
    private boolean placed;
    // the left edge the window was given (null when it has none): one the user dragged it away from is not touched again
    private String placedLeft;
    private UI page;
    private final List<Runnable> endListeners = new ArrayList<>();

    /**
     * A window of the story (annotate, redraft): source {@link #SOURCE}.
     *
     * @param label what the window does, for the corner and the cards
     * @param stop  stops the model when it is working; called by the window's Stop in the panel and when the window is closed for good
     */
    public FloatingWindow(Dialog dialog, Supplier<String> label, Runnable stop) {
        this(dialog, SOURCE, label, stop);
    }

    /**
     * @param source what the run is (the id of the panel and of {@code /jobs})
     * @param label  what the window does, read each time the corner shows it
     * @param stop   stops the model when it is working (the window stays); null when the request can not be stopped
     */
    public FloatingWindow(Dialog dialog, String source, Supplier<String> label, Runnable stop) {
        this.dialog = dialog;
        this.source = source;
        this.label = label;
        this.stop = stop == null ? () -> { } : stop;
        this.stoppable = stop != null;
        this.owner = bookWindow(UI.getCurrent());

        if (UIUtils.isPhone()) {
            dialog.setModality(ModalityMode.STRICT);
        } else {
            dialog.setModality(ModalityMode.MODELESS);
            dialog.setDraggable(true);
            dialog.setResizable(true);
        }

        minimise = new Button(Solid.CHEVRON_DOWN.create(), _ -> minimise());
        minimise.setThemeName("tertiary icon small");
        if (loc != null) {
            // none when the window is made outside the application (the tests of a plugin)
            minimise.setTooltipText(loc.getValue(L.LABEL_MINIMIZE));
        }
        minimise.setVisible(false);
        dialog.getHeader().add(minimise);

        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                opened();
            } else if (minimising) {
                // folded by its button
            } else if (!forGood && running && canFold()) {
                // closed while the model works: it goes on, the window waits in the corner
                fold();
                foldedByClose = true;
            } else {
                end(true);
            }
        });
        // opened over another modal (the summaries overview), Vaadin attaches the window to that one, and when it closes it takes the
        // window out of the page while the window is still open: nothing would show it again. It is closed as by its Close (folded
        // while the model works); a page that is going away ends it by itself.
        dialog.addDetachListener(event -> {
            // not one that has ended already (its book window closed and ended it first)
            if (!forGood && run != null && dialog.isOpened() && !event.getUI().isClosing()) {
                dialog.close();
            }
        });
    }

    /**
     * The size of the window on the desktop (it stands at the right, the story stays readable at the left and in the middle). The
     * window keeps its own size otherwise.
     */
    public FloatingWindow size(String width, String height) {
        if (!UIUtils.isPhone()) {
            dialog.setWidth(width);
            dialog.setHeight(height);
            // a window that grows after it was placed (a form that turns into the progress) would stand out of the page at the right
            if (placed && placedLeft != null && placedLeft.equals(dialog.getLeft())) {
                place();
            }
        }
        return this;
    }

    /**
     * At the right, never out of the page at the left; below the top bar, higher up for a tall window (a window of 94vh would stand
     * out of the page at the bottom, with its buttons).
     */
    private void place() {
        String height = dialog.getHeight();
        // a height that is not a length ("auto") would make the calc invalid
        dialog.setTop(height == null || !height.matches(".*\\d.*") ? "64px" : "max(0px, min(64px, calc(100vh - " + height + " - 16px)))");
        String width = dialog.getWidth();
        if (width != null) {
            placedLeft = "max(0px, calc(100vw - " + width + " - 24px))";
            dialog.setLeft(placedLeft);
        }
    }

    /**
     * The book the window works on, shown in the panel of the runs.
     */
    public FloatingWindow book(Manuscript book) {
        this.book = book;
        return this;
    }

    /**
     * The modal window the run is opened from (the book), whose closing ends the run: the one Vaadin holds as modal, else the last
     * modal dialog of the page that is open.
     */
    private static Component bookWindow(UI ui) {
        if (ui == null) {
            return null;
        }
        // only the window of a book: another modal on top of it (the summaries overview the window was opened from) closes while the
        // window goes on, and would end it with itself
        return ui.getChildren().filter(ManuscriptWindow.class::isInstance).map(Dialog.class::cast)
                .filter(Dialog::isOpened).reduce((first, second) -> second).orElse(null);
    }

    private void opened() {
        minimised = false;
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        page = ui;
        if (!placed && !UIUtils.isPhone()) {
            placed = true;
            place();
        }
        PageRuns runs = PageRuns.of(ui);
        if (run == null) {
            run = runs.add(label, dialog::open, stop, this::closeForGood);
            run.setRunning(running);
            if (phase != null) {
                // the run it is a part of holds the part and has the Stop
                run.setRegistryRun(phase);
            } else if (hold != null) {
                // started by a request sent before the window was opened
                run.setRegistryRun(hold);
            } else if (holdsParts || running) {
                startHold();
            }
            if (owner != null && ownerGone == null) {
                ownerGone = owner.addDetachListener(_ -> end(true));
            }
            if (pageGone == null) {
                pageGone = ui.addDetachListener(_ -> end(true));
            }
        }
        run.setMinimised(false);
        minimise.setVisible(runs.hasCorner());
    }

    private boolean canFold() {
        UI ui = page != null ? page : UI.getCurrent();
        return ui != null && PageRuns.of(ui).hasCorner();
    }

    /** The run of the registry: the panel of another page of the user may stop it, the window is touched in its own page. */
    private void startHold() {
        if (hold != null || phase != null || page == null) {
            return;
        }
        UI ui = page;
        Runs.Builder builder = Runs.run(source, label).book(book).page(ui);
        if (stoppable) {
            builder.onStop(() -> inPage(ui, stop));
        }
        if (!holdsParts || mode == Runs.Mode.READ) {
            // only a window that may change its parts marks them busy on their cards
            builder.quiet();
        }
        hold = builder.start();
        if (holdsParts) {
            hold.holdParts(parts, mode);
        }
        if (run != null) {
            run.setRegistryRun(hold);
        }
    }

    private void endHold() {
        if (hold != null) {
            hold.end();
            hold = null;
            if (run != null) {
                run.setRegistryRun(null);
            }
        }
    }

    private static void inPage(UI ui, Runnable work) {
        if (UI.getCurrent() == ui || ui.getSession() == null || ui.getSession().hasLock()) {
            work.run();
            return;
        }
        try {
            ui.access(() -> {
                work.run();
                UIPushGuard.push(ui);
            });
        } catch (UIDetachedException e) {
            // the page is gone, and its windows with it
        }
    }

    /**
     * Folds the window into the corner; the run goes on.
     */
    public void minimise() {
        if (!dialog.isOpened()) {
            return;
        }
        minimising = true;
        try {
            dialog.close();
        } finally {
            minimising = false;
        }
        fold();
    }

    private void fold() {
        minimised = true;
        if (run != null) {
            run.setMinimised(true);
        }
    }

    /**
     * To call from {@code close()} of the window after {@code super.close()}: a folded window is not open, so closing it fires nothing
     * by itself; this ends it then. A close that just folded the window (the model works) leaves it folded.
     */
    public void closed() {
        if (foldedByClose) {
            foldedByClose = false;
            return;
        }
        if (minimised && !minimising) {
            end(true);
        }
    }

    /**
     * Closes the window and ends its run, also while the model works (which stops it) and while the window is folded: the Close of
     * the panel, and a window that closes itself.
     */
    public void closeForGood() {
        forGood = true;
        try {
            dialog.close();
        } finally {
            forGood = false;
        }
        foldedByClose = false;
        if (run != null || hold != null || minimised) {
            end(true);
        }
    }

    /**
     * Whether the window is still there, open or folded: what it does at the end of its run (adding the notes, applying the redraft
     * by itself) is done.
     */
    public boolean isLive() {
        return dialog.isOpened() || minimised;
    }

    public boolean isMinimised() {
        return minimised;
    }

    /**
     * Binds the window's run to this thread while the window starts its request: the panel of the runs shows what the model writes
     * and its place in the queue (null when the window is not open; try-with-resources takes it). Starts the run when the window had
     * none.
     */
    public Runs.Binding bind() {
        if (hold == null && phase == null) {
            if (page == null) {
                // a request sent before the window is opened
                page = UI.getCurrent();
            }
            startHold();
        }
        Runs.Run bound = hold != null ? hold : phase;
        return bound == null ? null : bound.bind();
    }

    /**
     * Sends a request with the window's run bound ({@link #bind()}), for a window that keeps the binding out of its own method.
     *
     * @return what the request returned (what stops it)
     */
    public <T> T send(WindowRun.Request<T> request) throws Exception {
        try (Runs.Binding _ = bind()) {
            return request.send();
        }
    }

    /**
     * The run of the registry the window's request reports to, for a worker thread the window starts ({@link Runs.Run#bind()} there):
     * started when there was none; null when the window has none (not opened, no page).
     */
    public Runs.Run getRun() {
        if (hold == null && phase == null) {
            if (page == null) {
                page = UI.getCurrent();
            }
            startHold();
        }
        return hold != null ? hold : phase;
    }

    /**
     * The window is a part of the run (what follows a generated part, which holds the part): it has no run of its own. Before it is
     * opened.
     */
    public void partOf(Runs.Run run) {
        this.phase = run;
    }

    /**
     * The work of the window is a run of the registry a service started (a batch of summaries): the corner shows the window as that
     * run's entry, and the window starts no run of its own.
     */
    public void follow(Runs.Run serviceRun) {
        if (serviceRun == null) {
            return;
        }
        endHold();
        phase = serviceRun;
        if (run != null) {
            run.setRegistryRun(serviceRun);
        }
    }

    /**
     * As {@link #follow(Runs.Run)}, with the newest run of the book with that source the service started on this page; on the thread
     * of the page, right after the service was called.
     */
    public void follow(String serviceSource) {
        UI ui = page != null ? page : UI.getCurrent();
        if (book == null || ui == null) {
            return;
        }
        Runs.ofBook(book.getId()).stream()
                .filter(started -> serviceSource.equals(started.getSource()) && started.getPage() == ui && !started.isEnded())
                .max(Comparator.comparingLong(Runs.Run::getId))
                .ifPresent(this::follow);
    }

    /**
     * Whether the model works for the window now: while it does, closing the window folds it, and the run is in the panel.
     */
    public void setRunning(boolean value) {
        running = value;
        if (run != null) {
            run.setRunning(value);
            if (value) {
                startHold();
            } else if (!holdsParts) {
                endHold();
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * The parts the window works on, marked busy on their cards and held {@link Runs.Mode#STRUCTURE} (a delete or a merge of them is
     * refused) while the window is there.
     */
    public void setParts(Manuscript book, Collection<ChatMessage> parts) {
        holdParts(book, parts, Runs.Mode.STRUCTURE);
    }

    /**
     * The parts the window works on, held in the given mode while the window is there (for a window that only reads them:
     * {@link Runs.Mode#READ}).
     */
    public void holdParts(Manuscript book, Collection<ChatMessage> parts, Runs.Mode mode) {
        this.book = book;
        this.parts = parts.stream().filter(Objects::nonNull).toList();
        this.mode = mode;
        this.holdsParts = true;
        if (hold != null) {
            hold.holdParts(this.parts, mode);
        } else if (run != null) {
            startHold();
        }
    }

    /**
     * Runs when the window ends: closed for good (also from the corner, or with its book), not when it is folded.
     */
    public Registration addEndListener(Runnable listener) {
        endListeners.add(listener);
        return () -> endListeners.remove(listener);
    }

    private void end(boolean stopRun) {
        boolean wasThere = run != null || minimised;
        minimised = false;
        if (stopRun) {
            stop.run();
        }
        if (ownerGone != null) {
            ownerGone.remove();
            ownerGone = null;
        }
        if (pageGone != null) {
            pageGone.remove();
            pageGone = null;
        }
        if (run != null) {
            run.remove();
            run = null;
        }
        if (hold != null) {
            hold.end();
            hold = null;
        }
        if (wasThere) {
            for (Runnable listener : List.copyOf(endListeners)) {
                listener.run();
            }
        }
    }

    // for the tests

    public Button minimiseButton() {
        return minimise;
    }

    public PageRuns.Run runForTest() {
        return run;
    }

    public Runs.Run holdForTest() {
        return hold;
    }
}
