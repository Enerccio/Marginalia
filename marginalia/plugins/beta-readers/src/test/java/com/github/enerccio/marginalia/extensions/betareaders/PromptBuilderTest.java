package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.github.enerccio.marginalia.extensions.betareaders.service.PromptBuilder;
import com.github.enerccio.marginalia.extensions.betareaders.service.PromptBuilder.Options;
import com.github.enerccio.marginalia.extensions.betareaders.service.PromptBuilder.Prompt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The request of a chat: the system prompt with the description of the answer, the history, and the roster of the personas only in
 * the last message of the user.
 */
class PromptBuilderTest {

    private static final Options JSON = new Options(true, 0, 0, null);
    private static final Options TEXT = new Options(false, 0, 0, null);

    private static Persona athena() {
        return new Persona("Athena", "The Analytical Archivist.");
    }

    private static Persona vesper() {
        return new Persona("Vesper", "The Snarky Realist.");
    }

    private static BetaChat group(Persona... personas) {
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        for (Persona persona : personas) {
            chat.addPersona(persona);
        }
        return chat;
    }

    private static BetaMessage reply(String reasoning, String speaker, String speech) {
        return BetaMessage.assistant(reasoning, List.of(new BetaSpoken(speaker,
                List.of(new BetaPart(PartType.ACTION, "nods"), new BetaPart(PartType.SPEECH, speech)))));
    }

    private static List<LLMChatMessage> history(Prompt prompt) {
        return prompt.messages().subList(1, prompt.messages().size());
    }

    @Test
    void aPreviewHoldsTheMessageBeingWrittenAndLeavesTheChatAlone() {
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        chat.addPersona(new Persona("Athena", "A reader."));
        chat.getMessages().add(BetaMessage.user("First"));
        BetaMessage draft = BetaMessage.user("Second, still typed");
        Options options = new Options(true, 0, 0, null, "[Part 1] long story");

        Prompt now = PromptBuilder.build(chat, "P", options);
        Prompt preview = PromptBuilder.build(chat.preview(draft), "P", options);

        assertEquals(1, chat.getMessages().size());
        assertTrue(preview.tokens() > now.tokens(), "the text and the parts count");
        assertTrue(preview.messages().getLast().getContent().contains("[Part 1] long story"));
        assertTrue(preview.messages().getLast().getContent().contains("Second, still typed"));
    }

    @Test
    void theSceneGoesAfterThePromptUnlessThePromptPlacesIt() {
        assertEquals("Prompt.\n\nThe scene of the conversation:\nA bar.", PromptBuilder.withScene("Prompt.", "Prompt.", " A bar. "));
        assertEquals("Prompt in A bar.", PromptBuilder.withScene("Prompt in {{scene}}", "Prompt in A bar.", "A bar."));
        assertEquals("X", PromptBuilder.withScene("{{#scene}}x{{/scene}}", "X", "A bar."));
        assertEquals("X", PromptBuilder.withScene("{{{ scene }}}", "X", "A bar."));
        assertEquals("Prompt.", PromptBuilder.withScene("Prompt.", "Prompt.", ""));
        assertEquals("Prompt.", PromptBuilder.withScene("Prompt.", "Prompt.", null));
        assertTrue(PromptBuilder.withScene("About the sceneries", "About the sceneries", "A bar.").endsWith("A bar."),
                "a word that only starts like it is not the variable");
    }

    @Test
    void theSystemMessageIsThePromptAndTheDescriptionOfTheAnswer() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("Hello"));

        Prompt prompt = PromptBuilder.build(chat, "  The prompt.  ", JSON);

        LLMChatMessage system = prompt.messages().getFirst();
        assertEquals(LLMRole.SYSTEM, system.getRole());
        assertTrue(system.getContent().startsWith("The prompt.\n\n# How to answer"));
        assertFalse(system.getContent().contains("reasoning"), "the model is not asked for a reasoning: its own thinking is shown");
        assertTrue(system.getContent().contains("JSON"));
    }

    @Test
    void theTextFormatAsksForParagraphsAndNoReasoning() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("Hello"));

        String system = PromptBuilder.build(chat, "P", TEXT).messages().getFirst().getContent();

        assertTrue(system.contains("**Name:**"));
        assertFalse(system.contains("JSON"));
        assertFalse(system.contains("reasoning"));
    }

    @Test
    void theLimitOfMessagesIsToldToTheModel() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("Hello"));

        assertTrue(PromptBuilder.build(chat, "P", new Options(true, 4, 0, null)).messages().getFirst().getContent()
                .contains("at most 4 messages"));
        assertFalse(PromptBuilder.build(chat, "P", JSON).messages().getFirst().getContent().contains("at most"));
    }

    @Test
    void theRosterIsOnlyInTheLastUserMessage() {
        BetaChat chat = group(athena(), vesper());
        chat.getMessages().add(BetaMessage.user("First"));
        chat.getMessages().add(reply("r", "Athena", "Hi"));
        chat.getMessages().add(BetaMessage.user("Second"));

        List<LLMChatMessage> messages = history(PromptBuilder.build(chat, "P", JSON));

        assertEquals(3, messages.size());
        assertEquals("First", messages.get(0).getContent());
        assertFalse(messages.get(1).getContent().contains("Snarky"));
        assertTrue(messages.get(2).getContent().startsWith("Second\n\n[The personas present]"));
        assertTrue(messages.get(2).getContent().contains("**Athena**: The Analytical Archivist."));
        assertTrue(messages.get(2).getContent().contains("**Vesper**: The Snarky Realist."));
    }

    @Test
    void theRosterIsNotStoredInTheChat() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("Hello"));

        PromptBuilder.build(chat, "P", JSON);

        assertEquals("Hello", chat.getMessages().getFirst().getText());
    }

    @Test
    void theSystemPromptAndTheHistoryDoNotChangeWhenAPersonaJoins() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("First"));
        chat.getMessages().add(reply("r", "Athena", "Hi"));
        chat.getMessages().add(BetaMessage.user("Second"));
        Prompt before = PromptBuilder.build(chat, "P", JSON);

        chat.getMessages().add(reply("r2", "Athena", "Again"));
        chat.addPersona(vesper());
        chat.getMessages().add(BetaMessage.joined(vesper(), "Vesper arrives late."));
        chat.getMessages().add(BetaMessage.user("Third"));
        Prompt after = PromptBuilder.build(chat, "P", JSON);

        assertEquals(before.messages().get(0).getContent(), after.messages().get(0).getContent());
        assertEquals("First", after.messages().get(1).getContent());
        assertEquals(before.messages().get(2).getContent(), after.messages().get(2).getContent());
        // the roster went with the earlier question only then, the new roster is on the new last message
        assertTrue(before.messages().get(3).getContent().startsWith("Second\n\n[The personas present]"));
        assertEquals("Second", after.messages().get(3).getContent());
        String last = after.messages().getLast().getContent();
        assertTrue(last.contains("[Vesper joins the conversation.] Vesper arrives late."));
        assertTrue(last.contains("Third"));
        assertTrue(last.contains("**Vesper**"));
    }

    @Test
    void theJoinLineAndTheNextMessageAreOneMessageOfTheUser() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("First"));
        chat.getMessages().add(reply("r", "Athena", "Hi"));
        chat.getMessages().add(BetaMessage.joined(vesper(), ""));
        chat.getMessages().add(BetaMessage.user("Second"));

        List<LLMChatMessage> messages = history(PromptBuilder.build(chat, "P", JSON));

        assertEquals(3, messages.size());
        assertTrue(messages.get(2).getContent().startsWith("[Vesper joins the conversation.]\n\nSecond"));
    }

    @Test
    void aPersonaThatLeftIsNotInTheRosterButItsLineIs() {
        BetaChat chat = group(athena(), vesper());
        Persona leaving = chat.findPersona("vesper");
        chat.removePersona(leaving.getUuid());
        chat.getMessages().add(BetaMessage.left(leaving, "Vesper has to go."));
        chat.getMessages().add(BetaMessage.user("Go on"));

        String last = PromptBuilder.build(chat, "P", JSON).messages().getLast().getContent();

        assertTrue(last.contains("[Vesper leaves the conversation.] Vesper has to go."));
        assertTrue(last.contains("**Athena**"));
        assertFalse(last.contains("**Vesper**"));
    }

    @Test
    void aSingleChatDescribesItsOnePersona() {
        BetaChat chat = new BetaChat("Chat", ChatKind.SINGLE);
        chat.addPersona(athena());
        chat.getMessages().add(BetaMessage.user("Hello"));

        String last = PromptBuilder.build(chat, "P", JSON).messages().getLast().getContent();

        assertTrue(last.contains("[The persona you play]"));
        assertTrue(last.contains("**Athena**: The Analytical Archivist."));
        assertFalse(PromptBuilder.build(chat, "P", JSON).messages().getFirst().getContent().contains("join or leave"));
    }

    @Test
    void theRepliesGoBackAsTheJsonTheyWereWithoutTheReasoning() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("First"));
        chat.getMessages().add(reply("Athena first", "Athena", "Hi"));
        chat.getMessages().add(BetaMessage.user("Second"));

        String json = history(PromptBuilder.build(chat, "P", JSON)).get(1).getContent();

        assertEquals("{\"messages\":[{\"speaker\":\"Athena\",\"parts\":"
                + "[{\"type\":\"action\",\"text\":\"nods\"},{\"type\":\"speech\",\"text\":\"Hi\"}]}]}", json);
    }

    @Test
    void theRepliesGoBackAsTextInTheTextFormat() {
        BetaChat chat = group(athena(), vesper());
        chat.getMessages().add(BetaMessage.user("First"));
        chat.getMessages().add(BetaMessage.assistant("", List.of(
                new BetaSpoken("Athena", List.of(new BetaPart(PartType.ACTION, "nods"), new BetaPart(PartType.SPEECH, "Hi"))),
                new BetaSpoken("Vesper", List.of(new BetaPart(PartType.SPEECH, "Hmm"))))));
        chat.getMessages().add(BetaMessage.user("Second"));

        String text = history(PromptBuilder.build(chat, "P", TEXT)).get(1).getContent();

        assertEquals("**Athena:** *nods* Hi\n\n**Vesper:** Hmm", text);
    }

    @Test
    void aReplyThatCouldNotBeReadGoesBackAsItWasWritten() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("First"));
        BetaMessage broken = BetaMessage.assistant("", List.of());
        broken.setRawText("Sorry, I lost my train of thought");
        chat.getMessages().add(broken);
        chat.getMessages().add(BetaMessage.user("Second"));

        assertEquals("Sorry, I lost my train of thought", history(PromptBuilder.build(chat, "P", JSON)).get(1).getContent());
    }

    @Test
    void theStoryGoesBeforeTheFirstMessageOfTheHistory() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("What do you think?"));

        Prompt prompt = PromptBuilder.build(chat, "P", JSON.withStory("[The story]\n[Part 3]\nHe opened the door."));

        assertTrue(history(prompt).getFirst().getContent().startsWith("[The story]\n[Part 3]\nHe opened the door.\n\nWhat do you think?"));
        // it is in the prompt once, whatever is asked
        assertEquals(1, prompt.messages().stream().filter(message -> message.getContent().contains("He opened the door.")).count());
    }

    @Test
    void theStoryIsNotDroppedWithTheOldLinesOfTheHistory() {
        BetaChat chat = group(athena());
        for (int i = 0; i < 6; i++) {
            chat.getMessages().add(BetaMessage.user("Question number " + i + " " + "x".repeat(200)));
            chat.getMessages().add(reply("r", "Athena", "Answer " + i));
        }
        chat.getMessages().add(BetaMessage.user("The last question"));
        Options story = new Options(true, 0, 0, null, "[The story]\n[Part 1]\nOnce upon a time.");
        int whole = PromptBuilder.build(chat, "P", story).tokens();

        Prompt trimmed = PromptBuilder.build(chat, "P", new Options(true, 0, whole / 2, null, story.story()));

        assertTrue(trimmed.dropped() > 0);
        assertTrue(history(trimmed).getFirst().getContent().startsWith("[The story]\n[Part 1]\nOnce upon a time.\n\n"));
        assertTrue(history(trimmed).getLast().getContent().contains("The last question"));
        assertEquals(1, trimmed.messages().stream().filter(message -> message.getContent().contains("Once upon a time.")).count());
    }

    @Test
    void aStoryAloneIsAMessageOfTheUserWhenTheChatHasNoLines() {
        Prompt prompt = PromptBuilder.build(group(athena()), "P", JSON.withStory("[The story]"));

        assertEquals(2, prompt.messages().size());
        assertTrue(prompt.messages().getLast().getContent().startsWith("[The story]"));
    }

    @Test
    void theSameChatGivesTheSamePrompt() {
        BetaChat chat = group(athena(), vesper());
        chat.getMessages().add(BetaMessage.user("First"));
        chat.getMessages().add(reply("r", "Athena", "Hi"));
        chat.getMessages().add(BetaMessage.user("Second"));

        assertEquals(PromptBuilder.build(chat, "P", JSON).messages().stream().map(LLMChatMessage::getContent).toList(),
                PromptBuilder.build(chat, "P", JSON).messages().stream().map(LLMChatMessage::getContent).toList());
    }

    @Test
    void aChatWithoutMessagesGetsTheRosterAsTheMessage() {
        Prompt prompt = PromptBuilder.build(group(athena()), "P", JSON);

        assertEquals(2, prompt.messages().size());
        assertEquals(LLMRole.USER, prompt.messages().get(1).getRole());
        assertTrue(prompt.messages().get(1).getContent().startsWith("[The personas present]"));
    }

    @Test
    void theHistoryThatDoesNotFitLosesItsOldestMessages() {
        BetaChat chat = group(athena());
        for (int i = 0; i < 5; i++) {
            chat.getMessages().add(BetaMessage.user("question " + i + " " + "x".repeat(400)));
            chat.getMessages().add(reply("r", "Athena", "answer " + i + " " + "y".repeat(400)));
        }
        chat.getMessages().add(BetaMessage.user("the last question"));
        Prompt all = PromptBuilder.build(chat, "P", JSON);

        Prompt trimmed = PromptBuilder.build(chat, "P", new Options(true, 0, all.tokens() / 2, null));

        assertTrue(trimmed.dropped() > 0);
        assertTrue(trimmed.fits());
        assertTrue(trimmed.tokens() <= all.tokens() / 2);
        assertEquals(LLMRole.SYSTEM, trimmed.messages().getFirst().getRole());
        assertEquals(LLMRole.USER, trimmed.messages().get(1).getRole());
        assertTrue(trimmed.messages().getLast().getContent().startsWith("the last question"));
        assertEquals(all.messages().size() - trimmed.messages().size(), trimmed.dropped());
    }

    @Test
    void theLastQuestionIsKeptEvenWhenNothingFits() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("question " + "x".repeat(4000)));

        Prompt prompt = PromptBuilder.build(chat, "P", new Options(true, 0, 10, null));

        assertFalse(prompt.fits());
        assertEquals(2, prompt.messages().size());
    }

    @Test
    void theHistoryOfAPromptThatFitsIsComplete() {
        BetaChat chat = group(athena());
        chat.getMessages().add(BetaMessage.user("First"));

        Prompt prompt = PromptBuilder.build(chat, "P", new Options(true, 0, 100000, null));

        assertEquals(0, prompt.dropped());
        assertTrue(prompt.fits());
    }
}
