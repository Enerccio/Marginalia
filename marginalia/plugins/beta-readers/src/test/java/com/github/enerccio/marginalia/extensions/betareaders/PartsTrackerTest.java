package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.ChatKind;
import com.github.enerccio.marginalia.extensions.betareaders.model.SentPart;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Blocks;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Status;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.StoryPart;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Summaries;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the personas were given and the story they read: built from the story as it is now, from the parts they were given, within
 * the spoiler limit and the first part, with the summaries in place of the oldest parts.
 */
class PartsTrackerTest {

    private static final ToIntFunction<String> TOKENS = text -> (text.length() + 3) / 4;

    private static List<StoryPart> story(String... texts) {
        List<StoryPart> parts = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            parts.add(new StoryPart("p" + (i + 1), i + 1, texts[i]));
        }
        return parts;
    }

    private static BetaChat chat() {
        return new BetaChat("Chat", ChatKind.GROUP);
    }

    /** A message that includes the latest parts: it gives the chat what it was not given. */
    private static BetaMessage give(BetaChat chat, List<StoryPart> branch, String text) {
        return give(chat, branch, 0, 0, text);
    }

    private static BetaMessage give(BetaChat chat, List<StoryPart> branch, int min, int max, String text) {
        BetaMessage message = BetaMessage.user(text);
        message.setSentParts(new ArrayList<>(PartsTracker.unreleased(chat, branch, min, max)));
        chat.getMessages().add(message);
        return message;
    }

    private static Blocks read(BetaChat chat, List<StoryPart> branch) {
        return PartsTracker.blocks(chat, branch, Summaries.NONE, 0, 0, 0, TOKENS);
    }

    /** The story as the model reads it: the blocks of the messages one after the other. */
    private static String text(Blocks blocks) {
        return String.join("\n\n", blocks.byMessage().values());
    }

    @Test
    void aChatThatWasGivenNothingReadsNothing() {
        BetaChat chat = chat();
        chat.getMessages().add(BetaMessage.user("Just a question, no parts"));

        Status status = PartsTracker.status(chat, story("A"), 0, 0);

        assertTrue(status.first());
        assertEquals(1, status.count());
        assertTrue(read(chat, story("A")).isEmpty());
    }

    @Test
    void includingThePartsGivesTheWholeStory() {
        BetaChat chat = chat();
        List<StoryPart> story = story("He came in.", "She left.");

        BetaMessage message = give(chat, story, "Hi");

        assertEquals(List.of("p1", "p2"), message.getSentParts().stream().map(SentPart::getId).toList());
        assertEquals(2, message.getSentParts().get(1).getNumber());
        Blocks block = read(chat, story);
        assertEquals("[The story the author shows you]\n[Part 1]\nHe came in.\n[Part 2]\nShe left.", text(block));
        assertEquals(2, block.included());
        assertEquals(0, block.omitted());
        Status status = PartsTracker.status(chat, story, 0, 0);
        assertFalse(status.first());
        assertFalse(status.pending());
    }

    @Test
    void anEmptyStoryGivesNothing() {
        assertTrue(PartsTracker.unreleased(chat(), story(), 0, 0).isEmpty());
        assertTrue(PartsTracker.unreleased(chat(), story("  ", ""), 0, 0).isEmpty());
    }

    @Test
    void aNewPartIsNotReadUntilItIsIncluded() {
        BetaChat chat = chat();
        give(chat, story("A", "B"), "Hi");
        List<StoryPart> longer = story("A", "B", "C", "D");

        Status status = PartsTracker.status(chat, longer, 0, 0);

        assertEquals(2, status.added());
        // the author can wait: the personas read what they were given
        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB", text(read(chat, longer)));

        BetaMessage more = give(chat, longer, "More");

        // the new parts are in front of the message that gave them: what was read before is the same text
        Blocks now = read(chat, longer);
        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB", now.byMessage().values().iterator().next());
        assertEquals("[More of the story the author shows you]\n[Part 3]\nC\n[Part 4]\nD", now.byMessage().get(more.getId()));
        assertFalse(PartsTracker.status(chat, longer, 0, 0).pending());
    }

    @Test
    void anEditedPartIsReadAsItIsNow() {
        BetaChat chat = chat();
        give(chat, story("A", "B"), "Hi");
        List<StoryPart> edited = story("A", "B edited");

        // nothing to include: the edit is simply the story now, once
        assertFalse(PartsTracker.status(chat, edited, 0, 0).pending());
        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB edited", text(read(chat, edited)));
    }

    @Test
    void aPartThatIsGoneFromTheBranchIsNotRead() {
        BetaChat chat = chat();
        give(chat, story("A", "B", "C"), "Hi");

        assertEquals("[The story the author shows you]\n[Part 1]\nA", text(read(chat, story("A"))));
    }

    @Test
    void aSwitchedBranchIsReadFromWhatWasGiven() {
        BetaChat chat = chat();
        give(chat, story("A", "B", "C"), "Hi");
        List<StoryPart> other = List.of(new StoryPart("p1", 1, "A"), new StoryPart("q2", 2, "B2"), new StoryPart("q3", 3, "C2"));

        // the parts of the other branch were not given
        assertEquals(2, PartsTracker.status(chat, other, 0, 0).added());
        assertEquals("[The story the author shows you]\n[Part 1]\nA", text(read(chat, other)));

        give(chat, other, "Other branch");

        assertEquals("[The story the author shows you]\n[Part 1]\nA\n\n[More of the story the author shows you]\n[Part 2]\nB2\n[Part 3]\nC2",
                text(read(chat, other)));
        // and back: what was given stays given
        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB\n[Part 3]\nC",
                text(read(chat, story("A", "B", "C"))));
    }

    @Test
    void deletingTheMessageTakesItsPartsBack() {
        BetaChat chat = chat();
        give(chat, story("A", "B"), "Hi");
        BetaMessage second = give(chat, story("A", "B", "C"), "More");
        assertFalse(PartsTracker.status(chat, story("A", "B", "C"), 0, 0).pending());

        chat.getMessages().remove(second);

        assertEquals(1, PartsTracker.status(chat, story("A", "B", "C"), 0, 0).added());
        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB", text(read(chat, story("A", "B", "C"))));
    }

    @Test
    void deletingTheFirstMessageMakesTheChatReadNothingAgain() {
        BetaChat chat = chat();
        BetaMessage first = give(chat, story("A"), "Hi");

        chat.getMessages().remove(first);

        assertTrue(PartsTracker.status(chat, story("A"), 0, 0).first());
        assertTrue(read(chat, story("A")).isEmpty());
    }

    @Test
    void thePersonasDoNotReadPastTheSpoilerLimit() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D");

        give(chat, story, 0, 2, "Hi");

        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB",
                text(PartsTracker.blocks(chat, story, Summaries.NONE, 0, 2, 0, TOKENS)));
        assertFalse(PartsTracker.status(chat, story, 0, 2).pending());
        assertEquals(2, PartsTracker.status(chat, story, 0, 4).added());
    }

    @Test
    void aLowerSpoilerLimitTakesTheLaterPartsOutOfWhatIsRead() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D");
        give(chat, story, "Hi");

        // they were given all four, they read two: no resend, the story is simply built from the limit
        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB",
                text(PartsTracker.blocks(chat, story, Summaries.NONE, 0, 2, 0, TOKENS)));
        // raised again: what they were given is read again
        assertEquals(4, PartsTracker.blocks(chat, story, Summaries.NONE, 0, 0, 0, TOKENS).included());
    }

    @Test
    void thePersonasStartFromAPartAndNothingBeforeItIsRead() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D");

        BetaMessage message = give(chat, story, 3, 0, "Hi");

        assertEquals(List.of("p3", "p4"), message.getSentParts().stream().map(SentPart::getId).toList());
        assertEquals(2, PartsTracker.status(chat(), story, 3, 0).count());
        assertEquals("[The story the author shows you]\n[Part 3]\nC\n[Part 4]\nD",
                text(PartsTracker.blocks(chat, story, Summaries.NONE, 3, 0, 0, TOKENS)));
        // a first part chosen later hides the parts before it from a chat that was given them
        BetaChat wide = chat();
        give(wide, story, "Hi");
        assertEquals("[The story the author shows you]\n[Part 3]\nC\n[Part 4]\nD",
                text(PartsTracker.blocks(wide, story, Summaries.NONE, 3, 0, 0, TOKENS)));
    }

    @Test
    void theRangeHasTwoEnds() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D", "E");
        give(chat, story, 2, 4, "Hi");

        assertEquals("[The story the author shows you]\n[Part 2]\nB\n[Part 3]\nC\n[Part 4]\nD",
                text(PartsTracker.blocks(chat, story, Summaries.NONE, 2, 4, 0, TOKENS)));
        assertEquals(1, PartsTracker.status(chat, story, 2, 5).added());
    }

    @Test
    void theSummariesStandForTheOldestParts() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D");
        give(chat, story, "Hi");
        Summaries summaries = new Summaries(List.of("Summary one.", "Summary two."), "p2");

        Blocks block = PartsTracker.blocks(chat, story, summaries, 0, 0, 0, TOKENS);

        assertEquals("[The story the author shows you]\nSummaries of the story so far:\nSummary one.\n\nSummary two."
                + "\n[Part 3]\nC\n[Part 4]\nD", text(block));
        assertEquals(2, block.included());
        // the story is built again from what is there: no summaries, the parts
        assertEquals(4, read(chat, story).included());
    }

    @Test
    void aSummaryMadeLaterIsUsedByAChatThatWasGivenThePartsBefore() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C");
        give(chat, story, "Hi");

        assertTrue(text(read(chat, story)).contains("[Part 1]\nA"));

        Blocks later = PartsTracker.blocks(chat, story, new Summaries(List.of("Summary."), "p2"), 0, 0, 0, TOKENS);

        assertEquals("[The story the author shows you]\nSummaries of the story so far:\nSummary.\n[Part 3]\nC", text(later));
    }

    @Test
    void aSummaryThatEndsBeyondTheLimitOrWhatWasGivenIsNotUsed() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C");
        give(chat, story, 0, 2, "Hi");

        Blocks block = PartsTracker.blocks(chat, story, new Summaries(List.of("Summary."), "p3"), 0, 2, 0, TOKENS);

        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB", text(block));
    }

    @Test
    void theSummariesAreNotUsedWhenTheStoryDoesNotStartAtTheBeginning() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D", "E");
        give(chat, story, 3, 4, "Hi");
        Summaries summaries = new Summaries(List.of("Summary."), "p2");

        assertEquals("[The story the author shows you]\n[Part 3]\nC\n[Part 4]\nD",
                text(PartsTracker.blocks(chat, story, summaries, 3, 4, 0, TOKENS)));

        BetaChat fromStart = chat();
        give(fromStart, story, 1, 4, "Hi");
        assertEquals("[The story the author shows you]\nSummaries of the story so far:\nSummary.\n[Part 3]\nC\n[Part 4]\nD",
                text(PartsTracker.blocks(fromStart, story, summaries, 1, 4, 0, TOKENS)));
    }

    @Test
    void whatDoesNotFitTheBudgetIsLeftOutOldestFirstAndSaidSo() {
        BetaChat chat = chat();
        String text = "x".repeat(400);
        List<StoryPart> story = story(text, text, text, text);
        give(chat, story, "Hi");

        // a part is about 100 tokens plus the label: room for two
        Blocks block = PartsTracker.blocks(chat, story, Summaries.NONE, 0, 0, 250, TOKENS);

        assertEquals(2, block.included());
        assertEquals(2, block.omitted());
        assertTrue(text(block).contains("(2 parts of the story did not fit and are left out.)"));
        assertTrue(text(block).contains("[Part 3]"));
        assertTrue(text(block).contains("[Part 4]"));
        assertFalse(text(block).contains("[Part 2]"));
    }

    @Test
    void theSummariesTakeTheirTokensFromTheBudget() {
        BetaChat chat = chat();
        String text = "x".repeat(400);
        List<StoryPart> story = story(text, text, text);
        give(chat, story, "Hi");
        Summaries summaries = new Summaries(List.of("s".repeat(400)), "p1");

        Blocks block = PartsTracker.blocks(chat, story, summaries, 0, 0, 250, TOKENS);

        // the summary uses 100 tokens: of the 150 left one part fits
        assertEquals(1, block.included());
        assertEquals(1, block.omitted());
        assertTrue(text(block).contains("[Part 3]"));
    }

    @Test
    void aBudgetTheSummariesUseUpLeavesNoPartRatherThanAllOfThem() {
        BetaChat chat = chat();
        String text = "x".repeat(400);
        List<StoryPart> story = story(text, text, text);
        give(chat, story, "Hi");
        Summaries summaries = new Summaries(List.of("s".repeat(400)), "p1");

        Blocks block = PartsTracker.blocks(chat, story, summaries, 0, 0, 100, TOKENS);

        assertEquals(0, block.included());
        assertEquals(2, block.omitted());
    }

    @Test
    void whatTheChatWasGivenIsWrittenAsRanges() {
        BetaChat chat = chat();
        assertEquals("", PartsTracker.read(chat));
        BetaMessage message = BetaMessage.user("x");
        message.getSentParts().add(new SentPart("p1", 1));
        message.getSentParts().add(new SentPart("p2", 2));
        message.getSentParts().add(new SentPart("p5", 5));
        chat.getMessages().add(message);

        assertEquals("1-2, 5", PartsTracker.read(chat));
    }

    @Test
    void numbersAreWrittenAsRanges() {
        assertEquals("41-43, 50", PartsTracker.ranges(List.of(43, 41, 50, 42)));
        assertEquals("7", PartsTracker.ranges(List.of(7)));
        assertEquals("1-2, 4-5, 9", PartsTracker.ranges(List.of(1, 2, 4, 5, 9)));
        assertEquals("", PartsTracker.ranges(List.of()));
    }
}
