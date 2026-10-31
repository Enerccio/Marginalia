package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Lets the model write a persona from a few words of the user: a name and a description in the style of the personas of the library.
 * It touches no service and stores nothing; the caller shows the result, which the user can still change. The reply is plain text
 * ({@code Name: ...}, then {@code Description: ...}), so every kind of AI can give it.
 */
public final class PersonaWriter {

    static final String SYSTEM = """
            You create personas for beta readers: invented readers who react to an author's work in progress, each with a voice, tastes \
            and priorities of their own. The author describes the reader they want; you write that persona.

            Reply in exactly this form and write nothing else:

            Name: <a short name, one or two words>
            Description: <a title for the persona, then two to four sentences about how the persona reads: what they notice, what they \
            like and what annoys them, and how they talk>

            Example:

            Name: Quill
            Description: The Picky Editor. Meticulous and exacting, but not unkind. He reads line by line: repetition, filler, weak verbs \
            and continuity slips. His praise is rare and specific.""";

    private PersonaWriter() {
    }

    /**
     * What the model wrote.
     *
     * @param name        the name; blank when the reply had none
     * @param description the description; blank when the reply had none
     */
    public record Written(String name, String description) {

        public boolean isEmpty() {
            return name.isBlank() && description.isBlank();
        }
    }

    /**
     * How the request ended.
     *
     * @param written   the persona; null when the model wrote nothing that can be read
     * @param cancelled the user stopped it
     * @param error     what went wrong; null when nothing did
     */
    public record Outcome(Written written, boolean cancelled, Throwable error) {
    }

    /**
     * The messages to send.
     *
     * @param brief the wish of the user, a few words
     * @param taken the names of the personas that exist, which the new one must not take
     */
    public static List<LLMChatMessage> request(String brief, List<String> taken) {
        String system = SYSTEM + (taken.isEmpty() ? "" : "\n\nThese names are taken, choose another one: " + String.join(", ", taken) + ".");
        List<LLMChatMessage> messages = new ArrayList<>();
        messages.add(LLMChatMessage.of(LLMRole.SYSTEM, system));
        messages.add(LLMChatMessage.of(LLMRole.USER, brief.strip()));
        return messages;
    }

    /**
     * Reads the reply. A reply without the labels is read as a name (its first line) and a description (the rest); the marks of
     * Markdown around the labels and the name are ignored.
     */
    public static Written read(String reply) {
        String text = reply == null ? "" : reply.strip();
        text = text.replaceAll("(?s)^```[a-zA-Z]*\\s*(.*?)\\s*```$", "$1").strip();
        String name = "";
        StringBuilder description = new StringBuilder();
        boolean inDescription = false;
        for (String line : text.split("\\R")) {
            String bare = plain(line);
            if (!inDescription && bare.regionMatches(true, 0, "Name:", 0, 5) && name.isBlank()) {
                name = clean(bare.substring(5));
            } else if (bare.regionMatches(true, 0, "Description:", 0, 12)) {
                inDescription = true;
                description.append(bare.substring(12).strip());
            } else if (inDescription) {
                description.append('\n').append(line.strip());
            } else if (name.isBlank() && !bare.isBlank()) {
                // no labels: the first line is the name, the rest the description
                name = clean(bare);
                inDescription = true;
            } else if (!bare.isBlank()) {
                description.append(description.isEmpty() ? "" : "\n").append(line.strip());
            }
        }
        return new Written(name, description.toString().strip());
    }

    /** The line without the emphasis marks of Markdown at its start and its label. */
    private static String plain(String line) {
        return line.strip().replace("**", "").replace("__", "").strip();
    }

    private static String clean(String name) {
        return name.strip().replaceAll("^[\"'“”`*_]+|[\"'“”`*_]+$", "").strip();
    }

    /**
     * Asks the model.
     *
     * @param done told when the request ended, on a thread of the inference service
     * @return what stops it
     */
    public static CancellationToken run(InferenceService inference, Protocol protocol, String brief, List<String> taken,
                                        Consumer<Outcome> done) throws Exception {
        StringBuilder reply = new StringBuilder();
        AtomicBoolean finished = new AtomicBoolean();
        CancellationToken[] token = new CancellationToken[1];
        InferenceService.InferenceAsyncCallback callback = new InferenceService.InferenceAsyncCallback() {
            @Override
            public void onChunk(InferenceAsyncController controller, ChunkType type, String text) {
                try {
                    if (type != ChunkType.REASONING) {
                        reply.append(text);
                    }
                } finally {
                    controller.continueInference();
                }
            }

            @Override
            public void onCompletion() {
                finish(false, null);
            }

            @Override
            public void onCancel() {
                finish(true, null);
            }

            @Override
            public void onError(Throwable exception) {
                finish(false, exception);
            }

            @Override
            public boolean isDead() {
                return token[0] != null && token[0].isCancelled();
            }

            private void finish(boolean cancelled, Throwable error) {
                if (!finished.compareAndSet(false, true)) {
                    return;
                }
                Written written = error == null && !cancelled ? read(reply.toString()) : null;
                done.accept(new Outcome(written == null || written.isEmpty() ? null : written, cancelled, error));
            }
        };
        token[0] = inference.stream(request(brief, taken), protocol, callback);
        return token[0];
    }
}
