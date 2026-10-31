package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.ChatKind;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Plan;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Status;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.StoryPart;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Summaries;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the personas have read: the context of the first message, the parts that are new or changed, a branch that was switched, the
 * spoiler limit, and what a deleted message gives back.
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

    /** Sends what the plan says, as the message that stores it does. */
    private static BetaMessage send(BetaChat chat, Plan plan, String text) {
        BetaMessage message = BetaMessage.user(text);
        message.setContext(plan.text());
        message.setSentParts(new ArrayList<>(plan.sent()));
        message.setGoneParts(new ArrayList<>(plan.gone()));
        chat.getMessages().add(message);
        return message;
    }

    private static Plan plan(BetaChat chat, List<StoryPart> branch) {
        return PartsTracker.plan(chat, branch, Summaries.NONE, 0, 0, TOKENS);
    }

    @Test
    void theFirstMessageSendsTheWholeStory() {
        BetaChat chat = chat();
        List<StoryPart> story = story("He came in.", "She left.");

        Status status = PartsTracker.status(chat, story, 0);
        Plan plan = plan(chat, story);

        assertTrue(status.first());
        assertEquals(2, status.count());
        assertTrue(plan.first());
        assertEquals("[The story the author shows you]\n[Part 1]\nHe came in.\n[Part 2]\nShe left.", plan.text());
        assertEquals(2, plan.included());
        assertEquals(0, plan.omitted());
        assertEquals(List.of("p1", "p2"), plan.sent().stream().map(part -> part.getId()).toList());
        assertEquals(2, plan.sent().get(1).getNumber());
    }

    @Test
    void anEmptyStorySendsNothing() {
        assertTrue(plan(chat(), story()).isEmpty());
        assertTrue(plan(chat(), story("  ", "")).isEmpty());
    }

    @Test
    void nothingIsNewRightAfterSending() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B");
        send(chat, plan(chat, story), "Hi");

        Status status = PartsTracker.status(chat, story, 0);

        assertFalse(status.first());
        assertFalse(status.pending());
        assertTrue(plan(chat, story).isEmpty());
    }

    @Test
    void aNewPartIsSentAsNewStuff() {
        BetaChat chat = chat();
        send(chat, plan(chat, story("A", "B")), "Hi");
        List<StoryPart> longer = story("A", "B", "C", "D");

        Status status = PartsTracker.status(chat, longer, 0);
        Plan plan = plan(chat, longer);

        assertEquals(2, status.added());
        assertEquals(0, status.changed());
        assertFalse(plan.first());
        assertEquals("[New in the story since you last read it]\n[Part 3]\nC\n[Part 4]\nD", plan.text());
        assertEquals(List.of("p3", "p4"), plan.sent().stream().map(part -> part.getId()).toList());
    }

    @Test
    void aChangedPartIsSentAgainAndSaysSo() {
        BetaChat chat = chat();
        send(chat, plan(chat, story("A", "B")), "Hi");
        List<StoryPart> edited = story("A", "B edited");

        Status status = PartsTracker.status(chat, edited, 0);
        Plan plan = plan(chat, edited);

        assertEquals(1, status.changed());
        assertEquals(0, status.added());
        assertEquals("[New in the story since you last read it]\n[Part 2 (changed since you read it)]\nB edited", plan.text());
    }

    @Test
    void whatWasSentTwiceIsSentOnce() {
        BetaChat chat = chat();
        send(chat, plan(chat, story("A")), "Hi");
        List<StoryPart> edited = story("A edited");
        send(chat, plan(chat, edited), "Again");

        assertFalse(PartsTracker.status(chat, edited, 0).pending());
    }

    @Test
    void aMessageThatSentNothingKeepsTheFirstMessageForLater() {
        BetaChat chat = chat();
        chat.getMessages().add(BetaMessage.user("Just a question, no parts"));

        assertTrue(PartsTracker.status(chat, story("A"), 0).first());
        assertTrue(plan(chat, story("A")).first());
    }

    @Test
    void deletingTheMessageGivesItsPartsBack() {
        BetaChat chat = chat();
        send(chat, plan(chat, story("A", "B")), "Hi");
        BetaMessage second = send(chat, plan(chat, story("A", "B", "C")), "More");
        assertFalse(PartsTracker.status(chat, story("A", "B", "C"), 0).pending());

        chat.getMessages().remove(second);

        Status status = PartsTracker.status(chat, story("A", "B", "C"), 0);
        assertEquals(1, status.added());
        assertTrue(plan(chat, story("A", "B", "C")).text().contains("[Part 3]\nC"));
    }

    @Test
    void deletingTheFirstMessageMakesTheNextOneTheFirstAgain() {
        BetaChat chat = chat();
        BetaMessage first = send(chat, plan(chat, story("A")), "Hi");

        chat.getMessages().remove(first);

        assertTrue(PartsTracker.status(chat, story("A"), 0).first());
    }

    @Test
    void aSwitchedBranchReportsTheGonePartsOnce() {
        BetaChat chat = chat();
        List<StoryPart> original = story("A", "B", "C");
        send(chat, plan(chat, original), "Hi");
        List<StoryPart> other = List.of(new StoryPart("p1", 1, "A"), new StoryPart("q2", 2, "B2"), new StoryPart("q3", 3, "C2"));

        Status status = PartsTracker.status(chat, other, 0);
        Plan plan = plan(chat, other);

        assertEquals(2, status.added());
        assertEquals(2, status.gone());
        assertEquals(List.of("p2", "p3"), plan.gone());
        assertTrue(plan.text().contains("(The story was changed: the parts 2-3 you read are not there any more. What follows replaces them.)"));
        assertTrue(plan.text().contains("[Part 2]\nB2"));
        assertTrue(plan.text().contains("[Part 3]\nC2"));

        send(chat, plan, "Other branch");

        assertFalse(PartsTracker.status(chat, other, 0).pending());
        assertTrue(plan(chat, other).isEmpty());
    }

    @Test
    void goingBackToTheFirstBranchSendsItsPartsAgain() {
        BetaChat chat = chat();
        List<StoryPart> original = story("A", "B");
        List<StoryPart> other = List.of(new StoryPart("p1", 1, "A"), new StoryPart("q2", 2, "B2"));
        send(chat, plan(chat, original), "Hi");
        send(chat, plan(chat, other), "Other");

        Plan back = plan(chat, original);

        assertEquals(List.of("q2"), back.gone());
        assertTrue(back.text().contains("[Part 2]\nB"));
    }

    @Test
    void aPartThatWasOnlyRemovedIsGoneWithoutNewStuff() {
        BetaChat chat = chat();
        send(chat, plan(chat, story("A", "B")), "Hi");

        Plan plan = plan(chat, story("A"));

        assertEquals("[New in the story since you last read it]\n(The story was changed: the part 2 you read is not there any more.)",
                plan.text());
        assertEquals(List.of("p2"), plan.gone());
    }

    @Test
    void thePersonasDoNotReadPastTheSpoilerLimit() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D");

        Plan first = PartsTracker.plan(chat, story, Summaries.NONE, 2, 0, TOKENS);

        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB", first.text());
        assertEquals(2, PartsTracker.status(chat, story, 2).count());

        send(chat, first, "Hi");

        assertFalse(PartsTracker.status(chat, story, 2).pending());
        Plan wider = PartsTracker.plan(chat, story, Summaries.NONE, 3, 0, TOKENS);
        assertEquals("[New in the story since you last read it]\n[Part 3]\nC", wider.text());
        // a part beyond the limit that was sent earlier is not gone: it is still in the story
        assertEquals(0, PartsTracker.status(chat, story, 1).gone());
    }

    @Test
    void theFirstMessageSendsTheSummariesAndThePartsAfterTheNewest() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D");
        Summaries summaries = new Summaries(List.of("Summary one.", "Summary two."), "p2");

        Plan plan = PartsTracker.plan(chat, story, summaries, 0, 0, TOKENS);

        assertEquals("[The story the author shows you]\nSummaries of the story so far:\nSummary one.\n\nSummary two."
                + "\n[Part 3]\nC\n[Part 4]\nD", plan.text());
        assertEquals(2, plan.included());
        // the parts the summaries stand for count as sent, so they are not new in the next message
        assertEquals(4, plan.sent().size());
        send(chat, plan, "Hi");
        assertFalse(PartsTracker.status(chat, story, 0).pending());
    }

    @Test
    void aSummaryThatEndsBeyondTheLimitIsNotUsed() {
        Plan plan = PartsTracker.plan(chat(), story("A", "B", "C"), new Summaries(List.of("Summary."), "p3"), 2, 0, TOKENS);

        assertEquals("[The story the author shows you]\n[Part 1]\nA\n[Part 2]\nB", plan.text());
    }

    @Test
    void anEditedPartInsideTheSummariesIsSentAsChanged() {
        BetaChat chat = chat();
        Summaries summaries = new Summaries(List.of("Summary."), "p2");
        send(chat, PartsTracker.plan(chat, story("A", "B", "C"), summaries, 0, 0, TOKENS), "Hi");

        Plan plan = plan(chat, story("A edited", "B", "C"));

        assertEquals("[New in the story since you last read it]\n[Part 1 (changed since you read it)]\nA edited", plan.text());
    }

    @Test
    void whatDoesNotFitTheBudgetIsLeftOutOldestFirstButCountsAsSent() {
        BetaChat chat = chat();
        String text = "x".repeat(400);
        List<StoryPart> story = story(text, text, text, text);

        // a part is about 100 tokens plus the label: room for two
        Plan plan = PartsTracker.plan(chat, story, Summaries.NONE, 0, 250, TOKENS);

        assertEquals(2, plan.included());
        assertEquals(2, plan.omitted());
        assertTrue(plan.text().contains("[Part 3]"));
        assertTrue(plan.text().contains("[Part 4]"));
        assertFalse(plan.text().contains("[Part 2]"));
        assertEquals(4, plan.sent().size());
    }

    @Test
    void theSummariesTakeTheirTokensFromTheBudget() {
        String text = "x".repeat(400);
        Summaries summaries = new Summaries(List.of("s".repeat(400)), "p1");

        Plan plan = PartsTracker.plan(chat(), story(text, text, text), summaries, 0, 250, TOKENS);

        // the summary uses 100 tokens: of the 150 left one part fits
        assertEquals(1, plan.included());
        assertEquals(1, plan.omitted());
        assertTrue(plan.text().contains("[Part 3]"));
    }

    @Test
    void newPartsAreAlsoLimitedByTheBudget() {
        BetaChat chat = chat();
        send(chat, plan(chat, story("A")), "Hi");
        String text = "x".repeat(400);

        Plan plan = PartsTracker.plan(chat, story("A", text, text, text), Summaries.NONE, 0, 150, TOKENS);

        assertEquals(1, plan.included());
        assertEquals(2, plan.omitted());
        assertTrue(plan.text().contains("[Part 4]"));
        assertEquals(3, plan.sent().size());
    }

    @Test
    void theHashFollowsTheText() {
        assertEquals(PartsTracker.hash("same"), PartsTracker.hash("same"));
        assertNotEquals(PartsTracker.hash("same"), PartsTracker.hash("other"));
        assertEquals(16, PartsTracker.hash("x").length());
    }

    @Test
    void numbersAreWrittenAsRanges() {
        assertEquals("41-43, 50", PartsTracker.ranges(List.of(43, 41, 50, 42)));
        assertEquals("7", PartsTracker.ranges(List.of(7)));
        assertEquals("1-2, 4-5, 9", PartsTracker.ranges(List.of(1, 2, 4, 5, 9)));
        assertEquals("", PartsTracker.ranges(List.of()));
    }

    @Test
    void thePersonasStartFromAPartAndNothingBeforeItIsSent() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D");

        Plan first = PartsTracker.plan(chat, story, Summaries.NONE, 3, 0, 0, TOKENS);

        assertEquals("[The story the author shows you]\n[Part 3]\nC\n[Part 4]\nD", first.text());
        assertEquals(List.of("p3", "p4"), first.sent().stream().map(part -> part.getId()).toList());
        assertEquals(2, PartsTracker.status(chat, story, 3, 0).count());
        assertEquals(0, PartsTracker.status(chat, story, 3, 0).gone());
    }

    @Test
    void theRangeHasTwoEndsAndTheSummariesAreNotUsedWhenTheStoryDoesNotStartAtTheBeginning() {
        List<StoryPart> story = story("A", "B", "C", "D", "E");
        Summaries summaries = new Summaries(List.of("Summary."), "p2");

        Plan fromThree = PartsTracker.plan(chat(), story, summaries, 3, 4, 0, TOKENS);
        Plan fromStart = PartsTracker.plan(chat(), story, summaries, 1, 4, 0, TOKENS);

        assertEquals("[The story the author shows you]\n[Part 3]\nC\n[Part 4]\nD", fromThree.text());
        assertEquals("[The story the author shows you]\nSummaries of the story so far:\nSummary.\n[Part 3]\nC\n[Part 4]\nD", fromStart.text());
    }

    @Test
    void widerRangesSendWhatIsNewInIt() {
        BetaChat chat = chat();
        List<StoryPart> story = story("A", "B", "C", "D");
        send(chat, PartsTracker.plan(chat, story, Summaries.NONE, 2, 3, 0, TOKENS), "Hi");

        Plan wider = PartsTracker.plan(chat, story, Summaries.NONE, 2, 4, 0, TOKENS);

        assertEquals("[New in the story since you last read it]\n[Part 4]\nD", wider.text());
    }

    @Test
    void whatTheChatHasReadIsWrittenAsRanges() {
        BetaChat chat = chat();
        assertEquals("", PartsTracker.read(chat));

        send(chat, plan(chat, story("A", "B", "C", "D", "E")), "Hi");
        chat.getMessages().clear();
        List<StoryPart> gapped = story("A", "B", "C", "D", "E");
        BetaMessage message = BetaMessage.user("x");
        message.getSentParts().add(new com.github.enerccio.marginalia.extensions.betareaders.model.SentPart("p1", "h", 1));
        message.getSentParts().add(new com.github.enerccio.marginalia.extensions.betareaders.model.SentPart("p2", "h", 2));
        message.getSentParts().add(new com.github.enerccio.marginalia.extensions.betareaders.model.SentPart("p5", "h", 5));
        chat.getMessages().add(message);

        assertEquals("1-2, 5", PartsTracker.read(chat));
        assertEquals(5, gapped.size());
    }
}
