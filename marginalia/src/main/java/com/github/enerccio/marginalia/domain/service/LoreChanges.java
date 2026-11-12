package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Tells the pages that lorebook entries were written (saved, deleted), so a page that shows them can load them again: the full-screen
 * lorebook editor of another window, while an agent or another window changes its entries. The repository of the entries reports every
 * write; the listeners hear of them once the transaction commits, all the writes of one transaction in one {@link Change} (a mass edit
 * is one change, not hundreds).
 * <p>
 * A change carries ids only, never the content: a listener loads what it needs through the services, as its own user. Static like
 * {@link com.github.enerccio.marginalia.domain.service.run.Runs}, in memory only.
 */
public final class LoreChanges {

    private static final Logger log = LoggerFactory.getLogger(LoreChanges.class);

    private static final List<Consumer<Change>> LISTENERS = new CopyOnWriteArrayList<>();

    /** The writes of the current transaction, bound to it until it commits. */
    private static final Object PENDING_KEY = new Object();

    /**
     * The entries one transaction wrote.
     *
     * @param lorebookIds the ids of their lorebooks
     * @param entryUuids  the uuids of the entries
     * @param source      the page (the Vaadin {@code UI}) the writes came from, {@code null} for a worker thread (an agent)
     */
    public record Change(Set<Long> lorebookIds, Set<String> entryUuids, Object source) {
    }

    private LoreChanges() {
    }

    /**
     * Called by the repository after it wrote the entry: the listeners hear of it after the commit (at once without a transaction).
     */
    public static void written(LorebookEntry entry) {
        if (entry == null || entry.getUuid() == null || LISTENERS.isEmpty()) {
            return;
        }
        Long lorebookId = entry.getLorebook() == null ? null : entry.getLorebook().getId();
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            fire(new Change(lorebookId == null ? Set.of() : Set.of(lorebookId), Set.of(entry.getUuid()), currentPage()));
            return;
        }
        Pending pending = (Pending) TransactionSynchronizationManager.getResource(PENDING_KEY);
        if (pending == null) {
            Pending created = new Pending(currentPage());
            pending = created;
            TransactionSynchronizationManager.bindResource(PENDING_KEY, created);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fire(new Change(Set.copyOf(created.lorebookIds), Set.copyOf(created.entryUuids), created.source));
                }

                @Override
                public void afterCompletion(int status) {
                    TransactionSynchronizationManager.unbindResourceIfPossible(PENDING_KEY);
                }
            });
        }
        if (lorebookId != null) {
            pending.lorebookIds.add(lorebookId);
        }
        pending.entryUuids.add(entry.getUuid());
    }

    /**
     * Told of every change on the thread that committed it: hand it to the page ({@code ui.access}), do not load anything here.
     *
     * @return removes the listener (on detach)
     */
    public static Registration addListener(Consumer<Change> listener) {
        LISTENERS.add(listener);
        return () -> LISTENERS.remove(listener);
    }

    private static void fire(Change change) {
        for (Consumer<Change> listener : LISTENERS) {
            try {
                listener.accept(change);
            } catch (RuntimeException e) {
                log.warn("A listener of the lore changes failed: {}", e.getMessage());
                log.debug(e.getMessage(), e);
            }
        }
    }

    private static Object currentPage() {
        try {
            return UI.getCurrent();
        } catch (RuntimeException | LinkageError e) {
            return null;
        }
    }

    private static final class Pending {
        final Set<Long> lorebookIds = new LinkedHashSet<>();
        final Set<String> entryUuids = new LinkedHashSet<>();
        final Object source;

        Pending(Object source) {
            this.source = source;
        }
    }
}
