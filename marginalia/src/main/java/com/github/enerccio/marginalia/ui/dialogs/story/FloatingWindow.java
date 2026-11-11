package com.github.enerccio.marginalia.ui.dialogs.story;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.PageRuns;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.shared.Registration;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Makes the window of a model run on the story (annotate, redraft) one the author can work beside: on the desktop it is not modal,
 * stands at the right and can be moved and resized; on the phone it stays modal. On both it can be folded into the corner of the page
 * ({@link com.github.enerccio.marginalia.ui.components.RunCorner}) and opened again from there, while the model goes on.
 * <p>
 * The window is in the {@link PageRuns} of its page (the corner) and a run of the {@link Runs} (its parts held in
 * {@link Runs.Mode#STRUCTURE}: the cards show them as busy, a delete is refused) from its opening until it is closed for good. Closing
 * it, and closing the book it was opened from (also while it is folded), stops its run.
 */
final class FloatingWindow {

    /** The source of the runs of these windows. */
    static final String SOURCE = "story-window";

    private final Dialog dialog;
    private final Supplier<String> label;
    private final Runnable stop;
    private final Button minimise;
    // the book window (the dialog that was modal when this one was made), whose closing ends this one
    private final Component owner;

    private Registration ownerGone;
    private PageRuns.Run run;
    private Runs.Run hold;
    // the run of the registry the window is a part of (what follows a generated part), instead of one of its own
    private Runs.Run phase;
    private Manuscript book;
    private List<ChatMessage> parts = List.of();
    private boolean running;
    private boolean minimising;
    private boolean minimised;

    /**
     * @param label what the window does, for the corner and the cards
     * @param stop  stops the model when it is working; called when the window is closed for good
     */
    FloatingWindow(Dialog dialog, Localization loc, Supplier<String> label, Runnable stop) {
        this.dialog = dialog;
        this.label = label;
        this.stop = stop;
        this.owner = bookWindow(UI.getCurrent());

        if (UIUtils.isPhone()) {
            dialog.setModality(ModalityMode.STRICT);
        } else {
            dialog.setModality(ModalityMode.MODELESS);
            dialog.setDraggable(true);
            dialog.setResizable(true);
            dialog.setWidth("min(680px, 95vw)");
            dialog.setHeight("80vh");
            dialog.setTop("64px");
            // at the right, the story stays readable at the left and in the middle
            dialog.setLeft("calc(100vw - min(680px, 95vw) - 24px)");
        }

        minimise = new Button(Solid.CHEVRON_DOWN.create(), _ -> minimise());
        minimise.setThemeName("tertiary icon small");
        minimise.setTooltipText(loc.getValue(L.LABEL_MINIMIZE));
        minimise.setVisible(false);
        dialog.getHeader().add(minimise);

        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                opened();
            } else if (!minimising) {
                end(true);
            }
        });
    }

    /**
     * The modal window the run is opened from (the book), whose closing ends the run: the one Vaadin holds as modal, else the last
     * modal dialog of the page that is open.
     */
    private static Component bookWindow(UI ui) {
        if (ui == null) {
            return null;
        }
        Component modal = ui.getInternals().getActiveModalComponent();
        if (modal != null && modal != ui) {
            return modal;
        }
        return ui.getChildren().filter(Dialog.class::isInstance).map(Dialog.class::cast)
                .filter(dialog -> dialog.isOpened() && dialog.isModal()).reduce((first, second) -> second).orElse(null);
    }

    private void opened() {
        minimised = false;
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        PageRuns runs = PageRuns.of(ui);
        if (run == null) {
            run = runs.add(label, dialog::open, stop, dialog::close);
            run.setRunning(running);
            if (phase != null) {
                // the run it is a part of holds the part and has the Stop
                run.setRegistryRun(phase);
            } else {
                // the panel of another page of the user may stop it: the window is touched in its own page
                hold = Runs.run(SOURCE, label).book(book).page(ui).onStop(() -> inPage(ui, stop)).start();
                hold.holdParts(parts, Runs.Mode.STRUCTURE);
                run.setRegistryRun(hold);
            }
            if (owner != null && ownerGone == null) {
                ownerGone = owner.addDetachListener(_ -> end(true));
            }
        }
        run.setMinimised(false);
        minimise.setVisible(runs.hasCorner());
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
    void minimise() {
        if (!dialog.isOpened()) {
            return;
        }
        minimising = true;
        try {
            dialog.close();
        } finally {
            minimising = false;
        }
        minimised = true;
        if (run != null) {
            run.setMinimised(true);
        }
    }

    /**
     * To call from {@code close()} of the window: a folded window is not open, so closing it fires nothing by itself.
     */
    void closed() {
        if (minimised && !minimising) {
            end(true);
        }
    }

    /**
     * Whether the window is still there, open or folded: what it does at the end of its run (adding the notes, applying the redraft
     * by itself) is done.
     */
    boolean isLive() {
        return dialog.isOpened() || minimised;
    }

    boolean isMinimised() {
        return minimised;
    }

    /**
     * Binds the window's run to this thread while the window starts its request: the panel of the runs shows what the model writes
     * (null when the window has no run yet; try-with-resources takes it).
     */
    Runs.Binding bind() {
        Runs.Run bound = hold != null ? hold : phase;
        return bound == null ? null : bound.bind();
    }

    /**
     * The window is a part of the run (what follows a generated part, which holds the part): it has no run of its own. Before it is
     * opened.
     */
    void partOf(Runs.Run run) {
        this.phase = run;
    }

    void setRunning(boolean value) {
        running = value;
        if (run != null) {
            run.setRunning(value);
        }
    }

    /**
     * The parts the window works on, marked busy on their cards.
     */
    void setParts(Manuscript book, Collection<ChatMessage> parts) {
        this.book = book;
        this.parts = parts.stream().filter(Objects::nonNull).toList();
        if (hold != null) {
            hold.holdParts(this.parts, Runs.Mode.STRUCTURE);
        }
    }

    private void end(boolean stopRun) {
        minimised = false;
        if (stopRun) {
            stop.run();
        }
        if (ownerGone != null) {
            ownerGone.remove();
            ownerGone = null;
        }
        if (run != null) {
            run.remove();
            run = null;
        }
        if (hold != null) {
            hold.end();
            hold = null;
        }
    }

    // for the tests

    Button minimiseButton() {
        return minimise;
    }

    PageRuns.Run runForTest() {
        return run;
    }

    Runs.Run holdForTest() {
        return hold;
    }
}
