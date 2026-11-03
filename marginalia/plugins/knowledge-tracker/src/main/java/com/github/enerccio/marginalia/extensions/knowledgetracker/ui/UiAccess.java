package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes a component from a thread that is not the one of its session (the worker of a run): through
 * {@code ui.access} and a push, skipped when the page is gone.
 */
final class UiAccess {

    private static final Logger log = LoggerFactory.getLogger(UiAccess.class);

    private UiAccess() {
    }

    static void run(Component component, Runnable change) {
        component.getUI().ifPresent(ui -> run(ui, change));
    }

    static void run(UI ui, Runnable change) {
        try {
            ui.access(() -> {
                try {
                    change.run();
                } catch (RuntimeException e) {
                    log.error(e.getMessage(), e);
                }
                UIPushGuard.push(ui);
            });
        } catch (UIDetachedException e) {
            log.debug("The page is gone, the change is skipped");
        }
    }
}
