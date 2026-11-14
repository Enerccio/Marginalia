package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.service.impl.TemplateServiceImpl;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LabelingTest {

    private static final TemplateService TEMPLATES = templates();

    private static TemplateService templates() {
        try {
            TemplateServiceImpl service = new TemplateServiceImpl();
            service.afterPropertiesSet();
            return service;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** The built-in instruction for these characters. */
    private static String sys(String... characters) {
        try {
            return LabelPrompt.system(TEMPLATES, null, List.of(characters));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Piece> pieces(int n) {
        List<Piece> pieces = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            pieces.add(new Piece(i, i % 2 == 0 ? Piece.Kind.NARRATION : Piece.Kind.SPEECH, "Words " + i + ".", i / 4, false));
        }
        return pieces;
    }

    /** Labels every piece that is in the request (a line "[id] N|D: ..." that is not marked as context). */
    private static LabelModel answersAll(List<String> requests) {
        return (system, user) -> {
            requests.add(user);
            List<PieceLabel> labels = new ArrayList<>();
            for (String line : user.split("\n")) {
                if (!line.startsWith("(context)")) {
                    int id = Integer.parseInt(line.substring(1, line.indexOf(']')));
                    labels.add(new PieceLabel(id, line.contains("] D:") ? "Marta" : "Narrator", Delivery.NEUTRAL, 0.5, ""));
                }
            }
            return new LabelAnswer(labels);
        };
    }

    @Test
    void aLongPartGoesInWindowsWithTheLastPiecesAsContext() throws Exception {
        List<String> requests = new ArrayList<>();

        PieceLabeler.Labelled labelled = PieceLabeler.label(pieces(95), sys("Marta"), answersAll(requests));

        assertEquals(6, requests.size(), "16 * 5 + 15");
        assertEquals(95, labelled.labels().size());
        assertEquals(0, labelled.missing());
        assertFalse(requests.get(0).contains("(context)"), "the first window has no context");
        assertEquals(4, requests.get(1).lines().filter(l -> l.startsWith("(context)")).count());
        assertTrue(requests.get(1).contains("(context) [12] N: Words 12."), requests.get(1).substring(0, 200));
        assertTrue(requests.get(1).contains("\n[16] N: Words 16."));
    }

    @Test
    void aWindowWhoseAnswerDoesNotFitTheSchemaIsAskedInHalves() throws Exception {
        List<String> sizes = new ArrayList<>();
        LabelModel truncating = (system, user) -> {
            long pieces = user.lines().filter(l -> !l.startsWith("(context)")).count();
            sizes.add(String.valueOf(pieces));
            if (pieces > 5) {
                throw new com.github.enerccio.marginalia.domain.service.structured.StructuredOutputException("{\"labels\": [", List.of("cut off"), null);
            }
            return answersAll(new ArrayList<>()).ask(system, user);
        };

        PieceLabeler.Labelled labelled = PieceLabeler.label(pieces(16), sys(), truncating);

        assertEquals(16, labelled.labels().size());
        assertEquals(0, labelled.missing());
        assertEquals(List.of("16", "8", "4", "4", "8", "4", "4"), sizes, "16 failed, 8 failed (4 and 4 fit), 8 failed (4 and 4 fit)");
    }

    @Test
    void aWindowThatCannotBeHalvedFurtherFailsWithTheReason() {
        LabelModel never = (system, user) -> {
            throw new com.github.enerccio.marginalia.domain.service.structured.StructuredOutputException("junk", List.of("not json"), null);
        };

        var e = assertThrows(Exception.class, () -> PieceLabeler.label(pieces(5), sys(), never));
        assertTrue(e.getMessage().contains("not json"), e.getMessage());
    }

    @Test
    void otherFailuresAreNotRetriedInHalves() {
        AtomicInteger asked = new AtomicInteger();
        LabelModel down = (system, user) -> {
            asked.incrementAndGet();
            throw new java.io.IOException("the server is down");
        };

        assertThrows(java.io.IOException.class, () -> PieceLabeler.label(pieces(16), sys(), down));
        assertEquals(1, asked.get(), "a server that is down is not helped by smaller requests");
    }

    @Test
    void anAnswerThatLeavesOutAPieceOrInventsOneIsHandled() throws Exception {
        LabelModel sloppy = (system, user) -> new LabelAnswer(List.of(
                new PieceLabel(0, "Narrator", Delivery.NEUTRAL, 0.5, ""),
                new PieceLabel(0, "Someone", Delivery.SAD, 0.9, ""),      // the same piece twice: the first wins
                new PieceLabel(2, "Marta", Delivery.CALM, 0.4, ""),
                new PieceLabel(99, "Ghost", Delivery.NEUTRAL, 0.5, "")));  // not a piece of the window

        PieceLabeler.Labelled labelled = PieceLabeler.label(pieces(4), sys(), sloppy);

        assertEquals(2, labelled.labels().size());
        assertEquals("Narrator", labelled.labels().get(0).speaker());
        assertEquals(2, labelled.missing());
        assertNull(labelled.labels().get(99));
    }

    @Test
    void theInstructionNamesTheCharactersAndTheAllowedTags() {
        String system = sys("Marta", "Jonas");
        assertTrue(system.contains("Marta, Jonas"));
        assertTrue(system.contains("[clear throat]") && system.contains("[laugh]"));
        assertTrue(system.contains("WHISPER") && system.contains("SHOUT"), "the words of the schema");
        assertTrue(sys().contains("not known"));
        assertFalse(system.contains("{{"), "every variable is filled in");
        assertFalse(system.contains("not known"));
    }

    @Test
    void aChangedTemplateIsUsedAndAskedAgainInsteadOfTheStoredLabels() throws Exception {
        String custom = "Label the pieces. Known: {{characters}}. Choose from {{deliveries}}.";
        String system = LabelPrompt.system(TEMPLATES, custom, List.of("Marta"));
        assertEquals("Label the pieces. Known: Marta. Choose from " + LabelPrompt.data(List.of("Marta")).getDeliveries() + ".", system);
        assertEquals(sys(), LabelPrompt.system(TEMPLATES, "   ", List.of()), "a blank template is the built-in one");
        assertNotEquals(LabelService.cacheKey("text", sys("Marta")), LabelService.cacheKey("text", system), "another instruction, other labels");
    }

    @Test
    void theLabelsAreAskedOnceAndStoredWhenComplete() throws Exception {
        Map<String, List<PieceLabel>> stored = new HashMap<>();
        LabelStore store = new LabelStore() {
            public List<PieceLabel> find(String partId, String key) {
                return stored.get(partId + "/" + key);
            }

            public void put(String partId, String key, List<PieceLabel> labels) {
                stored.put(partId + "/" + key, labels);
            }
        };
        AtomicInteger asked = new AtomicInteger();
        List<String> requests = new ArrayList<>();
        LabelModel counting = (system, user) -> {
            asked.incrementAndGet();
            return answersAll(requests).ask(system, user);
        };
        List<Piece> pieces = pieces(6);

        LabelService.Result first = LabelService.labels("p1", "text", pieces, sys("Marta"), counting, store);
        LabelService.Result again = LabelService.labels("p1", "text", pieces, sys("Marta"), counting, store);

        assertFalse(first.fromStore());
        assertTrue(again.fromStore());
        assertEquals(1, asked.get());
        assertEquals(6, again.labels().size());

        // another text or another list of characters is another question
        LabelService.labels("p1", "text changed", pieces, sys("Marta"), counting, store);
        LabelService.labels("p1", "text", pieces, sys("Marta", "Jonas"), counting, store);
        assertEquals(3, asked.get());
    }

    @Test
    void piecesTheModelLeftOutAreTheNarratorAndTheLabelsAreStored() throws Exception {
        Map<String, List<PieceLabel>> stored = new HashMap<>();
        LabelStore store = new LabelStore() {
            public List<PieceLabel> find(String partId, String key) {
                return stored.get(partId + "/" + key);
            }

            public void put(String partId, String key, List<PieceLabel> labels) {
                stored.put(partId + "/" + key, labels);
            }
        };
        LabelModel half = (system, user) -> new LabelAnswer(List.of(new PieceLabel(0, "Narrator", Delivery.NEUTRAL, 0.5, "")));

        LabelService.Result result = LabelService.labels("p1", "text", pieces(3), sys(), half, store);

        assertEquals(2, result.missing());
        assertEquals(3, result.labels().size());
        assertEquals("Narrator", result.labels().get(2).speaker());
        assertEquals(Delivery.NEUTRAL, result.labels().get(2).delivery());
        assertEquals(1, stored.size(), "stored complete: the next run does not ask again");
        assertEquals(3, stored.values().iterator().next().size());
        assertTrue(LabelService.labels("p1", "text", pieces(3), sys(), half, store).fromStore());
    }

    @Test
    void theSchemaAndTheAnswerOfTheModelAreWhatTheApplicationExpects() throws Exception {
        var schema = StructuredOutputs.schemaOf(LabelAnswer.class);
        String text = schema.toString();
        assertTrue(text.contains("tagged_text"), text);
        assertTrue(text.contains("\"WHISPER\"") && text.contains("\"SHOUT\""), "the deliveries are an enum: " + text);

        LabelAnswer answer = StructuredOutputs.parse("""
                {"labels": [{"id": 0, "speaker": "Narrator", "delivery": "NEUTRAL", "intensity": 0.5, "tagged_text": ""},
                            {"id": 1, "speaker": "Jonas", "delivery": "WHISPER", "intensity": 0.7, "tagged_text": "[sigh] Then you won't mind."}]}""",
                LabelAnswer.class);

        assertEquals(2, answer.labels().size());
        assertEquals(Delivery.WHISPER, answer.labels().get(1).delivery());
        assertEquals("[sigh] Then you won't mind.", answer.labels().get(1).taggedText());
        assertTrue(StructuredOutputs.validate(schema, "{\"labels\": [{\"id\": 0, \"speaker\": \"Narrator\", \"delivery\": \"NEUTRAL\", \"intensity\": 0.5, \"tagged_text\": \"\"}]}").valid());
        assertFalse(StructuredOutputs.validate(schema, "{\"labels\": [{\"id\": 0, \"speaker\": \"Narrator\", \"delivery\": \"FURIOUS\", \"intensity\": 0.5, \"tagged_text\": \"\"}]}").valid());
    }

    @Test
    void theScriptIsRecognisedBySoundsOfTheBook() {
        assertNull(Languages.ofScript("An English sentence."));
        assertEquals("ja", Languages.ofScript("息をしてる。"));
        assertEquals("zh", Languages.ofScript("你好，世界"));
        assertEquals("ru", Languages.ofScript("Привет, мир"));
        assertEquals("ko", Languages.ofScript("안녕하세요"));
        assertNull(Languages.ofScript("1234 ... !"));
        assertNull(Languages.ofScript("Tomáš said 息 once"), "mostly Latin: the language of the book");
    }
}
