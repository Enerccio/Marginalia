package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.impl.StaleSaves;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The safe writes of {@code TODO.CONCURRENCY.md} (design 1): {@code update(entity, change)} applies a change to the row as it is
 * stored, and {@link StaleSaves} reports a save of a copy read before the last save of its row (the build makes it fail the tests).
 */
class SafeWritesTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    private Manuscript book;
    private ChatMessage part;

    @BeforeEach
    void setUp() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        ChatMessage message = new ChatMessage();
        message.setResponse("Anna met John.");
        part = chatMessageService.createRoot(book, message);
        // a part saved after the book touches it, the copy of the book is older than that touch and is not stale
        book.setActiveLeaf(part);
        book = manuscriptService.save(book);
    }

    @AfterEach
    void mode() {
        StaleSaves.setMode(null);
    }

    private static void put(ChatMessage part, String key, String value) {
        if (part.getAttributes() == null) {
            part.setAttributes(new JsonObject());
        }
        part.getAttributes().addProperty(key, value);
    }

    private static void put(Manuscript book, String key, String value) {
        if (book.getAttributes() == null) {
            book.setAttributes(new JsonObject());
        }
        book.getAttributes().addProperty(key, value);
    }

    private static void later() throws InterruptedException {
        // the modification is in milliseconds
        Thread.sleep(5);
    }

    @Test
    void anUpdateOfAPartKeepsWhatWasSavedSinceTheCopyWasRead() throws Exception {
        ChatMessage held = chatMessageService.find(part.getId());
        later();
        chatMessageService.update(part, p -> put(p, "first", "1"));
        later();
        ChatMessage saved = chatMessageService.update(held, p -> put(p, "second", "2"));

        assertThat(saved.getAttributes().get("first").getAsString()).isEqualTo("1");
        ChatMessage stored = chatMessageService.find(part.getId());
        assertThat(stored.getAttributes().get("first").getAsString()).isEqualTo("1");
        assertThat(stored.getAttributes().get("second").getAsString()).isEqualTo("2");
        assertThat(stored.getResponse()).isEqualTo("Anna met John.");
    }

    @Test
    void anUpdateOfAPartThatIsGoneChangesNothing() throws Exception {
        chatMessageService.delete(chatMessageService.find(part.getId()), false);

        assertThatThrownBy(() -> chatMessageService.update(part, p -> put(p, "x", "y")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aSaveOfAStaleCopyFailsInTheTests() throws Exception {
        assertThat(StaleSaves.getMode()).as("set by the build").isEqualTo(StaleSaves.Mode.FAIL);
        ChatMessage held = chatMessageService.find(part.getId());
        later();
        chatMessageService.update(part, p -> put(p, "plugin", "data"));
        put(held, "other", "value");

        assertThatThrownBy(() -> chatMessageService.save(held)).isInstanceOf(StaleSaves.StaleSaveException.class)
                .hasMessageContaining("ChatMessage").hasMessageContaining(SafeWritesTest.class.getSimpleName());
        assertThat(chatMessageService.find(part.getId()).getAttributes().has("plugin")).isTrue();
    }

    @Test
    void aSaveOfAStaleCopyIsOnlyLoggedOutsideTheTests() throws Exception {
        StaleSaves.setMode(StaleSaves.Mode.LOG);
        Manuscript held = manuscriptService.find(book.getId());
        later();
        manuscriptService.update(book, b -> put(b, "plugin", "data"));

        manuscriptService.save(held);

        // the warning does not repair anything: the copy wrote the old data back
        assertThat(manuscriptService.find(book.getId()).getAttributes() == null
                || !manuscriptService.find(book.getId()).getAttributes().has("plugin")).isTrue();
    }

    @Test
    void aCopyReadAgainIsNotStale() throws Exception {
        later();
        manuscriptService.update(book, b -> put(b, "plugin", "data"));
        Manuscript fresh = manuscriptService.find(book.getId());
        fresh.setName(uniqueName("renamed"));

        manuscriptService.save(fresh);

        assertThat(manuscriptService.find(book.getId()).getAttributes().get("plugin").getAsString()).isEqualTo("data");
    }

    @Test
    void parallelUpdatesOfTwoPluginsOnOneBookLoseNothing() throws Exception {
        int rounds = 15;
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> done = new ArrayList<>();
            for (String plugin : List.of("a", "b")) {
                done.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < rounds; i++) {
                        int round = i;
                        manuscriptService.update(book, b -> put(b, plugin + round, "x"));
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : done) {
                future.get(60, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        JsonObject attributes = manuscriptService.find(book.getId()).getAttributes();
        for (int i = 0; i < rounds; i++) {
            assertThat(attributes.has("a" + i)).as("a" + i).isTrue();
            assertThat(attributes.has("b" + i)).as("b" + i).isTrue();
        }
    }
}
