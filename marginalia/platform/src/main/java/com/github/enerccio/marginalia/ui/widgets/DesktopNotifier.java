package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.NotificationService.Notice;
import com.github.enerccio.marginalia.domain.service.NotificationService.Outcome;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.vaadin.flow.component.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Tells the user, with a desktop notification of the browser, that a run of a model is over. The code that ends the run (a dialog, a
 * listener of the generation, an extension) calls {@link #finished} or {@link #failed} with the id of its
 * {@link NotificationService.Source source}; whether anything is shown is up to the user's settings (the Notifications tab), and the
 * browser shows it only while the user is not looking at the page.
 * <p>
 * Call it where the UI can be changed (inside {@code ui.access}), the settings of the current user are read. It never throws: a
 * notification is not worth breaking the end of a run.
 */
@Configurable(preConstruction = true)
public class DesktopNotifier {

    private static final Logger log = LoggerFactory.getLogger(DesktopNotifier.class);

    @Autowired
    private NotificationService notificationService;

    private final UI ui;

    /**
     * @param ui the page of the user who started the run
     */
    public DesktopNotifier(UI ui) {
        this.ui = ui;
    }

    /**
     * The run of the source ended well.
     *
     * @param source    the id of the source, see {@link NotificationService.Source}
     * @param automatic whether the run started by itself and not by the user (the automatic annotation of a generated part)
     * @param detail    what it was about (the title of the book), null for nothing
     */
    public void finished(String source, boolean automatic, String detail) {
        notify(source, automatic, Outcome.FINISHED, detail);
    }

    /**
     * The run of the source failed.
     */
    public void failed(String source, boolean automatic, String detail) {
        notify(source, automatic, Outcome.FAILED, detail);
    }

    private void notify(String source, boolean automatic, Outcome outcome, String detail) {
        if (ui == null) {
            return;
        }
        try {
            Notice notice = notificationService.prepare(source, automatic, outcome, detail);
            if (notice != null) {
                show(ui, notice, false);
            }
        } catch (Exception e) {
            log.warn("The notification of {} could not be prepared", source, e);
        }
    }

    /**
     * Shows the notice in the browser of the page, see {@link JavascriptLibrary#showDesktopNotification}.
     */
    public static void show(UI ui, Notice notice, boolean force) {
        JavascriptLibrary.showDesktopNotification(ui.getPage(), notice.title(), notice.body(), notice.tag(), force);
    }
}
