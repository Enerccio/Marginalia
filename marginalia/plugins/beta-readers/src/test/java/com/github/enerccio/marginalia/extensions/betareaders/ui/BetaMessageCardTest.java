package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The cards of the history: what the user wrote and the parts it sent, a persona that joined, and a turn with its reasoning and the
 * messages of the personas (the narrator without an icon). Built without a browser.
 */
class BetaMessageCardTest {

    // UI.setCurrent keeps only a weak reference
    private UI ui;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(BetaMessageCardTest::all));
    }

    private static String text(Component root) {
        return root.getElement().getTextRecursively();
    }

    private static BetaChat chat() {
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        chat.addPersona(new Persona("Athena", "The Archivist."));
        chat.addPersona(new Persona("Vesper", "The Realist."));
        return chat;
    }

    private static BetaSpoken spoken(String speaker, PartType type, String text) {
        return new BetaSpoken(speaker, List.of(new BetaPart(type, text)));
    }

    @Test
    void aSceneMessageIsACardOfItsOwnWithTheTextAndTheButtons() {
        BetaMessage scene = BetaMessage.scene("The lights go out.");
        BetaMessageCard.Actions none = new BetaMessageCard.Actions() {
            public void edit(BetaMessage message) { }
            public void delete(BetaMessage message) { }
            public void deleteSpoken(BetaMessage turn, int index) { }
            public void regenerate() { }
        };

        Component card = BetaMessageCard.create(chat(), scene, none, true);

        assertTrue(text(card).contains("Scene"));
        assertTrue(text(card).contains("The lights go out."));
        assertFalse(text(card).contains("You"));
        assertEquals(2, all(card).filter(Button.class::isInstance).count());
    }

    @Test
    void aMessageOfTheUserShowsTheTextAndWhichPartsItSent() {
        BetaMessage message = BetaMessage.user("What do you think?");
        message.getSentParts().add(new SentPart("a", 41));
        message.getSentParts().add(new SentPart("b", 42));
        message.getSentParts().add(new SentPart("c", 43));
        message.getSentParts().add(new SentPart("d", 50));
        message.getSentParts().add(new SentPart("e", 0));

        String shown = text(BetaMessageCard.create(chat(), message));

        assertTrue(shown.contains("You"));
        assertTrue(shown.contains("Parts 41-43, 50 sent"));
        assertTrue(shown.contains("What do you think?"));
    }

    @Test
    void aMessageThatSentNothingHasNoLineAboutIt() {
        String shown = text(BetaMessageCard.create(chat(), BetaMessage.user("Hi")));

        assertFalse(shown.contains("sent"));
    }

    @Test
    void aMessageThatOnlySentPartsHasNoEmptyText() {
        BetaMessage message = BetaMessage.user("");
        message.getSentParts().add(new SentPart("a", 1));

        assertTrue(text(BetaMessageCard.create(chat(), message)).contains("Parts 1 sent"));
    }

    @Test
    void aPersonaThatJoinedOrLeftIsALineWithTheNoteOfTheUser() {
        Persona nox = new Persona("Nox", "N");

        String joined = text(BetaMessageCard.create(chat(), BetaMessage.joined(nox, "Nox read ahead.")));
        String left = text(BetaMessageCard.create(chat(), BetaMessage.left(nox, "")));

        assertTrue(joined.contains("Nox joined the conversation."));
        assertTrue(joined.contains("Nox read ahead."));
        assertEquals("Nox left the conversation.", left);
    }

    @Test
    void aTurnShowsTheReasoningClosedAboveTheMessages() {
        BetaMessage reply = BetaMessage.assistant("Athena goes first.", List.of(
                spoken("Athena", PartType.SPEECH, "A strong chapter."), spoken("Narrator", PartType.ACTION, "The fire crackles.")));

        Component card = BetaMessageCard.create(chat(), reply);

        Details details = all(card).filter(Details.class::isInstance).map(Details.class::cast).findFirst().orElseThrow();
        assertFalse(details.isOpened());
        assertTrue(text(details).contains("Athena goes first."));
        assertTrue(card.getChildren().findFirst().orElseThrow() == details, "the reasoning is above the messages");
        String shown = text(card);
        assertTrue(shown.contains("Athena"));
        assertTrue(shown.contains("A strong chapter."));
        assertTrue(shown.contains("Narrator"));
    }

    @Test
    void aTurnWithoutReasoningHasNoDetails() {
        BetaMessage reply = BetaMessage.assistant("", List.of(spoken("Athena", PartType.SPEECH, "Hi")));

        assertTrue(all(BetaMessageCard.create(chat(), reply)).noneMatch(Details.class::isInstance));
    }

    @Test
    void thePersonasHaveAnIconAndTheNarratorHasNone() {
        BetaMessage reply = BetaMessage.assistant("", List.of(
                spoken("Athena", PartType.SPEECH, "Hi"), spoken("Narrator", PartType.ACTION, "Quiet."),
                spoken("Vesper", PartType.SPEECH, "Hm")));

        List<Image> icons = all(BetaMessageCard.create(chat(), reply)).filter(Image.class::isInstance).map(Image.class::cast).toList();

        assertEquals(2, icons.size());
        assertEquals("Athena", icons.get(0).getAlt().orElse(""));
        assertEquals("Vesper", icons.get(1).getAlt().orElse(""));
    }

    @Test
    void aSpeakerTheChatDoesNotKnowStillGetsAnIconWithAStableColor() {
        BetaMessage reply = BetaMessage.assistant("", List.of(spoken("Mara", PartType.SPEECH, "Hi")));

        String first = all(BetaMessageCard.create(chat(), reply)).filter(Image.class::isInstance).map(Image.class::cast)
                .findFirst().orElseThrow().getSrc();
        String second = all(BetaMessageCard.create(chat(), reply)).filter(Image.class::isInstance).map(Image.class::cast)
                .findFirst().orElseThrow().getSrc();

        assertEquals(first, second);
    }

    @Test
    void anActionIsSetApartFromWhatIsSaid() {
        BetaMessage reply = BetaMessage.assistant("", List.of(new BetaSpoken("Athena",
                List.of(new BetaPart(PartType.ACTION, "nods"), new BetaPart(PartType.SPEECH, "Good.")))));

        List<String> actions = all(BetaMessageCard.create(chat(), reply))
                .filter(c -> c.getElement().getClassList().contains(BetaReadersStyles.ACTION)).map(BetaMessageCardTest::text).toList();

        assertEquals(List.of("nods"), actions);
    }

    @Test
    void aReplyThatWasNotReadAsMessagesIsShownAsItWasWritten() {
        BetaMessage reply = BetaMessage.assistant("", List.of());
        reply.setRawText("Sorry, I lost my train of thought.");

        assertEquals("Sorry, I lost my train of thought.", text(BetaMessageCard.create(chat(), reply)));
    }

    @Test
    void theTurnThatStreamsIsRebuiltInPlace() {
        BetaMessageCard.Turn turn = new BetaMessageCard.Turn(chat());

        turn.show("thinking", List.of(), "");
        turn.show("thinking more", List.of(spoken("Athena", PartType.SPEECH, "Hi")), "");

        assertTrue(text(turn).contains("thinking more"));
        assertFalse(text(turn).contains("thinking thinking"));
        assertEquals(1, all(turn).filter(Image.class::isInstance).count());
    }

    @Test
    void theReasoningTheUserOpenedStaysOpenWhenTheTurnIsRebuilt() {
        BetaMessageCard.Turn turn = new BetaMessageCard.Turn(chat());
        turn.show("thinking", List.of(), "");
        Details first = all(turn).filter(Details.class::isInstance).map(Details.class::cast).findFirst().orElseThrow();

        first.setOpened(true);
        turn.show("thinking more", List.of(spoken("Athena", PartType.SPEECH, "Hi")), "");

        Details second = all(turn).filter(Details.class::isInstance).map(Details.class::cast).findFirst().orElseThrow();
        assertTrue(second.isOpened(), "the redraw must not close what the user is reading");
        assertTrue(turn.isReasoningOpen());

        second.setOpened(false);
        turn.show("thinking even more", List.of(), "");
        assertFalse(all(turn).filter(Details.class::isInstance).map(Details.class::cast).findFirst().orElseThrow().isOpened());
    }

    /** Remembers what the buttons asked for. */
    private static class Recorder implements BetaMessageCard.Actions {
        final java.util.List<String> calls = new java.util.ArrayList<>();

        @Override
        public void edit(BetaMessage message) {
            calls.add("edit " + message.getText());
        }

        @Override
        public void delete(BetaMessage message) {
            calls.add("delete " + message.getKind());
        }

        @Override
        public void deleteSpoken(BetaMessage turn, int index) {
            calls.add("deleteSpoken " + index);
        }

        @Override
        public void regenerate() {
            calls.add("regenerate");
        }
    }

    private static void click(Component root, String tooltip) {
        Button button = all(root).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> tooltip.equals(b.getTooltip().getText()) || tooltip.equals(b.getText())).findFirst()
                .orElseThrow(() -> new AssertionError("No button " + tooltip));
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    @Test
    void aMessageOfTheUserHasButtonsToEditAndDeleteIt() {
        Recorder recorder = new Recorder();
        Component card = BetaMessageCard.create(chat(), BetaMessage.user("Hello"), recorder, false);

        click(card, "Edit message");
        click(card, "Delete message");

        assertEquals(List.of("edit Hello", "delete USER"), recorder.calls);
    }

    @Test
    void everyMessageOfATurnCanBeDeletedAndTheLastTurnAskedForAgain() {
        Recorder recorder = new Recorder();
        BetaMessage reply = BetaMessage.assistant("", List.of(
                spoken("Athena", PartType.SPEECH, "Hi"), spoken("Vesper", PartType.SPEECH, "Hm")));
        Component last = BetaMessageCard.create(chat(), reply, recorder, true);
        Component earlier = BetaMessageCard.create(chat(), reply, recorder, false);

        List<Button> trash = all(last).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> "Delete message".equals(b.getTooltip().getText())).toList();
        ComponentUtil.fireEvent(trash.get(1), new ClickEvent<>(trash.get(1)));
        click(last, "Regenerate");

        assertEquals(2, trash.size());
        assertEquals(List.of("deleteSpoken 1", "regenerate"), recorder.calls);
        assertTrue(all(earlier).filter(Button.class::isInstance).map(Button.class::cast).noneMatch(b -> "Regenerate".equals(b.getText())));
    }

    @Test
    void aTurnWithoutMessagesCanBeDeletedAsAWhole() {
        Recorder recorder = new Recorder();
        BetaMessage broken = BetaMessage.assistant("", List.of());
        broken.setRawText("Lost.");

        click(BetaMessageCard.create(chat(), broken, recorder, false), "Delete message");

        assertEquals(List.of("delete ASSISTANT"), recorder.calls);
    }

    @Test
    void aLineAboutAPersonaOrTheMediumCanBeDeleted() {
        Recorder recorder = new Recorder();
        BetaMessage medium = BetaMessage.user("Manga");
        medium.setKind(MessageKind.MEDIUM);

        Component joined = BetaMessageCard.create(chat(), BetaMessage.joined(new Persona("Nox", "N"), ""), recorder, false);
        Component changed = BetaMessageCard.create(chat(), medium, recorder, false);
        click(joined, "Delete message");
        click(changed, "Delete message");

        assertEquals(List.of("delete JOIN", "delete MEDIUM"), recorder.calls);
        assertTrue(text(changed).contains("From now on the text is read as: Manga"));
    }

    @Test
    void cardsWithoutActionsHaveNoButtons() {
        BetaMessage reply = BetaMessage.assistant("", List.of(spoken("Athena", PartType.SPEECH, "Hi")));

        assertTrue(all(BetaMessageCard.create(chat(), reply)).noneMatch(Button.class::isInstance));
        assertTrue(all(BetaMessageCard.create(chat(), BetaMessage.user("Hi"))).noneMatch(Button.class::isInstance));
    }
}
