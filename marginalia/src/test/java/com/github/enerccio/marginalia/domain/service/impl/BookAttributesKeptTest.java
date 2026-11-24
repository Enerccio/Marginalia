package com.github.enerccio.marginalia.domain.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The attributes of a book (the data of the plugins, the layout of the tabs) survive the saves around it: an edit of the AI the book
 * uses, the cleanup of a generation that failed, and a save that drops them all is logged with its caller.
 */
class BookAttributesKeptTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    /** The AI is saved from the settings while the book is closed, then the book is opened again (its window reads it and saves it). */
    @Test
    void editingTheAIKeepsTheAttributesOfItsBooks() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        Manuscript book = new Manuscript();
        book.setName(uniqueName("book"));
        book.setAi(ai);
        book = manuscriptService.save(book);
        manuscriptService.update(book, current -> {
            JsonObject data = new JsonObject();
            data.addProperty("turns", 3);
            current.getAttributes().add("plugin.data", data);
        });
        // the book list holds the book as it was read
        Manuscript listed = manuscriptService.find(book);
        assertThat(listed.getAttributes().has("plugin.data")).isTrue();

        // the AI dialog: the AI as the settings list read it, edited and saved (the model renamed)
        OpenAICompatible edited = (OpenAICompatible) aiService.find(ai.getId());
        edited.setModel("renamed-model");
        edited.setModelName("renamed-model");
        AI saved = aiService.save(edited);
        assertThat(((OpenAICompatible) saved).getModel()).isEqualTo("renamed-model");

        // the book window opens: it reads the book and saves it
        Manuscript opened = manuscriptService.find(listed);
        assertThat(opened.getAttributes().has("plugin.data")).as("attributes after the AI edit").isTrue();
        manuscriptService.save(opened);

        Manuscript stored = manuscriptService.find(book);
        assertThat(stored.getAttributes().has("plugin.data")).as("attributes after the window saved the book").isTrue();
        assertThat(stored.getAttributes().getAsJsonObject("plugin.data").get("turns").getAsInt()).isEqualTo(3);
    }

    /**
     * A generation that fails on a new part deletes the part again (the cleanup): the book must keep its attributes. The delete
     * loads the part, whose book is a lazy proxy in the same transaction, and the book is then read and saved.
     */
    @Test
    void deletingTheFailedNewPartKeepsTheAttributesOfTheBook() throws Exception {
        login();
        Manuscript book = new Manuscript();
        book.setName(uniqueName("book"));
        book.setDescription("A description");
        book = manuscriptService.save(book);
        ChatMessage first = chatMessageService.createPart(manuscriptService.find(book.getId()), null, "One two", null);
        // the generation's copy of the book, read before the new part
        Manuscript held = manuscriptService.find(book.getId());
        ChatMessage generated = chatMessageService.createPart(manuscriptService.find(book.getId()), first, "", null);
        manuscriptService.update(book, current -> {
            current.setActiveLeaf(generated);
            current.getAttributes().add("plugin.data", new JsonObject());
            current.getAttributes().getAsJsonObject("plugin.data").addProperty("turns", 3);
        });

        chatMessageService.deleteNodeAndMigrateChildren(chatMessageService.find(generated.getId()), held, true);

        Manuscript stored = manuscriptService.find(book.getId());
        assertThat(stored.getActiveLeaf().getId()).isEqualTo(first.getId());
        assertThat(stored.getAttributes().has("plugin.data")).as("attributes after the cleanup of the failed part").isTrue();
        assertThat(stored.getDescription()).isEqualTo("A description");
    }

    @Test
    void aSaveThatDropsAllTheAttributesIsLoggedWithItsCaller() throws Exception {
        login();
        Manuscript book = new Manuscript();
        book.setName(uniqueName("book"));
        book = manuscriptService.save(book);
        manuscriptService.update(book, current -> {
            current.getAttributes().addProperty("one", 1);
            current.getAttributes().addProperty("two", 2);
        });
        Logger logger = (Logger) LoggerFactory.getLogger(ExtendableEntityListener.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            // one key removed is a plugin's own change
            manuscriptService.update(book, current -> current.getAttributes().remove("one"));
            assertThat(appender.list).isEmpty();
            manuscriptService.update(book, current -> current.getAttributes().addProperty("one", 1));

            // all of them at once: what the proxy of the book did
            manuscriptService.update(book, current -> current.setAttributes(new JsonObject()));
        } finally {
            logger.detachAppender(appender);
        }
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(appender.list.getFirst().getFormattedMessage()).contains("drops all its 2 attributes");
        assertThat(appender.list.getFirst().getThrowableProxy()).as("the stack of the caller").isNotNull();
    }
}
