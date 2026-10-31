package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaSpoken;
import com.github.enerccio.marginalia.extensions.betareaders.model.PartType;
import com.github.enerccio.marginalia.extensions.betareaders.service.ReplyReader;
import com.github.enerccio.marginalia.extensions.betareaders.service.ReplyReader.Reading;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Replies are read into messages: JSON (also cut or broken, and while it streams: only the finished messages), free text, and text
 * that is neither.
 */
class ReplyReaderTest {

    private static final List<String> SPEAKERS = List.of("Athena", "Vesper");

    private static final String JSON = """
            {"reasoning": "Athena starts, Vesper objects.", "messages": [
              {"speaker": "Athena", "parts": [{"type": "action", "text": "nods slowly"}, {"type": "speech", "text": "A strong chapter."}]},
              {"speaker": "Vesper", "parts": [{"type": "speech", "text": "Slow, with {braces} and \\"quotes\\"."}]},
              {"speaker": "Narrator", "parts": [{"type": "action", "text": "The fire crackles."}]}]}""";

    private static Reading read(String text, boolean done) {
        return ReplyReader.read(text, done, SPEAKERS, 0);
    }

    private static List<String> speakers(Reading reading) {
        return reading.messages().stream().map(BetaSpoken::getSpeaker).toList();
    }

    // ---- JSON

    @Test
    void aCompleteJsonReplyIsRead() {
        Reading reading = read(JSON, true);

        assertEquals("Athena starts, Vesper objects.", reading.reasoning());
        assertEquals(List.of("Athena", "Vesper", "Narrator"), speakers(reading));
        assertEquals(PartType.ACTION, reading.messages().get(0).getParts().get(0).getType());
        assertEquals("nods slowly", reading.messages().get(0).getParts().get(0).getText());
        assertEquals(PartType.SPEECH, reading.messages().get(0).getParts().get(1).getType());
        assertEquals("Slow, with {braces} and \"quotes\".", reading.messages().get(1).getParts().get(0).getText());
        assertEquals("", reading.rawText());
        assertEquals(0, reading.dropped());
    }

    @Test
    void aReplyInAMarkdownBlockIsRead() {
        Reading reading = read("```json\n" + JSON + "\n```", true);

        assertEquals(3, reading.messages().size());
    }

    @Test
    void whileStreamingOnlyTheFinishedMessagesAreReturned() {
        int secondStarts = JSON.indexOf("{\"speaker\": \"Vesper\"");
        int secondEnds = JSON.indexOf("]}", secondStarts) + 2;

        assertEquals(List.of("Athena"), speakers(read(JSON.substring(0, secondStarts + 30), false)));
        assertEquals(List.of("Athena"), speakers(read(JSON.substring(0, secondStarts - 1), false)));
        assertEquals(List.of("Athena"), speakers(read(JSON.substring(0, secondEnds - 1), false)));
        assertEquals(List.of("Athena", "Vesper"), speakers(read(JSON.substring(0, secondEnds), false)));
    }

    @Test
    void theReasoningShowsAsItIsWritten() {
        Reading reading = read("{\"reasoning\": \"Athena starts, Ves", false);

        assertEquals("Athena starts, Ves", reading.reasoning());
        assertTrue(reading.messages().isEmpty());
        assertEquals("", reading.rawText());
    }

    @Test
    void aReplyCutAtAnyPositionNeverBreaksAndOnlyGrows() {
        List<String> previous = List.of();
        for (int i = 0; i <= JSON.length(); i++) {
            Reading streaming = read(JSON.substring(0, i), false);
            List<String> now = streaming.messages().stream().map(m -> m.getSpeaker() + m.getParts().size()).toList();

            assertTrue(now.size() >= previous.size(), "messages disappeared at " + i);
            assertEquals(previous, now.subList(0, previous.size()), "messages changed at " + i);
            assertTrue(now.size() <= 3);
            assertEquals("", streaming.rawText(), "raw text at " + i);
            previous = now;

            Reading cut = read(JSON.substring(0, i), true);
            assertTrue(cut.messages().size() >= streaming.messages().size(), "the cut reply lost messages at " + i);
        }
        assertEquals(3, previous.size());
    }

    @Test
    void aReplyCutForGoodKeepsTheMessageThatWasBeingWritten() {
        int cut = JSON.indexOf("The fire") + 4;

        Reading reading = read(JSON.substring(0, cut), true);

        assertEquals(List.of("Athena", "Vesper", "Narrator"), speakers(reading));
        assertEquals("The", reading.messages().get(2).getParts().get(0).getText());
    }

    @Test
    void brokenJsonKeepsWhatWasRead() {
        String broken = JSON.substring(0, JSON.indexOf("{\"speaker\": \"Narrator\"")) + "{\"speaker\": \"Narrator\", \"parts\": [oops";

        Reading reading = read(broken, true);

        assertEquals(List.of("Athena", "Vesper"), speakers(reading));
    }

    @Test
    void theSpeakerIsTheNameTheChatHas() {
        Reading reading = read("{\"messages\": [{\"speaker\": \" athena \", \"parts\": [{\"type\": \"speech\", \"text\": \"Hi\"}]},"
                + "{\"speaker\": \"NARRATOR\", \"parts\": [{\"type\": \"action\", \"text\": \"Silence.\"}]}]}", true);

        assertEquals(List.of("Athena", "Narrator"), speakers(reading));
    }

    @Test
    void anUnknownSpeakerIsKeptAsWritten() {
        Reading reading = read("{\"messages\": [{\"speaker\": \"Mara\", \"parts\": [{\"type\": \"speech\", \"text\": \"Hi\"}]}]}", true);

        assertEquals(List.of("Mara"), speakers(reading));
    }

    @Test
    void anUnknownTypeIsSpeechAndEmptyPartsAndMessagesAreSkipped() {
        Reading reading = read("{\"messages\": [{\"speaker\": \"Athena\", \"parts\": [{\"type\": \"thought\", \"text\": \"Hm\"},"
                + "{\"type\": \"speech\", \"text\": \"  \"}]}, {\"speaker\": \"Vesper\", \"parts\": []}]}", true);

        assertEquals(1, reading.messages().size());
        assertEquals(1, reading.messages().getFirst().getParts().size());
        assertEquals(PartType.SPEECH, reading.messages().getFirst().getParts().getFirst().getType());
    }

    @Test
    void messagesOverTheLimitAreDropped() {
        Reading reading = ReplyReader.read(JSON, true, SPEAKERS, 2);

        assertEquals(List.of("Athena", "Vesper"), speakers(reading));
        assertEquals(1, reading.dropped());
    }

    @Test
    void theLimitAlsoHoldsWhileStreaming() {
        Reading reading = ReplyReader.read(JSON, false, SPEAKERS, 1);

        assertEquals(List.of("Athena"), speakers(reading));
        assertEquals(2, reading.dropped());
    }

    @Test
    void aBareArrayOfMessagesIsRead() {
        String array = JSON.substring(JSON.indexOf('['), JSON.lastIndexOf(']') + 1);

        assertEquals(3, read(array, true).messages().size());
        assertEquals(2, read(array.substring(0, array.indexOf("{\"speaker\": \"Narrator\"")), false).messages().size());
    }

    @Test
    void jsonThatIsNotTheSchemaIsTheRawText() {
        Reading reading = read("{\"answer\": 42}", true);

        assertTrue(reading.messages().isEmpty());
        assertEquals("{\"answer\": 42}", reading.rawText());
    }

    @Test
    void theFinishedMessagesAreFoundByTheirClosingBraces() {
        assertEquals(List.of(), ReplyReader.messageEnds(""));
        assertEquals(List.of(), ReplyReader.messageEnds("{\"messages\": [{\"speaker\": \"A\", \"parts\": [{\"text\": \"}}]]\"}"));
        String one = "{\"messages\": [{\"speaker\": \"A\", \"parts\": [{\"text\": \"}}\\\"\"}]}";
        assertEquals(List.of(one.length() - 1), ReplyReader.messageEnds(one + ", {\"sp"));
        assertEquals(2, ReplyReader.messageEnds("[{\"a\": {}}, {\"b\": 1}, {").size());
    }

    // ---- free text

    @Test
    void freeTextIsReadIntoMessagesAndParts() {
        Reading reading = read("**Athena:** *nods slowly* A strong chapter. *smiles*\n\n**Vesper:** Slow.\nVery slow.\n\n"
                + "**Narrator:** *The fire crackles.*", true);

        assertEquals(List.of("Athena", "Vesper", "Narrator"), speakers(reading));
        List<String> athena = reading.messages().get(0).getParts().stream().map(p -> p.getType() + ":" + p.getText()).toList();
        assertEquals(List.of("ACTION:nods slowly", "SPEECH:A strong chapter.", "ACTION:smiles"), athena);
        assertEquals("Slow.\nVery slow.", reading.messages().get(1).getParts().getFirst().getText());
        assertEquals("", reading.rawText());
        assertEquals("", reading.reasoning());
    }

    @Test
    void theNameMayBeBoldWithTheColonOutside() {
        Reading reading = read("**Athena**: Hello\n**vesper:** Hi", true);

        assertEquals(List.of("Athena", "Vesper"), speakers(reading));
    }

    @Test
    void aBoldWordThatIsNoSpeakerDoesNotStartAMessage() {
        Reading reading = read("**Athena:** She said **Note:** this matters.\n**Note:** also this", true);

        assertEquals(List.of("Athena"), speakers(reading));
        assertTrue(reading.messages().getFirst().getParts().getFirst().getText().contains("**Note:** also this"));
    }

    @Test
    void whileStreamingTheLastMessageOfFreeTextIsHidden() {
        String text = "**Athena:** First.\n\n**Vesper:** Second, still being wri";

        assertEquals(List.of("Athena"), speakers(read(text, false)));
        assertEquals(List.of("Athena", "Vesper"), speakers(read(text, true)));
        assertTrue(read("**Athena:** Fir", false).isEmpty());
    }

    @Test
    void textBeforeTheFirstSpeakerIsTheNarrators() {
        Reading reading = read("Here is what they say:\n\n**Athena:** Hello", true);

        assertEquals(List.of("Narrator", "Athena"), speakers(reading));
    }

    @Test
    void freeTextMessagesOverTheLimitAreDropped() {
        Reading reading = ReplyReader.read("**Athena:** One\n**Vesper:** Two\n**Athena:** Three", true, SPEAKERS, 2);

        assertEquals(List.of("Athena", "Vesper"), speakers(reading));
        assertEquals(1, reading.dropped());
    }

    @Test
    void aBoldActionIsNotAnAction() {
        Reading reading = read("**Athena:** This is **very** good and *really* so", true);

        assertEquals(List.of("SPEECH:This is **very** good and", "ACTION:really", "SPEECH:so"),
                reading.messages().getFirst().getParts().stream().map(p -> p.getType() + ":" + p.getText()).toList());
    }

    // ---- neither

    @Test
    void textWithoutSpeakersIsTheRawTextWhenDone() {
        Reading reading = read("Sorry, I lost my train of thought.", true);

        assertTrue(reading.messages().isEmpty());
        assertEquals("Sorry, I lost my train of thought.", reading.rawText());
    }

    @Test
    void textWithoutSpeakersIsNotShownWhileItStreams() {
        Reading reading = read("Sorry, I lost my", false);

        assertTrue(reading.isEmpty());
    }

    @Test
    void textThatStartsWithABracketIsStillRead() {
        Reading reading = read("[Aside]\n**Athena:** Hello", true);

        assertEquals(List.of("Narrator", "Athena"), speakers(reading));
    }

    @Test
    void nothingIsNothing() {
        assertTrue(read("", true).isEmpty());
        assertTrue(read(null, true).isEmpty());
        assertTrue(read("  \n ", true).isEmpty());
    }

    // ---- into the history

    @Test
    void aReadingBecomesALineOfTheHistory() {
        BetaMessage message = read(JSON, true).toMessage();

        assertEquals("Athena starts, Vesper objects.", message.getReasoning());
        assertEquals(3, message.getSpoken().size());
        assertEquals("", message.getRawText());

        BetaMessage raw = read("Lost.", true).toMessage();

        assertTrue(raw.getSpoken().isEmpty());
        assertEquals("Lost.", raw.getRawText());
    }
}
