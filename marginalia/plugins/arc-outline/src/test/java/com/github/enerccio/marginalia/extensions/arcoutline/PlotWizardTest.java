package com.github.enerccio.marginalia.extensions.arcoutline;

import com.github.enerccio.marginalia.extensions.arcoutline.model.WizardChat;
import com.github.enerccio.marginalia.extensions.arcoutline.model.WizardData;
import com.github.enerccio.marginalia.extensions.arcoutline.model.WizardMessage;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.StoryMessage;
import com.github.enerccio.marginalia.extensions.arcoutline.service.WizardContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What of the book and the chat the plot wizard sends, and how chats are branched and summarized.
 */
class PlotWizardTest {

    private static StoryMessage part(int order, long tokens) {
        return new StoryMessage(order, order, "text " + order, tokens);
    }

    private static WizardMessage user(String text) {
        return new WizardMessage(true, text, null);
    }

    private static WizardMessage model(String text) {
        return new WizardMessage(false, text, null);
    }

    private static WizardMessage summary(String text) {
        WizardMessage message = model(text);
        message.setSummary(true);
        return message;
    }

    private static long tokens(String text) {
        return text.length();
    }

    @Test
    void theNewestPartsThatFitTheBudgetAreSentOldestFirst() {
        List<StoryMessage> parts = List.of(part(1, 40), part(2, 30), part(3, 30), part(4, 30));
        List<StoryMessage> chosen = WizardContext.newestThatFit(parts, 70);
        assertEquals(List.of(3, 4), chosen.stream().map(StoryMessage::order).toList());
    }

    @Test
    void aPartIsNotCutAndOlderOnesAreNotTakenInsteadOfIt() {
        // the third part is too big for what is left, the second would fit but must not be sent without the third
        List<StoryMessage> parts = List.of(part(1, 10), part(2, 10), part(3, 50), part(4, 10));
        assertEquals(List.of(4), WizardContext.newestThatFit(parts, 30).stream().map(StoryMessage::order).toList());
    }

    @Test
    void aThirdOfWhatTheModelTakesIsTheDefaultForTheStory() {
        assertEquals(1000, WizardContext.defaultStoryTokens(3000));
        assertEquals(0, WizardContext.defaultStoryTokens(0));
    }

    @Test
    void thePartsAreUnderTheirPlaceInTheStory() {
        String text = WizardContext.storyText(List.of(part(3, 1), part(7, 1)));
        assertEquals("--- Part 3 ---\ntext 3\n\n--- Part 7 ---\ntext 7", text);
    }

    @Test
    void blankSummariesAreLeftOut() {
        assertEquals("one\n\ntwo", WizardContext.summariesText(java.util.Arrays.asList("one ", null, " ", "two")));
    }

    @Test
    void theModelGetsTheNewestMessagesThatFit() {
        List<WizardMessage> chat = List.of(user("aaaaaaaaaa"), model("bbbbbbbbbb"), user("cccccccccc"), model("dddddddddd"), user("eeeeeeeeee"));
        List<WizardMessage> chosen = WizardContext.historyThatFits(chat, 30, PlotWizardTest::tokens);
        // three messages fit; the chat has to start with the author
        assertEquals(List.of("cccccccccc", "dddddddddd", "eeeeeeeeee"), chosen.stream().map(WizardMessage::getText).toList());
    }

    @Test
    void aChatThatWouldStartWithTheModelStartsWithTheNextMessageOfTheAuthor() {
        List<WizardMessage> chat = List.of(user("aaaaaaaaaa"), model("bbbbbbbbbb"), user("cccccccccc"));
        List<WizardMessage> chosen = WizardContext.historyThatFits(chat, 20, PlotWizardTest::tokens);
        assertEquals(List.of("cccccccccc"), chosen.stream().map(WizardMessage::getText).toList());
    }

    @Test
    void theNewestMessageIsKeptEvenWhenItAloneIsTooLong() {
        List<WizardMessage> chosen = WizardContext.historyThatFits(List.of(user("a very long message of the author")), 5, PlotWizardTest::tokens);
        assertEquals(1, chosen.size());
    }

    @Test
    void afterASummaryTheModelGetsTheSummaryAndWhatFollows() {
        List<WizardMessage> chat = List.of(user("q1"), model("a1"), summary("S1"), user("q2"), model("a2"), summary("S2"), user("q3"));
        assertEquals("S2", WizardContext.lastSummary(chat));
        assertEquals(List.of("q3"), WizardContext.sinceSummary(chat).stream().map(WizardMessage::getText).toList());
    }

    @Test
    void withoutASummaryTheModelGetsTheWholeChat() {
        List<WizardMessage> chat = List.of(user("q1"), model("a1"));
        assertNull(WizardContext.lastSummary(chat));
        assertEquals(2, WizardContext.sinceSummary(chat).size());
        assertEquals("system", WizardContext.withSummary("system", null));
        assertEquals("system", WizardContext.withSummary("system", "  "));
    }

    @Test
    void theSummaryIsPartOfTheSystemPrompt() {
        String system = WizardContext.withSummary("system", " the author picked option B ");
        assertTrue(system.startsWith("system\n\n"));
        assertTrue(system.endsWith("the author picked option B"));
    }

    @Test
    void whatTheAuthorAsksTheSummaryToKeepGoesUnderTheInstructions() {
        assertEquals("base", WizardContext.summaryPrompt("base", null));
        assertEquals("base", WizardContext.summaryPrompt("base", "  "));
        String prompt = WizardContext.summaryPrompt("base\n", " keep the twist ");
        assertTrue(prompt.startsWith("base\n\n### What the author asks the summary to keep or stress\nkeep the twist\n"));
    }

    @Test
    void everyGroupOfToolsHasCommandsAndNoneOfThemDeletesFromTheBookExceptThePlanner() {
        for (var group : com.github.enerccio.marginalia.extensions.arcoutline.model.WizardOptions.ToolGroup.values()) {
            List<String> names = com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.toolNames(group);
            assertFalse(names.isEmpty(), group.name());
            if (group != com.github.enerccio.marginalia.extensions.arcoutline.model.WizardOptions.ToolGroup.PLANNER) {
                names.forEach(name -> assertFalse(name.startsWith("delete-"), name + " asks a question nobody can answer there"));
            }
        }
    }

    @Test
    void theToolsAreExplainedToTheModelWithTheHintsOfTheNameToolsUnder() {
        String system = WizardContext.withTools("system\n", "### Tools\nuse them", " hint ");
        assertEquals("system\n\n### Tools\nuse them\n\nhint", system);
        assertEquals("system\n\n### Tools\nuse them", WizardContext.withTools("system", "### Tools\nuse them", null));
    }

    @Test
    void anEarlierAnswerIsSentWithItsToolCallsAndResultsSoTheModelSeesWhatItDid() {
        var made = new com.github.enerccio.marginalia.extensions.arcoutline.model.WizardCall("create-lore-entry", "{\"name\":\"Bracelet\"}", "{\"entry\":10}", false);
        WizardMessage answer = new WizardMessage(false, "Done.", null);
        answer.setCalls(List.of(made));
        var payload = WizardContext.toPayload(List.of(user("make the entry"), answer, user("now the arc")));

        assertEquals(5, payload.size());
        assertEquals(com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole.USER, payload.get(0).getRole());
        var calls = payload.get(1);
        assertEquals(com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole.ASSISTANT, calls.getRole());
        assertEquals(1, calls.getToolCalls().size());
        assertEquals("create-lore-entry", calls.getToolCalls().getFirst().getName());
        assertEquals("{\"name\":\"Bracelet\"}", calls.getToolCalls().getFirst().getArguments());
        var result = payload.get(2);
        assertEquals(com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole.TOOL, result.getRole());
        assertEquals(calls.getToolCalls().getFirst().getId(), result.getToolCallId());
        assertEquals("{\"entry\":10}", result.getContent());
        assertEquals("Done.", payload.get(3).getContent());
        assertEquals("now the arc", payload.get(4).getContent());
    }

    @Test
    void aLongResultIsCutAndAnAnswerOfCallsOnlyHasNoEmptyTextMessage() {
        var made = new com.github.enerccio.marginalia.extensions.arcoutline.model.WizardCall("get-arc-outline", "", "x".repeat(5000), false);
        WizardMessage answer = new WizardMessage(false, " ", null);
        answer.setCalls(List.of(made));
        var payload = WizardContext.toPayload(List.of(user("show it"), answer));

        // the author's message, the call and its result: no empty answer after them
        assertEquals(3, payload.size());
        assertEquals("{}", payload.get(1).getToolCalls().getFirst().getArguments());
        assertTrue(payload.get(2).getContent().length() < 2100);
        assertTrue(payload.get(2).getContent().endsWith("...[cut]"));
        // what is sent is what is counted
        assertTrue(WizardContext.sentText(answer).length() < 2200);
    }

    @Test
    void anEarlierAnswerTellsTheModelWhichToolsItUsed() {
        var ok = new com.github.enerccio.marginalia.extensions.arcoutline.model.WizardCall("add-planned-scene", "{}", "{}", false);
        var bad = new com.github.enerccio.marginalia.extensions.arcoutline.model.WizardCall("update-lore-entry", "{}", "no", true);
        assertEquals("text", WizardContext.withCalls("text", List.of()));
        assertEquals("text\n\n[Tools used in this answer: add-planned-scene, update-lore-entry (failed)]", WizardContext.withCalls("text", List.of(ok, bad)));
    }

    @Test
    void aBranchIsACopyThatHasNothingToDoWithTheOriginal() {
        WizardChat chat = new WizardChat();
        chat.getMessages().addAll(List.of(user("q1"), model("a1"), summary("S"), user("q2")));

        WizardChat all = chat.branch("copy", 99);
        assertEquals(4, all.getMessages().size());
        assertNotEquals(chat.getUuid(), all.getUuid());
        assertTrue(all.getMessages().get(2).isSummary());

        WizardChat upTo = chat.branch("part", 2);
        assertEquals(List.of("q1", "a1"), upTo.getMessages().stream().map(WizardMessage::getText).toList());

        all.getMessages().get(0).setText("changed");
        all.getMessages().removeLast();
        assertEquals("q1", chat.getMessages().get(0).getText());
        assertEquals(4, chat.getMessages().size());
    }

    @Test
    void theActiveChatIsFoundByItsUuidElseTheFirstOne() {
        WizardData data = new WizardData();
        assertNull(data.activeChat());
        WizardChat first = new WizardChat();
        WizardChat second = new WizardChat();
        data.getChats().add(first);
        data.getChats().add(second);
        assertSame(first, data.activeChat());
        data.setActive(second.getUuid());
        assertSame(second, data.activeChat());
        data.setActive("gone");
        assertSame(first, data.activeChat());
    }
}
