package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentLockCheck;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.dialog.Dialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

/**
 * A book is open for editing in one window only, for its user across all their pages (tabs, browsers, the phone). The window of a book
 * holds it in {@link Runs.Mode#OPEN} from its opening to its closing ({@link #hold}); a second page that opens the book is asked whether
 * to open it there and close it in the first one ({@link #open}). Restoring the last open book after a login never takes it over.
 * <p>
 * The window's hold also keeps a delete, a restore or an import of the book from other pages away while it is open. When the window leaves
 * its page, a generation of the book the page started stops ({@link #stopGenerationOf}).
 */
@Configurable(preConstruction = true)
public class BookWindows {

    private static final DateTimeFormatter SINCE = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private Localization loc;

    /**
     * Opens the book unless an agent works on it or it is open in another page; in that case asks to take it over.
     *
     * @param open      opens the window here
     * @param restoring the book is opened again by itself (the restored workspace): it is left closed and the author is told where it is
     */
    public void open(Manuscript book, Runnable open, boolean restoring) {
        if (new AgentLockCheck().refuses(book)) {
            return;
        }
        Runs.Run other = Runs.holder(book, Runs.Operation.OPEN);
        if (other == null) {
            open.run();
            return;
        }
        if (restoring) {
            Notification.warning(loc.getValue(L.MSG_BOOK_OPEN_ELSEWHERE_NOT_RESTORED));
            return;
        }
        List<Runs.Run> generations = Runs.ofBook(book.getId()).stream().filter(run -> Runs.GENERATION.equals(run.getSource())).toList();
        String since = SINCE.format(other.getStarted().atZone(ZoneId.systemDefault()));
        String message = String.format(loc.getValue(generations.isEmpty() ? L.MSG_BOOK_OPEN_ELSEWHERE : L.MSG_BOOK_OPEN_ELSEWHERE_GENERATING), since);
        ConfirmDialog.show(message, loc.getValue(L.LABEL_OPEN_HERE), loc.getValue(L.LABEL_CANCEL), () -> {
            // the Stop the author knows: what arrived is kept
            generations.forEach(Runs.Run::stop);
            other.stop();
            open.run();
        }, () -> {
        }, false);
    }

    /**
     * Makes the window hold its book while it is open. Call it from the constructor of the window.
     *
     * @param book      the book of the window, read when it opens
     * @param closeHere closes the window as its exit button does; run in the window's page when the book is opened in another one
     */
    public static void hold(Dialog window, Supplier<Manuscript> book, Runnable closeHere) {
        Holder holder = new Holder(window, book, closeHere);
        window.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                holder.take();
            } else {
                holder.release();
            }
        });
        window.addDetachListener(event -> {
            holder.release();
            stopGenerationOf(book.get(), event.getUI());
        });
    }

    /**
     * The window left its page (closed, or the page is gone: a closed tab, a page whose heartbeats stopped): a generation of the book that
     * this page started stops, through the normal Stop (what arrived is kept, the cleanup runs, the connection of the model is given
     * back). Nobody could watch or stop it any more.
     */
    static void stopGenerationOf(Manuscript book, UI page) {
        if (book == null || book.getId() == null || page == null) {
            return;
        }
        Runs.Run generation = Runs.generationOf(book.getId());
        if (generation != null && generation.getPage() == page) {
            generation.stop();
        }
    }

    @Configurable(preConstruction = true)
    static final class Holder {

        @Autowired
        private Localization loc;

        private final Dialog window;
        private final Supplier<Manuscript> book;
        private final Runnable closeHere;
        private Runs.Run run;

        Holder(Dialog window, Supplier<Manuscript> book, Runnable closeHere) {
            this.window = window;
            this.book = book;
            this.closeHere = closeHere;
        }

        void take() {
            Manuscript manuscript = book.get();
            if (run != null || manuscript == null || manuscript.getId() == null) {
                return;
            }
            UI ui = window.getUI().orElse(UI.getCurrent());
            Localization localization = loc;
            run = Runs.run(Runs.BOOK_WINDOW, () -> localization.getValue(L.LABEL_BOOK_OPEN_ELSEWHERE))
                    .book(manuscript).hold(manuscript, Runs.Mode.OPEN).page(ui).quiet()
                    .onStop(() -> closeThere(ui))
                    .start();
        }

        void release() {
            if (run != null) {
                run.end();
                run = null;
            }
        }

        /**
         * The book is opened in another page: the hold goes at once (a page that does not answer cannot write), the window closes in its
         * own page.
         */
        private void closeThere(UI ui) {
            release();
            if (ui == null) {
                return;
            }
            try {
                ui.access(() -> {
                    if (window.isOpened()) {
                        closeHere.run();
                        Notification.warning(loc.getValue(L.MSG_BOOK_CLOSED_ELSEWHERE));
                    }
                    UIPushGuard.push(ui);
                });
            } catch (UIDetachedException e) {
                // the page is gone, and its window with it
            }
        }
    }
}
