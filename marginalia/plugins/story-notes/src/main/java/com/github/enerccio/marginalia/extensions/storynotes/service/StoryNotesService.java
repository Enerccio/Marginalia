package com.github.enerccio.marginalia.extensions.storynotes.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.storynotes.model.NoteBook;
import com.github.enerccio.marginalia.extensions.storynotes.model.NotePage;
import com.github.enerccio.marginalia.extensions.storynotes.model.StoryNotesData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Reads and stores the pages of a book, in its attributes. Every change goes through {@link #update}: the pages are read
 * again from the stored book, changed and stored under a lock, so the editor and a command (an agent, the plot wizard)
 * working on different pages at the same time do not overwrite each other with a copy that is older.
 */
@Configurable
public class StoryNotesService {

    @Autowired
    private ManuscriptService manuscriptService;

    private final Gson gson = new Gson();

    /** A change of the pages; it may throw, nothing is stored then. */
    @FunctionalInterface
    public interface Change<T> {
        T apply(NoteBook book) throws Exception;
    }

    /** Who wants to know that the pages of a book changed, by the uuid of the book. */
    private final Map<String, Set<Runnable>> changeListeners = new ConcurrentHashMap<>();

    /**
     * Tells {@code listener} whenever {@link #changed} is called for the book, from the thread of the caller. For a view
     * that keeps the pages in memory and has to read them again when another view or a command changed them.
     *
     * @return what removes the listener
     */
    public Runnable onChange(Manuscript manuscript, Runnable listener) {
        String key = manuscript.getUuid();
        changeListeners.computeIfAbsent(key, _ -> new CopyOnWriteArraySet<>()).add(listener);
        return () -> changeListeners.computeIfPresent(key, (_, set) -> {
            set.remove(listener);
            return set.isEmpty() ? null : set;
        });
    }

    /** Tells the views of the book that its pages were stored. */
    public void changed(Manuscript manuscript) {
        Set<Runnable> listeners = changeListeners.get(manuscript.getUuid());
        if (listeners != null) {
            listeners.forEach(Runnable::run);
        }
    }

    /**
     * The pages of the book as they are stored now - the story view keeps the book it was opened with, the pages may
     * have been saved since.
     */
    public NoteBook load(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return read(current != null ? current : manuscript);
    }

    public NoteBook read(Manuscript manuscript) {
        JsonObject attrs = manuscript.getAttributes();
        StoryNotesData data = null;
        if (attrs != null && attrs.has(StoryNotesData.KEY)) {
            data = gson.fromJson(attrs.get(StoryNotesData.KEY), StoryNotesData.class);
        }
        return new NoteBook(data);
    }

    /**
     * Applies the change to the pages as they are stored now, stores them and tells the open views.
     *
     * @return what the change returns
     */
    public <T> T update(Manuscript manuscript, Change<T> change) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        // read, change and save in one write transaction: two changes of the pages never lose one another
        manuscriptService.update(manuscript, current -> {
            NoteBook book = read(current);
            result.set(change.apply(book));
            JsonObject attrs = current.getAttributes();
            if (attrs == null) {
                attrs = new JsonObject();
                current.setAttributes(attrs);
            }
            attrs.add(StoryNotesData.KEY, gson.toJsonTree(book.toData()));
        });
        changed(manuscript);
        return result.get();
    }

    // --- the pages as text for the assistants ---

    /**
     * The titles of the pages the assistants may see, with the tags and the first line, empty when there are none.
     */
    public static String indexText(NoteBook book) {
        List<NotePage> pages = book.visible().stream().filter(p -> !p.getTitle().isBlank()).toList();
        if (pages.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder("Notes of the author (title, tags, first line):");
        for (NotePage page : pages) {
            text.append("\n- ").append(page.getTitle().strip());
            if (!page.getTags().isEmpty()) {
                text.append(" [").append(String.join(", ", page.getTags())).append(']');
            }
            String line = NoteBook.firstLine(page.getContent(), 100);
            if (!line.isEmpty()) {
                text.append(": ").append(line);
            }
        }
        return text.toString();
    }

    /**
     * The pages the assistants may see with their whole text, empty when there are none.
     */
    public static String fullText(NoteBook book) {
        return book.visible().stream().filter(p -> !p.getTitle().isBlank()).map(StoryNotesService::pageText)
                .collect(Collectors.joining("\n\n"));
    }

    /** One page as the model reads it: its title, the tags and the text. */
    public static String pageText(NotePage page) {
        StringBuilder text = new StringBuilder("## ").append(page.getTitle().strip());
        if (!page.getTags().isEmpty()) {
            text.append("\nTags: ").append(String.join(", ", page.getTags()));
        }
        if (!page.getContent().isBlank()) {
            text.append("\n\n").append(page.getContent().strip());
        }
        return text.toString();
    }
}
