package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The windows of model runs that are open in one page (a browser tab): the annotation and the redraft of the story, open or folded
 * into the corner of the page ({@link RunCorner}). What a window holds (its parts, which the cards show as busy) is a run of the
 * {@link Runs}, which the panel lists for every page of the user; this is only what the page itself can do with its windows: open
 * one again, close it. Touched on the thread of the UI only (the windows change it from {@code ui.access}).
 */
public final class PageRuns {

    /**
     * One window of a run.
     */
    public static final class Run {
        private final PageRuns owner;
        private final Supplier<String> label;
        private final Runnable open;
        private final Runnable stop;
        private final Runnable close;
        private boolean running;
        private boolean minimised;
        private Runs.Run registryRun;

        private Run(PageRuns owner, Supplier<String> label, Runnable open, Runnable stop, Runnable close) {
            this.owner = owner;
            this.label = label;
            this.open = open;
            this.stop = stop;
            this.close = close;
        }

        /**
         * What the window does, as its title says it now.
         */
        public String getLabel() {
            return label.get();
        }

        /**
         * Whether the model is working for the window now.
         */
        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            if (this.running != running) {
                this.running = running;
                owner.changed();
            }
        }

        /**
         * Whether the window is folded into the corner of the page.
         */
        public boolean isMinimised() {
            return minimised;
        }

        public void setMinimised(boolean minimised) {
            if (this.minimised != minimised) {
                this.minimised = minimised;
                owner.changed();
            }
        }

        /**
         * The run of the {@link Runs} the window holds its parts with, null when it has none (yet): the corner shows the window as
         * that run's entry.
         */
        public Runs.Run getRegistryRun() {
            return registryRun;
        }

        public void setRegistryRun(Runs.Run run) {
            this.registryRun = run;
            owner.changed();
        }

        /**
         * Shows the window again.
         */
        public void open() {
            open.run();
        }

        /**
         * Stops the model; the window stays with what arrived.
         */
        public void stop() {
            stop.run();
        }

        /**
         * Closes the window, which ends its run.
         */
        public void close() {
            close.run();
        }

        /**
         * The window is gone.
         */
        public void remove() {
            if (owner.runs.remove(this)) {
                owner.changed();
            }
        }
    }

    private final List<Run> runs = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private int corners;

    private PageRuns() {
    }

    /**
     * The windows of the page, created when it is asked for the first time.
     */
    public static PageRuns of(UI ui) {
        PageRuns runs = ComponentUtil.getData(ui, PageRuns.class);
        if (runs == null) {
            runs = new PageRuns();
            ComponentUtil.setData(ui, PageRuns.class, runs);
        }
        return runs;
    }

    /**
     * Adds a window that was opened.
     *
     * @param label what the window does, read each time it is shown
     * @param open  shows the window again
     * @param stop  stops the model, the window stays
     * @param close closes the window
     */
    public Run add(Supplier<String> label, Runnable open, Runnable stop, Runnable close) {
        Run run = new Run(this, label, open, stop, close);
        runs.add(run);
        changed();
        return run;
    }

    /**
     * The windows, oldest first.
     */
    public List<Run> getRuns() {
        return List.copyOf(runs);
    }

    /**
     * Called on the thread of the UI after every change.
     */
    public Registration addListener(Runnable listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    /**
     * Whether the page has a corner a window can be folded into (the shell has one, a window opened elsewhere, as in the tests, not).
     */
    public boolean hasCorner() {
        return corners > 0;
    }

    void cornerAttached(boolean attached) {
        corners = Math.max(0, corners + (attached ? 1 : -1));
    }

    private void changed() {
        for (Runnable listener : List.copyOf(listeners)) {
            listener.run();
        }
    }
}
