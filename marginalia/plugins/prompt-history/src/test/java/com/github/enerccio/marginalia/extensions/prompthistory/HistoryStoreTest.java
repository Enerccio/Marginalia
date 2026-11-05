package com.github.enerccio.marginalia.extensions.prompthistory;

import com.github.enerccio.marginalia.domain.service.InferenceListener.*;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class HistoryStoreTest {

    private static Request request(String content) {
        return new Request(Instant.now(), "annotate", null, "My AI", "model-1", null,
                List.of(new Message("SYSTEM", "Be brief.", 0, List.of()), new Message("USER", content, 2, List.of())));
    }

    @Test
    void entryIsListedWhileRunningAndShowsTheAnswerSoFar() {
        HistoryStore store = new HistoryStore();
        HistoryEntry entry = new HistoryEntry(request("Hello."), store);
        store.add(entry);

        entry.chunk(ChunkType.REASONING, "hmm");
        entry.chunk(ChunkType.RESPONSE, "Hi ");
        entry.chunk(ChunkType.RESPONSE, "there");

        assertTrue(entry.running());
        assertNull(entry.outcome());
        assertEquals("Hi there", entry.answer().text());
        assertEquals("hmm", entry.answer().reasoning());
        assertEquals(1, store.entries().size());
    }

    @Test
    void finishedEntryIsPackedAndUnpackedWhole() {
        HistoryStore store = new HistoryStore();
        String story = "Once upon a time. ".repeat(5000);
        HistoryEntry entry = new HistoryEntry(request(story), store);
        store.add(entry);
        entry.chunk(ChunkType.RESPONSE, "partial");

        entry.finish(new Response(Outcome.COMPLETED, "the answer", "the thoughts",
                List.of(new FunctionCall("lookup", "{\"q\":1}", "found", false)), null, 1234));

        assertFalse(entry.running());
        assertEquals(Outcome.COMPLETED, entry.outcome());
        assertEquals(1234, entry.millis());
        assertEquals("the answer", entry.answer().text());
        assertEquals("the thoughts", entry.answer().reasoning());
        assertEquals("lookup", entry.answer().calls().get(0).name());
        assertEquals("found", entry.answer().calls().get(0).result());
        List<Message> messages = entry.messages();
        assertEquals(2, messages.size());
        assertEquals("USER", messages.get(1).role());
        assertEquals(story, messages.get(1).content());
        assertEquals(2, messages.get(1).attachments());
    }

    @Test
    void failedEntryKeepsTheError() {
        HistoryStore store = new HistoryStore();
        HistoryEntry entry = new HistoryEntry(request("x"), store);
        entry.finish(new Response(Outcome.FAILED, "", null, List.of(), "boom", 5));

        assertEquals(Outcome.FAILED, entry.outcome());
        assertEquals("boom", entry.answer().error());
    }

    @Test
    void storeKeepsTheLastTwentyNewestFirstAndTellsListeners() {
        HistoryStore store = new HistoryStore();
        AtomicInteger told = new AtomicInteger();
        Runnable listener = told::incrementAndGet;
        store.addListener(listener);

        HistoryEntry first = null;
        for (int i = 0; i < HistoryStore.LIMIT + 5; i++) {
            HistoryEntry entry = new HistoryEntry(request("request " + i), store);
            if (i == 0) {
                first = entry;
            }
            store.add(entry);
        }

        List<HistoryEntry> entries = store.entries();
        assertEquals(HistoryStore.LIMIT, entries.size());
        assertEquals("request " + (HistoryStore.LIMIT + 4), entries.get(0).messages().get(1).content());
        assertFalse(entries.contains(first));
        assertEquals(HistoryStore.LIMIT + 5, told.get());

        store.removeListener(listener);
        store.clear();
        assertTrue(store.entries().isEmpty());
        assertEquals(HistoryStore.LIMIT + 5, told.get());
    }

    @Test
    void streamingTellsListenersAtMostEveryFewHundredMillis() {
        HistoryStore store = new HistoryStore();
        HistoryEntry entry = new HistoryEntry(request("x"), store);
        store.add(entry);
        AtomicInteger told = new AtomicInteger();
        store.addListener(told::incrementAndGet);

        for (int i = 0; i < 1000; i++) {
            entry.chunk(ChunkType.RESPONSE, "a");
        }

        assertTrue(told.get() <= 5, "told " + told.get() + " times");
    }
}
