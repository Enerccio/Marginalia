package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
    void aMessageOfTheUserShowsTheTextAndWhichPartsItSent() {
        BetaMessage message = BetaMessage.user("What do you think?");
        message.getSentParts().add(new SentPart("a", "h", 41));
        message.getSentParts().add(new SentPart("b", "h", 42));
        message.getSentParts().add(new SentPart("c", "h", 43));
        message.getSentParts().add(new SentPart("d", "h", 50));
        message.getSentParts().add(new SentPart("e", "h", 0));

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
        message.getSentParts().add(new SentPart("a", "h", 1));

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
}
