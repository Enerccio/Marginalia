package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.AnnotationFactsRegistry;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.UnderstandingSettings;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.UnderstandingBlock;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * Gives the models the language understanding of the characters ({@link UnderstandingBlock}): to the story model, as a message inserted into the payload (the
 * tokens it takes are reserved in the context budget first), and to the model that reviews a part, as a section of the facts of the annotation. One per plugin;
 * the plugin says where the levels are and how the book wants the block inserted.
 */
@Configurable
public class UnderstandingPrompt {

    /** Buffer for the headers of the inserted message, like the Author's Note adds. */
    private static final long MESSAGE_BUFFER_TOKENS = 100;

    /** What the plugin knows of a book. */
    public interface Source {

        /** The languages of the book with the speakers of each, for {@link UnderstandingBlock#build}. */
        List<UnderstandingBlock.Entry> entries(Manuscript book) throws Exception;

        /** How the book wants the block inserted. */
        UnderstandingSettings settings(Manuscript book) throws Exception;
    }

    @Autowired
    private InferenceServices inferenceServices;

    private final Source source;
    private final String propertyKey;
    private final Logger log;
    private final List<Registration> registrations = new ArrayList<>();

    /**
     * @param source      where the levels are
     * @param propertyKey a key of the generation properties that is the plugin's own (its package), where the block is kept between the two listeners
     * @param log         the log of the extension
     */
    public UnderstandingPrompt(Source source, String propertyKey, Logger log) {
        this.source = source;
        this.propertyKey = propertyKey;
        this.log = log;
    }

    /** Starts listening to the generation; {@link #unregister()} stops it. */
    public void register(StoryGenerationService generation) {
        registrations.add(generation.addEventListener(Events.BEFORE_PREPARE_CONTENT, this::reserve));
        registrations.add(generation.addEventListener(Events.AFTER_PREPARE_PAYLOAD, this::insert));
    }

    public void unregister() {
        registrations.forEach(Registration::unregister);
        registrations.clear();
    }

    /** The block of a book for the characters ({@link UnderstandingBlock#build}), all of them when blank. */
    public String block(Manuscript book, String characters) throws Exception {
        return UnderstandingBlock.build(source.entries(book), characters);
    }

    /** The facts of the annotation: the same block, a section of its own; nothing when no level is set. */
    public AnnotationFactsRegistry.Provider factsProvider() {
        return new AnnotationFactsRegistry.Provider() {
            @Override
            public List<AnnotationFactsRegistry.Section> facts(Manuscript book, ChatMessage part, List<ChatMessage> branch) throws Exception {
                String block = block(book, null);
                if (block.isBlank()) {
                    return List.of();
                }
                String body = block.substring(UnderstandingBlock.HEADER.length()).strip();
                return List.of(new AnnotationFactsRegistry.Section(UnderstandingBlock.HEADER, body));
            }
        };
    }

    private void reserve(GenerationControllerEvent event, EventChain chain) {
        try {
            // listeners are global: every generation of every user comes here, the levels are the book's
            Manuscript book = event.getManuscript();
            if (book != null && event.getPrePromptData() != null && source.settings(book).isInPrompt()) {
                String block = block(book, null);
                if (!block.isBlank()) {
                    // the same block is inserted later, even if the author edits the levels during the generation
                    event.getProperties().put(propertyKey, block);
                    long tokens = inferenceServices.forAI(event.getAi()).countTokens(block.strip()) + MESSAGE_BUFFER_TOKENS;
                    event.getPrePromptData().setReservedTokens(event.getPrePromptData().getReservedTokens() + tokens);
                }
            }
        } catch (Exception e) {
            // not inserted either: nothing is in the properties
            log.warn("Failed to prepare the language understanding block: {}", e.getMessage(), e);
            event.getProperties().remove(propertyKey);
        } finally {
            chain.next();
        }
    }

    private void insert(GenerationControllerEvent event, EventChain chain) {
        try {
            String block = event.getProperty(propertyKey);
            if (block != null) {
                event.setPayload(PayloadBlock.insert(event.getPayload(), block, source.settings(event.getManuscript()).getDepth(), LLMRole.SYSTEM));
            }
        } catch (Exception e) {
            // generate without the block rather than fail the generation
            log.warn("Failed to insert the language understanding block: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }
}
