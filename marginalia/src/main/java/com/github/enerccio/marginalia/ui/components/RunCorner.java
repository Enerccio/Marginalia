package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.jobs.JobFormat;
import com.github.enerccio.marginalia.ui.widgets.DesktopNotifier;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.dom.ElementUtil;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * The panel of the runs (bottom left of every page, above the book window as the help assistant is): every run of the user that works
 * on a book now, from any of the user's pages, a command or a plugin: what it does and in which book, whether it waits for the model
 * (and how many requests are before it), the end of what the model writes, and <b>Stop</b>. The windows of this page that were folded
 * into it (annotate, redraft) have <b>Open</b> and, once the model is done, <b>Close</b>.
 * <p>
 * Collapsed it is a chip: a bar while something runs, the first run and how many more; a click opens the list. Hidden when the user has
 * no run. On the desktop it can be moved by its grip, and stays where the author left it (in the browser). When a run whose page is
 * gone ends, the panel of one live page of the user sends its notification.
 */
@Configurable(preConstruction = true)
public class RunCorner extends Div {

    /** The end of the model's text an entry shows. */
    static final int SHOWN_TEXT = 600;

    @Autowired
    private Localization loc;

    @Autowired
    private User user;

    private final Div panel = new Div();
    private final Div chip = new Div();
    private final Span chipText = new Span();
    private final ProgressBar chipBar = new ProgressBar();
    private final Icon grip = Solid.GRIP_VERTICAL.create();
    private final Button toggle = new Button();
    private final Div list = new Div();
    private final Map<Object, Entry> entries = new LinkedHashMap<>();
    private final List<Registration> registrations = new ArrayList<>();
    private UI ui;
    // the user of this page, taken when it is attached (the refreshes run with its request scope, see RunsOnPage)
    private Long userId;
    private PageRuns runs;
    private boolean expanded;
    private int folded;
    // how many runs waited for the author at the last refresh
    private int waiting;

    public RunCorner() {
        addClassName(CssStyles.RUN_CORNER_ROOT);
        panel.addClassName(CssStyles.RUN_CORNER);
        panel.setVisible(false);

        grip.addClassName(CssStyles.RUN_CORNER_GRIP);
        chipBar.setIndeterminate(true);
        chipBar.addClassName(CssStyles.RUN_CORNER_BAR);
        chipText.addClassName(CssStyles.RUN_CORNER_LABEL);
        // the click on the button reaches the chip, which opens and closes the list
        toggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        chip.addClassName(CssStyles.RUN_CORNER_CHIP);
        chip.add(grip, chipText, toggle);
        chip.addClickListener(_ -> setExpanded(!expanded));

        list.addClassName(CssStyles.RUN_CORNER_LIST);
        panel.add(chip, chipBar, list);
        add(panel);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ui = attachEvent.getUI();
        userId = user == null ? null : user.getId();
        runs = PageRuns.of(ui);
        runs.cornerAttached(true);
        registrations.add(runs.addListener(this::refresh));
        registrations.add(RunsOnPage.listen(ui, this::refresh));
        registrations.add(RunsOnPage.listenText(ui, this::refreshText));
        registrations.add(RunsOnPage.listenEnd(ui, this::ended));
        refresh();
        // a book is a strict dialog: the clicks would be dropped on the server and the panel would be under it in the browser
        ElementUtil.setIgnoreParentInert(getElement(), true);
        JavascriptLibrary.keepOnTopOfDialogs(getElement());
        if (!UIUtils.isPhone()) {
            JavascriptLibrary.makeDraggable(panel.getElement(), grip.getElement(), "marginalia.runCorner");
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registrations.forEach(Registration::remove);
        registrations.clear();
        if (runs != null) {
            runs.cornerAttached(false);
            runs = null;
        }
        ui = null;
    }

    private void setExpanded(boolean value) {
        expanded = value;
        refresh();
    }

    /**
     * What the panel shows: the runs of the user and the windows of this page, a window together with the run it holds its parts
     * with.
     */
    private List<Item> items() {
        List<Item> items = new ArrayList<>();
        List<PageRuns.Run> windows = runs == null ? List.of() : runs.getRuns();
        for (Runs.Run run : Runs.all()) {
            // a run started by a step of another (a translation after a generated part) is in that one's entry
            if (Runs.BOOK_WINDOW.equals(run.getSource()) || run.isEnded() || run.getParent() != null) {
                continue;
            }
            boolean mine = userId != null && userId.equals(run.getOwnerId()) || ui != null && run.getPage() == ui;
            if (!mine) {
                continue;
            }
            PageRuns.Run window = windows.stream().filter(w -> w.getRegistryRun() == run).findFirst().orElse(null);
            items.add(new Item(run, window));
        }
        for (PageRuns.Run window : windows) {
            boolean listed = items.stream().anyMatch(item -> item.window() == window);
            if (!listed && window.isMinimised()) {
                items.add(new Item(null, window));
            }
        }
        return items;
    }

    private void refresh() {
        List<Item> items = items();
        int nowFolded = (int) items.stream().filter(item -> item.window() != null && item.window().isMinimised()).count();
        if (nowFolded > folded) {
            // a window was just folded into the panel: show it
            expanded = true;
        }
        folded = nowFolded;
        int nowWaiting = (int) items.stream().filter(item -> item.run() != null && item.run().isWaitingForUser()).count();
        if (nowWaiting > waiting) {
            // a run waits for the author's decision: show it
            expanded = true;
        }
        waiting = nowWaiting;

        list.removeAll();
        entries.clear();
        panel.setVisible(!items.isEmpty());
        if (items.isEmpty()) {
            return;
        }
        boolean anyRunning = items.stream().anyMatch(Item::running);
        chipBar.setVisible(anyRunning && !expanded);
        Item first = items.stream().filter(Item::running).findFirst().orElse(items.getFirst());
        String text = first.label();
        if (items.size() > 1) {
            text += " · " + String.format(loc.getValue(L.MSG_RUN_MORE), items.size() - 1);
        }
        chipText.setText(text);
        toggle.setIcon((expanded ? Solid.CHEVRON_DOWN : Solid.CHEVRON_UP).create());
        toggle.setTooltipText(loc.getValue(expanded ? L.LABEL_MINIMIZE : L.LABEL_AGENT_RUNS));
        list.setVisible(expanded);
        if (!expanded) {
            return;
        }
        for (Item item : items) {
            Entry entry = new Entry(item);
            entries.put(item.key(), entry);
            list.add(entry.root);
        }
    }

    /** The model wrote: only the status and the text of the entries change. */
    private void refreshText() {
        for (Entry entry : entries.values()) {
            entry.update();
        }
    }

    /**
     * A run ended: when the page that started it is gone, the page of the user that claims it first says so (the page itself says it
     * as before).
     */
    private void ended(Runs.Run run) {
        if (ui == null || run.getNotice() == null || userId == null || !Objects.equals(userId, run.getOwnerId())) {
            return;
        }
        if (run.getOutcome() == null || run.getOutcome() == Runs.Outcome.STOPPED || !pageGone(run) || !run.claimNotice()) {
            return;
        }
        Runs.Notice notice = run.getNotice();
        DesktopNotifier notifier = new DesktopNotifier(ui);
        if (run.getOutcome() == Runs.Outcome.FINISHED) {
            notifier.finished(notice.source(), notice.automatic(), notice.detail());
        } else {
            notifier.failed(notice.source(), notice.automatic(), notice.detail());
        }
    }

    private static boolean pageGone(Runs.Run run) {
        Object page = run.getPage();
        // a closed tab: its page is closing, or out of its session already
        return !(page instanceof UI started) || started.isClosing() || started.getSession() == null;
    }

    /**
     * A run of the user, a window of this page, or both.
     */
    private record Item(Runs.Run run, PageRuns.Run window) {

        Object key() {
            return run != null ? run : window;
        }

        String label() {
            return window != null ? window.getLabel() : run.getLabel();
        }

        boolean running() {
            if (window != null) {
                return window.isRunning();
            }
            return run != null && !run.isEnded() && !run.isWaitingForUser();
        }
    }

    /**
     * The entry of one item: what it does, where, how far, the end of the model's text and its buttons.
     */
    private final class Entry {
        private final Item item;
        private final Div root = new Div();
        private final Span status = new Span();
        private final Div text = new Div();

        Entry(Item item) {
            this.item = item;
            root.addClassName(CssStyles.RUN_CORNER_ENTRY);
            // what the entry says and its progress: a click opens its window again (the buttons are apart)
            Div body = new Div();
            body.addClassName(CssStyles.RUN_CORNER_BODY);
            Span label = new Span(item.label());
            label.addClassName(CssStyles.RUN_CORNER_LABEL);
            body.add(label);
            Runs.Run run = item.run();
            if (run != null && run.getBookName() != null && !item.label().contains(run.getBookName())) {
                Span book = new Span(run.getBookName());
                book.addClassName(CssStyles.RUN_CORNER_STATUS);
                body.add(book);
            }
            status.addClassName(CssStyles.RUN_CORNER_STATUS);
            text.addClassName(CssStyles.RUN_CORNER_TEXT);
            body.add(status, text);
            if (item.running()) {
                ProgressBar bar = new ProgressBar();
                bar.setIndeterminate(true);
                body.add(bar);
            }
            Runnable open = opener();
            if (open != null) {
                body.addClassName(CssStyles.CURSOR_POINTER);
                body.addClickListener(_ -> open.run());
            }
            root.add(body, buttons());
            update();
        }

        /**
         * What a click on the entry opens: the folded window of this page, or the result of a run that waits for the author; null
         * when there is nothing to open.
         */
        private Runnable opener() {
            PageRuns.Run window = item.window();
            Runs.Run run = item.run();
            if (window != null && window.isMinimised()) {
                return window::open;
            }
            if (window == null && run != null && run.getOnOpen() != null) {
                return () -> {
                    Runnable open = run.getOnOpen();
                    if (open != null) {
                        open.run();
                    }
                };
            }
            return null;
        }

        private Div buttons() {
            Div buttons = new Div();
            buttons.addClassName(CssStyles.RUN_CORNER_BUTTONS);
            PageRuns.Run window = item.window();
            Runs.Run run = item.run();
            if (window != null && window.isMinimised()) {
                Button open = new Button(loc.getValue(L.LABEL_OPEN), _ -> window.open());
                open.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                buttons.add(open);
            } else if (window == null && run != null && run.getOnOpen() != null) {
                // a result waits for the author (Review), or the run can be shown (Open): it opens in this page
                Button review = new Button(loc.getValue(run.isWaitingForUser() ? L.LABEL_REVIEW : L.LABEL_OPEN), _ -> {
                    Runnable open = run.getOnOpen();
                    if (open != null) {
                        open.run();
                    }
                });
                review.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                buttons.add(review);
            }
            if (item.running() || run != null && run.isWaitingForUser()) {
                boolean stoppable = window != null || run.canStop();
                if (stoppable) {
                    Button stop = new Button(loc.getValue(L.LABEL_STOP), _ -> {
                        // the run of the registry first: a window that is a part of a bigger run stops all of it
                        if (run != null && run.canStop()) {
                            run.stop();
                        } else if (window != null) {
                            window.stop();
                        }
                    });
                    stop.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                    stop.setEnabled(run == null || !run.isStopping());
                    buttons.add(stop);
                }
            } else if (window != null) {
                Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> window.close());
                close.addThemeVariants(ButtonVariant.LUMO_SMALL);
                buttons.add(close);
            }
            return buttons;
        }

        void update() {
            Runs.Run run = item.run();
            String shown = "";
            String state;
            if (run == null) {
                state = loc.getValue(item.running() ? L.MSG_RUN_WORKING : L.MSG_RUN_WAITS_FOR_YOU);
            } else {
                Runs.Text written = run.getText();
                String answer = written.getAnswer();
                String reasoning = written.getReasoning();
                if (run.isStopping()) {
                    state = loc.getValue(L.MSG_RUN_STOPPING);
                } else if (run.isWaitingForUser() || item.window() != null && !item.window().isRunning()) {
                    state = loc.getValue(L.MSG_RUN_WAITS_FOR_YOU);
                } else if (written.getWaiting() > 0) {
                    state = String.format(loc.getValue(L.MSG_WAITING_FOR_MODEL_AHEAD), written.getWaiting());
                } else if (!answer.isEmpty()) {
                    state = loc.getValue(L.MSG_RUN_WRITING);
                } else if (!reasoning.isEmpty()) {
                    state = loc.getValue(L.MSG_RUN_THINKING);
                } else {
                    state = loc.getValue(L.MSG_RUN_WORKING);
                }
                shown = tail(answer.isEmpty() ? reasoning : answer);
                state += " · " + JobFormat.duration(run.getStarted(), null);
            }
            status.setText(state);
            text.setText(shown);
            text.setVisible(!shown.isEmpty());
            if (!shown.isEmpty()) {
                JavascriptLibrary.scrollToBottom(text.getElement());
            }
        }
    }

    static String tail(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.strip();
        return trimmed.length() <= SHOWN_TEXT ? trimmed : "…" + trimmed.substring(trimmed.length() - SHOWN_TEXT);
    }

    // for the tests

    public Div panelForTest() {
        return panel;
    }

    Div chipForTest() {
        return chip;
    }

    boolean isExpandedForTest() {
        return expanded;
    }

    void setExpandedForTest(boolean value) {
        setExpanded(value);
    }

    /** What the listeners do in a page with a session (a test page has none). */
    void refreshForTest() {
        refresh();
    }

    void endedForTest(Runs.Run run) {
        ended(run);
    }
}
