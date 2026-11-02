package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.github.enerccio.marginalia.extensions.betareaders.service.ChatEdits;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.StoryPart;
import com.github.enerccio.marginalia.extensions.betareaders.service.PromptBuilder;
import com.github.enerccio.marginalia.extensions.betareaders.service.PromptBuilder.Options;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Editing and deleting what is in the history, taking the last reply back, and persons joining and leaving.
 */
class ChatEditsTest {

    private static BetaMessage turn(String... speakers) {
        return BetaMessage.assistant("r", java.util.Arrays.stream(speakers)
                .map(name -> new BetaSpoken(name, List.of(new BetaPart(PartType.SPEECH, "Hi from " + name)))).toList());
    }

    private static BetaChat group() {
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        chat.addPersona(new Persona("Athena", "A"));
        chat.addPersona(new Persona("Vesper", "V"));
        return chat;
    }

    @Test
    void aMessageOfTheUserCanBeEdited() {
        BetaChat chat = group();
        BetaMessage message = BetaMessage.user("Old");
        message.getSentParts().add(new SentPart("p1", 1));
        chat.getMessages().add(message);

        ChatEdits.edit(chat, message.getId(), "  New text  ");

        assertEquals("New text", message.getText());
        assertEquals(1, message.getSentParts().size());
    }

    @Test
    void onlyAMessageOfTheUserCanBeEditedAndNotToNothing() {
        BetaChat chat = group();
        BetaMessage reply = turn("Athena");
        BetaMessage plain = BetaMessage.user("Hi");
        chat.getMessages().add(plain);
        chat.getMessages().add(reply);

        assertThrows(IllegalArgumentException.class, () -> ChatEdits.edit(chat, reply.getId(), "x"));
        assertThrows(IllegalArgumentException.class, () -> ChatEdits.edit(chat, "nope", "x"));
        assertThrows(IllegalArgumentException.class, () -> ChatEdits.edit(chat, plain.getId(), "  "));
    }

    @Test
    void aMessageThatSentPartsMayBeEditedToNothingAsTheTextIsNotAllItSends() {
        BetaChat chat = group();
        BetaMessage message = BetaMessage.user("Old");
        message.getSentParts().add(new SentPart("p1", 1));
        chat.getMessages().add(message);

        ChatEdits.edit(chat, message.getId(), "");

        assertEquals("", message.getText());
    }

    @Test
    void aMessageIsDeleted() {
        BetaChat chat = group();
        BetaMessage user = BetaMessage.user("Hi");
        chat.getMessages().add(user);
        chat.getMessages().add(turn("Athena"));

        assertTrue(ChatEdits.delete(chat, user.getId()));
        assertFalse(ChatEdits.delete(chat, user.getId()));
        assertEquals(1, chat.getMessages().size());
    }

    @Test
    void oneMessageOfATurnIsDeletedAndTheTurnKeepsTheRest() {
        BetaChat chat = group();
        BetaMessage reply = turn("Athena", "Vesper", "Narrator");
        chat.getMessages().add(BetaMessage.user("Hi"));
        chat.getMessages().add(reply);
        chat.getMessages().add(BetaMessage.user("More"));

        assertTrue(ChatEdits.deleteSpoken(chat, reply.getId(), 1));

        assertEquals(List.of("Athena", "Narrator"), reply.getSpoken().stream().map(BetaSpoken::getSpeaker).toList());
        String sent = PromptBuilder.build(chat, "P", new Options(true, 0, 0, null)).messages().get(2).getContent();
        assertFalse(sent.contains("Vesper"));
        assertTrue(sent.contains("Narrator"));
    }

    @Test
    void aTurnLeftWithNothingIsDeleted() {
        BetaChat chat = group();
        BetaMessage reply = turn("Athena");
        chat.getMessages().add(reply);

        assertTrue(ChatEdits.deleteSpoken(chat, reply.getId(), 0));

        assertTrue(chat.getMessages().isEmpty());
        assertFalse(ChatEdits.deleteSpoken(chat, reply.getId(), 0));
    }

    @Test
    void aMessageOutsideTheTurnIsNotDeleted() {
        BetaChat chat = group();
        BetaMessage reply = turn("Athena");
        chat.getMessages().add(reply);

        assertFalse(ChatEdits.deleteSpoken(chat, reply.getId(), 3));
        assertFalse(ChatEdits.deleteSpoken(chat, reply.getId(), -1));
        assertFalse(ChatEdits.deleteSpoken(chat, "nope", 0));
        assertEquals(1, reply.getSpoken().size());
    }

    @Test
    void theLastReplyIsTakenBack() {
        BetaChat chat = group();
        BetaMessage user = BetaMessage.user("Hi");
        BetaMessage reply = turn("Athena");
        chat.getMessages().add(user);
        chat.getMessages().add(reply);

        assertSame(reply, ChatEdits.dropLastReply(chat));
        assertEquals(List.of(user), chat.getMessages());
        assertNull(ChatEdits.dropLastReply(chat));
    }

    @Test
    void thePartsOfTheMessageBeforeTheReplyStaySentWhenTheReplyIsTakenBack() {
        BetaChat chat = group();
        BetaMessage user = BetaMessage.user("Hi");
        List<StoryPart> story = List.of(new StoryPart("p1", 1, "A"));
        user.setSentParts(new java.util.ArrayList<>(PartsTracker.unreleased(chat, story, 0, 0)));
        chat.getMessages().add(user);
        chat.getMessages().add(turn("Athena"));

        ChatEdits.dropLastReply(chat);

        assertFalse(PartsTracker.status(chat, story, 0, 0).pending());
    }

    @Test
    void aPersonaJoinsAndTheModelReadsTheLine() {
        BetaChat chat = group();
        chat.getMessages().add(BetaMessage.user("Hi"));
        chat.getMessages().add(turn("Athena"));
        Persona nox = new Persona("Nox", "The Villain's Advocate.");

        ChatEdits.join(chat, nox, "Nox has read ahead.");
        chat.getMessages().add(BetaMessage.user("Welcome"));

        assertEquals(3, chat.present().size());
        assertEquals(MessageKind.JOIN, chat.getMessages().get(2).getKind());
        String last = PromptBuilder.build(chat, "P", new Options(true, 0, 0, null)).messages().getLast().getContent();
        assertTrue(last.contains("[Nox joins the conversation.] Nox has read ahead."));
        assertTrue(last.contains("**Nox**: The Villain's Advocate."));
    }

    @Test
    void aPersonaLeavesAndItsCopyStays() {
        BetaChat chat = group();
        Persona vesper = chat.findPersona("Vesper");

        ChatEdits.leave(chat, vesper.getUuid(), "");

        assertEquals(1, chat.present().size());
        assertEquals(2, chat.getPersonas().size());
        assertEquals(MessageKind.LEAVE, chat.getMessages().getLast().getKind());
    }

    @Test
    void theLastPersonaCannotLeaveAndNobodyJoinsAOneToOneChat() {
        BetaChat group = new BetaChat("Chat", ChatKind.GROUP);
        Persona athena = new Persona("Athena", "A");
        group.addPersona(athena);
        BetaChat single = new BetaChat("Chat", ChatKind.SINGLE);
        single.addPersona(athena);

        assertThrows(IllegalStateException.class, () -> ChatEdits.leave(group, athena.getUuid(), ""));
        assertThrows(IllegalStateException.class, () -> ChatEdits.leave(group, "nope", ""));
        assertThrows(IllegalStateException.class, () -> ChatEdits.join(single, new Persona("Nox", "N"), ""));
        assertThrows(IllegalStateException.class, () -> ChatEdits.leave(single, athena.getUuid(), ""));
    }

    @Test
    void aPersonaThatLeftCanComeBack() {
        BetaChat chat = group();
        Persona vesper = chat.findPersona("Vesper");
        ChatEdits.leave(chat, vesper.getUuid(), "");

        ChatEdits.join(chat, vesper, "");

        assertEquals(2, chat.present().size());
        assertEquals(2, chat.getPersonas().size());
    }

    @Test
    void repliesLeftNextToEachOtherAreOneTurnInThePrompt() {
        BetaChat chat = group();
        BetaMessage middle = BetaMessage.user("Between");
        chat.getMessages().add(BetaMessage.user("First"));
        chat.getMessages().add(turn("Athena"));
        chat.getMessages().add(middle);
        chat.getMessages().add(turn("Vesper"));
        chat.getMessages().add(BetaMessage.user("Last"));
        ChatEdits.delete(chat, middle.getId());

        var messages = PromptBuilder.build(chat, "P", new Options(true, 0, 0, null)).messages();

        assertEquals(4, messages.size());
        assertTrue(messages.get(2).getContent().contains("Athena"));
        assertTrue(messages.get(2).getContent().contains("Vesper"));
        assertTrue(messages.get(2).getContent().startsWith("{\"messages\":[{"));
    }

    @Test
    void aHistoryThatBeginsWithAReplyIsSentWithoutIt() {
        BetaChat chat = group();
        BetaMessage first = BetaMessage.user("First");
        chat.getMessages().add(first);
        chat.getMessages().add(turn("Athena"));
        chat.getMessages().add(BetaMessage.user("Second"));
        ChatEdits.delete(chat, first.getId());

        var messages = PromptBuilder.build(chat, "P", new Options(true, 0, 0, null)).messages();

        assertEquals(2, messages.size());
        assertTrue(messages.get(1).getContent().startsWith("Second"));
    }

    @Test
    void theMostMessagesFollowTheKindAndTheSizeOfTheGroup() {
        assertEquals(3, BetaReadersDefaults.maxMessages(ChatKind.SINGLE, 1));
        assertEquals(3, BetaReadersDefaults.maxMessages(ChatKind.GROUP, 1));
        assertEquals(6, BetaReadersDefaults.maxMessages(ChatKind.GROUP, 3));
        assertEquals(8, BetaReadersDefaults.maxMessages(ChatKind.GROUP, 12));
    }

    @Test
    void changingTheMediumLeavesALineInTheHistoryThatTheModelReads() {
        BetaChat chat = group();
        chat.getMessages().add(BetaMessage.user("Hi"));
        chat.getMessages().add(turn("Athena"));

        assertTrue(ChatEdits.changeMedium(chat, "manga", "Manga / comic"));
        chat.getMessages().add(BetaMessage.user("Now?"));

        assertEquals("manga", chat.getMedium());
        assertEquals(MessageKind.MEDIUM, chat.getMessages().get(2).getKind());
        var messages = PromptBuilder.build(chat, "P", new Options(true, 0, 0, null)).messages();
        assertTrue(messages.getLast().getContent().startsWith("[From now on the text is read as: Manga / comic.]\n\nNow?"));
    }

    @Test
    void changingTheSceneChangesTheChatAndLeavesNoLineInTheHistory() {
        BetaChat chat = group();
        chat.getMessages().add(BetaMessage.user("Hi"));
        chat.getMessages().add(turn("Athena"));

        assertTrue(ChatEdits.changeScene(chat, "  A rainy bar.  "));

        assertEquals("A rainy bar.", chat.getScene());
        assertEquals(2, chat.getMessages().size(), "a new scene says nothing and asks for nothing, a scene message does");
        assertEquals(MessageKind.ASSISTANT, chat.getMessages().getLast().getKind());
    }

    @Test
    void aSceneMessageIsASceneTheModelReadsOnTheSideOfTheUserAndCanBeEdited() {
        BetaChat chat = group();
        chat.getMessages().add(BetaMessage.user("Hi"));
        chat.getMessages().add(turn("Athena"));
        BetaMessage scene = BetaMessage.scene("  The lights go out.  ");
        chat.getMessages().add(scene);

        assertEquals(MessageKind.SCENE_MESSAGE, scene.getKind());
        assertTrue(scene.isUserSide());
        var system = PromptBuilder.build(chat, "P", new Options(true, 0, 0, null)).messages();
        assertTrue(system.getFirst().getContent().contains("starts with \"Scene:\""));
        assertTrue(system.getLast().getContent().startsWith("[Scene: The lights go out.]"));
        assertEquals(com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole.USER, system.getLast().getRole());

        ChatEdits.edit(chat, scene.getId(), " The door opens. ");
        assertEquals("The door opens.", scene.getText());
        assertThrows(IllegalArgumentException.class, () -> ChatEdits.edit(chat, scene.getId(), " "));
        // the last reply can be taken back and asked again after a scene, as after a message of the user
        chat.getMessages().add(turn("Vesper"));
        assertNotNull(ChatEdits.dropLastReply(chat));
    }

    @Test
    void aSceneMessageAfterAMessageOfTheUserIsOneMessageOfTheUserSide() {
        BetaChat chat = group();
        chat.getMessages().add(BetaMessage.user("I sit down."));
        chat.getMessages().add(BetaMessage.scene("A storm starts."));

        var messages = PromptBuilder.build(chat, "P", new Options(true, 0, 0, null)).messages();

        assertEquals(2, messages.size());
        assertTrue(messages.getLast().getContent().startsWith("I sit down.\n\n[Scene: A storm starts.]"));
    }

    @Test
    void theSameSceneChangesNothingAndAnEmptyOneIsTheInheritedOne() {
        BetaChat chat = group();
        assertTrue(ChatEdits.changeScene(chat, "A bar"));
        chat.getMessages().add(BetaMessage.user("Hi"));

        assertFalse(ChatEdits.changeScene(chat, "A bar"));
        assertTrue(ChatEdits.changeScene(chat, " "));

        assertNull(chat.getScene());
        assertEquals(1, chat.getMessages().size());
    }

    @Test
    void aSceneLineFromAnOlderVersionIsStillRead() {
        BetaChat chat = group();
        chat.getMessages().add(BetaMessage.user("Hi"));
        chat.getMessages().add(turn("Athena"));
        BetaMessage line = BetaMessage.user("A rainy bar.");
        line.setKind(MessageKind.SCENE);
        chat.getMessages().add(line);
        chat.getMessages().add(BetaMessage.user("Now?"));

        var messages = PromptBuilder.build(chat, "P", new Options(true, 0, 0, null)).messages();

        assertTrue(messages.getLast().getContent().startsWith("[The scene is now: A rainy bar.]\n\nNow?"));
    }

    @Test
    void theSameMediumLeavesNoLineAndAnEmptyChatNeedsNone() {
        BetaChat chat = group();
        assertTrue(ChatEdits.changeMedium(chat, "manga", "Manga"));
        assertTrue(chat.getMessages().isEmpty());

        chat.getMessages().add(BetaMessage.user("Hi"));

        assertFalse(ChatEdits.changeMedium(chat, "manga", "Manga"));
        assertTrue(ChatEdits.changeMedium(chat, "", "Book"));
        assertNull(chat.getMedium());
        assertEquals(2, chat.getMessages().size());
        assertEquals("Book", chat.getMessages().getLast().getText());
    }

    @Test
    void aPersonaIsUpdatedFromTheLibraryButKeepsItsName() {
        BetaChat chat = group();
        Persona copy = chat.findPersona("Athena");
        Persona library = new Persona("Athena the Great", "A new description.");
        library.setUuid(copy.getUuid());
        library.setIcon("icon");

        assertTrue(ChatEdits.refresh(chat, library));

        assertEquals("A new description.", copy.getDescription());
        assertEquals("icon", copy.getIcon());
        assertEquals("Athena", copy.getName());
        assertFalse(ChatEdits.refresh(chat, new Persona("Other", "x")));
    }

    @Test
    void theHistoryCanBeReadWhileAReplyIsAddedToIt() {
        BetaChat chat = group();
        chat.getMessages().add(BetaMessage.user("Hi"));

        for (BetaMessage message : chat.getMessages()) {
            chat.getMessages().add(turn("Athena"));
            assertNotNull(message);
        }

        assertEquals(2, chat.getMessages().size());
    }
}
