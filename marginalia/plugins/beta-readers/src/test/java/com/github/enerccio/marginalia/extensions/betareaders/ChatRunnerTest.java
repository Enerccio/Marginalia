package com.github.enerccio.marginalia.extensions.betareaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.github.enerccio.marginalia.extensions.betareaders.service.ChatRunner;
import com.github.enerccio.marginalia.extensions.betareaders.service.ChatRunner.Outcome;
import com.github.enerccio.marginalia.extensions.betareaders.service.ChatRunner.Request;
import com.github.enerccio.marginalia.extensions.betareaders.service.EmptyReplyException;
import com.github.enerccio.marginalia.extensions.betareaders.service.ReplyReader.Reading;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Asking the model with a fake stream: the request (the schema, the prompt), what is read while it streams, how it ends (finished,
 * stopped, failed, empty), and the fall back to text.
 */
class ChatRunnerTest {

    private static final String REPLY = """
            {"reasoning": "Athena first.", "messages": [
              {"speaker": "Athena", "parts": [{"type": "speech", "text": "Hello."}]},
              {"speaker": "Vesper", "parts": [{"type": "action", "text": "shrugs"}]}]}""";

    /** A service whose stream is a script: the chunks, then how it ends. */
    private static class Fake implements InferenceService {
        List<LLMChatMessage> payload;
        JsonNode schema;
        boolean schemaSupported = true;
        int starts;
        List<Object[]> script = new ArrayList<>();   // {ChunkType, String}
        String ending = "completion";               // completion, cancel, error
        boolean runOnStart = true;
        final AtomicInteger continued = new AtomicInteger();
        InferenceAsyncCallback callback;

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
        public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema, FunctionSet functions,
                                        InferenceAsyncCallback callback) throws Exception {
            starts++;
            if (schema != null && !schemaSupported) {
                throw new UnsupportedOperationException("no schema");
            }
            this.payload = payload;
            this.schema = schema;
            this.callback = callback;
            CancellationToken token = new CancellationToken();
            if (runOnStart) {
                play();
            }
            return token;
        }

        void play() throws Exception {
            InferenceAsyncController controller = new InferenceAsyncController() {
                @Override
                public void continueInference() {
                    continued.incrementAndGet();
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
        }

        Fake say(String text) {
            script.add(new Object[]{ChunkType.RESPONSE, text});
            return this;
        }

        Fake think(String text) {
            script.add(new Object[]{ChunkType.REASONING, text});
            return this;
        }
    }

    private static class Recorder implements ChatRunner.Listener {
        final List<Reading> progress = new ArrayList<>();
        final List<Outcome> finished = new ArrayList<>();

        @Override
        public void onProgress(Reading reading) {
            progress.add(reading);
        }

        @Override
        public void onFinished(Outcome outcome) {
            finished.add(outcome);
        }

        Outcome outcome() {
            assertEquals(1, finished.size(), "the run has to end exactly once");
            return finished.getFirst();
        }
    }

    private static BetaChat chat(ReplyFormat format) {
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        chat.setFormat(format);
        chat.addPersona(new Persona("Athena", "The Archivist."));
        chat.addPersona(new Persona("Vesper", "The Realist."));
        chat.getMessages().add(BetaMessage.user("What do you think?"));
        return chat;
    }

    private static Request request(BetaChat chat, boolean nativeReasoning) {
        return new Request(chat, "The prompt.", nativeReasoning, 4, 0, null);
    }

    private static List<String> streamed(Fake fake, String text) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += 7) {
            chunks.add(text.substring(i, Math.min(text.length(), i + 7)));
        }
        chunks.forEach(fake::say);
        return chunks;
    }

    @Test
    void theRequestIsAskedForInJsonWithTheSchemaOfTheChat() throws Exception {
        Fake fake = new Fake();
        streamed(fake, REPLY);

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), new Recorder());

        assertNotNull(fake.schema);
        assertEquals(List.of("Athena", "Vesper", "Narrator"), enumOf(fake.schema));
        assertEquals(4, fake.schema.path("properties").path("messages").path("maxItems").asInt());
        assertFalse(fake.schema.path("properties").path("reasoning").isMissingNode());
        assertEquals("The prompt.\n\n# How to answer", fake.payload.getFirst().getContent().substring(0, "The prompt.\n\n# How to answer".length()));
        assertTrue(fake.payload.getLast().getContent().contains("[The personas present]"));
    }

    private static List<String> enumOf(JsonNode schema) {
        List<String> names = new ArrayList<>();
        schema.path("properties").path("messages").path("items").path("properties").path("speaker").path("enum")
                .forEach(node -> names.add(node.asText()));
        return names;
    }

    @Test
    void aModelThatReasonsOnItsOwnGetsNoReasoningField() throws Exception {
        Fake fake = new Fake();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), true), new Recorder());

        assertTrue(fake.schema.path("properties").path("reasoning").isMissingNode());
    }

    @Test
    void theReplyIsReadWhileItStreamsAndOnlyFinishedMessagesAreShown() throws Exception {
        Fake fake = new Fake();
        streamed(fake, REPLY);
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), recorder);

        assertFalse(recorder.progress.isEmpty());
        int previous = 0;
        for (Reading reading : recorder.progress) {
            assertTrue(reading.messages().size() >= previous);
            assertTrue(reading.messages().size() <= 2);
            previous = reading.messages().size();
        }
        assertEquals(2, recorder.progress.stream().mapToInt(r -> r.messages().size()).max().orElse(0));
        assertEquals(fake.script.size(), fake.continued.get(), "every chunk has to let the stream go on");

        Outcome outcome = recorder.outcome();
        assertNull(outcome.error());
        assertFalse(outcome.cancelled());
        assertTrue(outcome.json());
        assertEquals("Athena first.", outcome.reply().getReasoning());
        assertEquals(2, outcome.reply().getSpoken().size());
        assertEquals("Athena", outcome.reply().getSpoken().getFirst().getSpeaker());
    }

    @Test
    void theReasoningOfTheModelShowsWhereTheFieldWouldBe() throws Exception {
        Fake fake = new Fake();
        fake.think("Let me ").think("think.");
        streamed(fake, "{\"messages\": [{\"speaker\": \"Athena\", \"parts\": [{\"type\": \"speech\", \"text\": \"Hi\"}]}]}");
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), true), recorder);

        assertEquals("Let me think.", recorder.outcome().reply().getReasoning());
        assertEquals("Let me think.", recorder.progress.getLast().reasoning());
    }

    @Test
    void theReasoningFieldWinsOverTheReasoningOfTheModel() throws Exception {
        Fake fake = new Fake();
        fake.think("native");
        streamed(fake, REPLY);
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), recorder);

        assertEquals("Athena first.", recorder.outcome().reply().getReasoning());
    }

    @Test
    void aChatThatWantsTextIsAskedWithoutASchema() throws Exception {
        Fake fake = new Fake();
        streamed(fake, "**Athena:** Hello.\n\n**Vesper:** *shrugs*");
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.TEXT), false), recorder);

        assertNull(fake.schema);
        assertTrue(fake.payload.getFirst().getContent().contains("**Name:**"));
        Outcome outcome = recorder.outcome();
        assertFalse(outcome.json());
        assertEquals(2, outcome.reply().getSpoken().size());
        assertEquals(PartType.ACTION, outcome.reply().getSpoken().get(1).getParts().getFirst().getType());
    }

    @Test
    void autoFallsBackToTextWhenTheServiceTakesNoSchema() throws Exception {
        Fake fake = new Fake();
        fake.schemaSupported = false;
        streamed(fake, "**Athena:** Hello.");
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), recorder);

        assertNull(fake.schema);
        assertFalse(recorder.outcome().json());
        assertEquals(1, recorder.outcome().reply().getSpoken().size());
    }

    @Test
    void aChatThatWantsJsonDoesNotFallBack() {
        Fake fake = new Fake();
        fake.schemaSupported = false;

        assertThrows(UnsupportedOperationException.class,
                () -> ChatRunner.run(fake, null, request(chat(ReplyFormat.JSON), false), new Recorder()));
    }

    @Test
    void aStoppedReplyKeepsWhatWasRead() throws Exception {
        Fake fake = new Fake();
        fake.ending = "cancel";
        fake.say(REPLY.substring(0, REPLY.indexOf("Vesper") - 20));
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), recorder);

        Outcome outcome = recorder.outcome();
        assertTrue(outcome.cancelled());
        assertNull(outcome.error());
        assertEquals(1, outcome.reply().getSpoken().size());
    }

    @Test
    void aStoppedReplyWithNothingIsNoReply() throws Exception {
        Fake fake = new Fake();
        fake.ending = "cancel";
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), recorder);

        assertTrue(recorder.outcome().cancelled());
        assertNull(recorder.outcome().reply());
        assertNull(recorder.outcome().error());
    }

    @Test
    void aFailedReplyIsNotKept() throws Exception {
        Fake fake = new Fake();
        fake.ending = "error";
        streamed(fake, REPLY.substring(0, 80));
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), recorder);

        assertNull(recorder.outcome().reply());
        assertEquals("boom", recorder.outcome().error().getMessage());
    }

    @Test
    void aModelThatWritesNothingIsAnError() throws Exception {
        Fake fake = new Fake();
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), recorder);

        assertNull(recorder.outcome().reply());
        assertInstanceOf(EmptyReplyException.class, recorder.outcome().error());
    }

    @Test
    void aReplyThatIsNotMessagesIsKeptAsText() throws Exception {
        Fake fake = new Fake();
        streamed(fake, "Sorry, I lost my train of thought.");
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), recorder);

        assertTrue(recorder.progress.stream().allMatch(Reading::isEmpty));
        assertTrue(recorder.outcome().reply().getSpoken().isEmpty());
        assertEquals("Sorry, I lost my train of thought.", recorder.outcome().reply().getRawText());
    }

    @Test
    void messagesOverTheLimitAreDropped() throws Exception {
        Fake fake = new Fake();
        streamed(fake, REPLY);
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, new Request(chat(ReplyFormat.AUTO), "P", false, 1, 0, null), recorder);

        assertEquals(1, recorder.outcome().reply().getSpoken().size());
    }

    @Test
    void theLimitOfTheTokensTrimsTheHistoryAndTheOutcomeSaysSo() throws Exception {
        Fake fake = new Fake();
        BetaChat chat = chat(ReplyFormat.AUTO);
        chat.getMessages().clear();
        for (int i = 0; i < 6; i++) {
            chat.getMessages().add(BetaMessage.user("question " + "x".repeat(400)));
            chat.getMessages().add(BetaMessage.assistant("", List.of(new BetaSpoken("Athena",
                    List.of(new BetaPart(PartType.SPEECH, "answer " + "y".repeat(400)))))));
        }
        chat.getMessages().add(BetaMessage.user("the last question"));
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, new Request(chat, "P", false, 4, 400, null), recorder);

        assertTrue(recorder.outcome().prompt().dropped() > 0);
        assertTrue(fake.payload.size() < 14);
        assertTrue(fake.payload.getLast().getContent().startsWith("the last question"));
    }

    @Test
    void theRunEndsOnceEvenWhenTheServiceReportsTwice() throws Exception {
        Fake fake = new Fake();
        fake.runOnStart = false;
        streamed(fake, REPLY);
        Recorder recorder = new Recorder();

        ChatRunner.run(fake, null, request(chat(ReplyFormat.AUTO), false), recorder);
        fake.play();
        fake.callback.onCancel();

        assertEquals(1, recorder.finished.size());
    }
}
