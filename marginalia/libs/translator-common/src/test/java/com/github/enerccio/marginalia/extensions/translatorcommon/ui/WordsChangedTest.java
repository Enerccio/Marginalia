package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The views told about changed words: only those about the key, in their page, until they are taken away. */
class WordsChangedTest {

    private final List<WordsChanged.Listener> added = new ArrayList<>();

    @AfterEach
    void removeListeners() {
        added.forEach(WordsChanged::remove);
    }

    private WordsChanged.Listener listen(java.util.function.Predicate<String> about, List<String> pages, String name, List<String> calls) {
        WordsChanged.Listener listener = WordsChanged.add(about, work -> {
            pages.add(name);
            work.run();
        }, () -> calls.add(name));
        added.add(listener);
        return listener;
    }

    @Test
    void onlyTheViewsAboutTheKeyAreToldInTheirPage() {
        List<String> pages = new ArrayList<>();
        List<String> calls = new ArrayList<>();
        listen("book-1"::equals, pages, "first", calls);
        listen("book-2"::equals, pages, "second", calls);
        WordsChanged.changed("book-1");
        assertEquals(List.of("first"), calls);
        assertEquals(List.of("first"), pages);
        WordsChanged.changed(null);
        assertEquals(List.of("first"), calls);
    }

    @Test
    void theKeyIsReadAtTheTimeOfTheChange() {
        AtomicReference<String> shown = new AtomicReference<>("lang-a");
        List<String> calls = new ArrayList<>();
        listen(key -> key.equals(shown.get()), new ArrayList<>(), "view", calls);
        WordsChanged.changed("lang-b");
        assertEquals(List.of(), calls);
        shown.set("lang-b");
        WordsChanged.changed("lang-b");
        assertEquals(List.of("view"), calls);
    }

    @Test
    void aRemovedViewIsNotTold() {
        List<String> calls = new ArrayList<>();
        WordsChanged.Listener listener = listen("book-1"::equals, new ArrayList<>(), "view", calls);
        WordsChanged.remove(listener);
        WordsChanged.remove(null);
        WordsChanged.changed("book-1");
        assertEquals(List.of(), calls);
    }
}
