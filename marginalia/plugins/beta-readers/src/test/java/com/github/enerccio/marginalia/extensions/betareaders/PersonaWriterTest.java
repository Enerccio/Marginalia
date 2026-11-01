package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaWriter;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaWriter.Outcome;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaWriter.Written;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Letting the model write a persona: the request, reading the reply (with the labels, without, with Markdown) and how the run ends.
 */
class PersonaWriterTest {

    /** A service that answers with a script: the chunks, then how it ends. */
    private static class Fake implements InferenceService {
        List<LLMChatMessage> payload;
        final List<Object[]> script = new ArrayList<>();
        String ending = "completion";

        Fake say(String text) {
            script.add(new Object[]{ChunkType.RESPONSE, text});
            return this;
        }

        Fake think(String text) {
            script.add(new Object[]{ChunkType.REASONING, text});
            return this;
        }

        @Override
        public List<String> getModels() {
            return List.of();
        }

        @Override
        public long countTokens(String text) {
            return text.length() / 4;
        }

        @Override
        public long countTokensApprox(String text) {
            return text.length() / 4;
        }

        @Override
        public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) throws Exception {
            this.payload = payload;
            InferenceAsyncController controller = new InferenceAsyncController() {
                @Override
                public void continueInference() {
                }

                @Override
                public void terminateInference() {
                }
            };
            for (Object[] chunk : script) {
                callback.onChunk(controller, (ChunkType) chunk[0], (String) chunk[1]);
            }
            switch (ending) {
                case "cancel" -> callback.onCancel();
                case "error" -> callback.onError(new IllegalStateException("boom"));
                default -> callback.onCompletion();
            }
            return new CancellationToken();
        }
    }

    @Test
    void theRequestHasTheWishAndTheNamesThatAreTaken() {
        List<LLMChatMessage> messages = PersonaWriter.request("  a grumpy editor ", List.of("Athena", "Vesper"));

        assertEquals(2, messages.size());
        assertEquals(LLMRole.SYSTEM, messages.getFirst().getRole());
        assertTrue(messages.getFirst().getContent().contains("Name: <"));
        assertTrue(messages.getFirst().getContent().endsWith("choose another one: Athena, Vesper."));
        assertEquals(LLMRole.USER, messages.getLast().getRole());
        assertEquals("a grumpy editor", messages.getLast().getContent());
        assertFalse(PersonaWriter.request("x", List.of()).getFirst().getContent().contains("taken"));
    }

    @Test
    void aReplyWithTheLabelsIsRead() {
        Written written = PersonaWriter.read("Name: Rook\nDescription: The Plot Hound. Wants to know who did it.\nAnd why.");

        assertEquals("Rook", written.name());
        assertEquals("The Plot Hound. Wants to know who did it.\nAnd why.", written.description());
    }

    @Test
    void theMarksOfMarkdownAndTheFenceAreIgnored() {
        Written written = PersonaWriter.read("```\n**Name:** \"Rook\"\n**Description:** The Plot Hound.\n```");

        assertEquals("Rook", written.name());
        assertEquals("The Plot Hound.", written.description());
    }

    @Test
    void aReplyWithoutLabelsIsANameAndADescription() {
        Written written = PersonaWriter.read("Rook\n\nThe Plot Hound. Wants to know who did it.");

        assertEquals("Rook", written.name());
        assertEquals("The Plot Hound. Wants to know who did it.", written.description());
    }

    @Test
    void anEmptyReplyIsEmpty() {
        assertTrue(PersonaWriter.read("  ").isEmpty());
        assertTrue(PersonaWriter.read(null).isEmpty());
    }

    @Test
    void theReplyIsReadWhenTheStreamEndsAndTheReasoningIsLeftOut() throws Exception {
        Fake fake = new Fake().think("Let me think.").say("Name: Ro").say("ok\nDescription: The Plot Hound.");
        List<Outcome> outcomes = new ArrayList<>();

        PersonaWriter.run(fake, null, "a plot hound", List.of("Athena"), outcomes::add);

        assertEquals(1, outcomes.size());
        assertEquals("Rook", outcomes.getFirst().written().name());
        assertEquals("The Plot Hound.", outcomes.getFirst().written().description());
        assertNull(outcomes.getFirst().error());
        assertFalse(outcomes.getFirst().cancelled());
        assertTrue(fake.payload.getFirst().getContent().contains("Athena"));
    }

    @Test
    void aStoppedRunAndAFailedRunHaveNoPersona() throws Exception {
        Fake stopped = new Fake().say("Name: Rook");
        stopped.ending = "cancel";
        Fake failed = new Fake().say("Name: Rook");
        failed.ending = "error";
        List<Outcome> outcomes = new ArrayList<>();

        PersonaWriter.run(stopped, null, "x", List.of(), outcomes::add);
        PersonaWriter.run(failed, null, "x", List.of(), outcomes::add);

        assertTrue(outcomes.get(0).cancelled());
        assertNull(outcomes.get(0).written());
        assertNotNull(outcomes.get(1).error());
        assertNull(outcomes.get(1).written());
    }

    @Test
    void aModelThatWritesNothingGivesNoPersona() throws Exception {
        List<Outcome> outcomes = new ArrayList<>();

        PersonaWriter.run(new Fake(), null, "x", List.of(), outcomes::add);

        assertNull(outcomes.getFirst().written());
        assertNull(outcomes.getFirst().error());
    }
}
