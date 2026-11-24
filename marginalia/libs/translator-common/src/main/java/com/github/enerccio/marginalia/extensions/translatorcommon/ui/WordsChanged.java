package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Tells the open views of a plugin that the stored words changed outside of them: a translation proposed names for the glossary of a book, or
 * added new words to the lexicon of a language. A view shows what it read when it was built, so without this it kept the old list until the
 * author pressed <i>Refresh</i>.
 * <p>
 * The key is what changed, in the plugin's own terms (the uuid of a book, the id of a language); a view says which keys are about what it shows.
 * The services call {@link #changed} after they stored the words, from any thread; each view does its work in its own page, with the session of
 * the user who caused the change (the services the view calls need it), and the page is pushed. The library is embedded in each plugin, so each
 * plugin has its own listeners.
 */
public final class WordsChanged {

    /** An open view: what it is about, how its page is reached, and what it does then. */
    record Listener(Predicate<String> about, Consumer<Runnable> page, Runnable action) {
    }

    private static final Set<Listener> LISTENERS = ConcurrentHashMap.newKeySet();

    private WordsChanged() {
    }

    /**
     * Calls {@code action} in the view's page while the view is attached, whenever the words of a key it is about changed.
     *
     * @param view   the view; it listens while it is attached (a view built again replaces the old one, which is detached)
     * @param about  whether a key is about what the view shows; read at the time of the change, so it may follow the view's choice
     * @param action reads the words again and shows them
     */
    public static void listen(Component view, Predicate<String> about, Runnable action) {
        Listener[] current = new Listener[1];
        Consumer<UI> attach = ui -> {
            if (current[0] == null) {
                current[0] = add(about, onPage(ui), action);
            }
        };
        view.addAttachListener(event -> attach.accept(event.getUI()));
        view.addDetachListener(_ -> {
            remove(current[0]);
            current[0] = null;
        });
        view.getUI().ifPresent(attach);
    }

    /** The stored words of the key changed: every attached view about it shows them again. */
    public static void changed(String key) {
        if (key == null) {
            return;
        }
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        for (Listener listener : LISTENERS) {
            if (listener.about().test(key)) {
                listener.page().accept(() -> {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        listener.action().run();
                    }
                });
            }
        }
    }

    static Listener add(Predicate<String> about, Consumer<Runnable> page, Runnable action) {
        Listener listener = new Listener(about, page, action);
        LISTENERS.add(listener);
        return listener;
    }

    static void remove(Listener listener) {
        if (listener != null) {
            LISTENERS.remove(listener);
        }
    }

    /** Runs the work in the page (later, when the page is locked by this thread) and pushes it; a page that is gone is skipped. */
    private static Consumer<Runnable> onPage(UI ui) {
        return work -> {
            try {
                ui.access(() -> {
                    try {
                        work.run();
                    } catch (RuntimeException e) {
                        // one view that can not read the words does not keep the others from it; the view shows its own errors
                    }
                    UIPushGuard.push(ui);
                });
            } catch (UIDetachedException e) {
                // the page is gone; its detach listener takes the view away
            }
        };
    }
}
