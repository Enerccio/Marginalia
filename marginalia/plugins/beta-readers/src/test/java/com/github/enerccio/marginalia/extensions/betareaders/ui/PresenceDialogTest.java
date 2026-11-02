package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The window that brings a persona in or sends one away, and the one that changes the settings of a chat.
 */
class PresenceDialogTest {

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

    private static void click(Button button) {
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    @Test
    void bringingInNeedsAPersonaAndTakesTheNote() {
        List<String> told = new ArrayList<>();
        Persona nox = new Persona("Nox", "N");
        PresenceDialog dialog = new PresenceDialog("Bring in a persona", "Bring in", List.of(nox, new Persona("Sable", "S")),
                (persona, note) -> told.add(persona.getName() + ":" + note));

        click(dialog.confirm);
        assertTrue(told.isEmpty(), "no persona chosen");

        dialog.persona.setValue(nox);
        dialog.note.setValue("  Nox read ahead.  ");
        click(dialog.confirm);

        assertEquals(List.of("Nox:Nox read ahead."), told);
    }

    @Test
    void sendingAwayAsksOnlyForTheNote() {
        List<String> told = new ArrayList<>();
        PresenceDialog dialog = new PresenceDialog("Send away Vesper", "Send away", null, (persona, note) -> told.add(persona + ":" + note));

        assertFalse(dialog.persona.isAttached());
        assertTrue(dialog.persona.getParent().isEmpty(), "the choice of a persona is not in the window");
        dialog.note.setValue("She has to go.");
        click(dialog.confirm);

        assertEquals(List.of("null:She has to go."), told);
    }

    @Test
    void theSettingsOfAChatShowWhatItChoseAndGiveBackTheNewChoice() {
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        chat.setMedium("manga");
        chat.setScene("A rainy bar.");
        chat.setAi("ai-1");
        chat.setFormat(ReplyFormat.TEXT);
        chat.setUserName("Petr");
        chat.setSpoilerLimit(5);
        List<ChatOptionsForm> saved = new ArrayList<>();
        ChatSettingsDialog dialog = new ChatSettingsDialog(chat, List.of(new Medium("book", "Book", "p"), new Medium("manga", "Manga", "p")),
                List.of(new Choice("ai-1", "Big"), new Choice("ai-2", "Small")), List.of(new Choice("pr-1", "Cold")),
                new ChatOptionsForm.Inherited("Book", "Big", null, "An online chat"), saved::add);

        assertEquals("Manga", dialog.options.medium.getValue().getName());
        assertEquals("A rainy bar.", dialog.options.scene.getValue());
        assertEquals("Inherited: An online chat", dialog.options.scene.getPlaceholder());
        assertEquals("Big", dialog.options.ai.getValue().label());
        assertNull(dialog.options.protocol.getValue());
        assertEquals(ReplyFormat.TEXT, dialog.options.format.getValue());
        assertEquals("Petr", dialog.options.userName.getValue());
        assertTrue(dialog.options.summaries.getValue());
        // the first and the last part the personas read are in the panel of the chat, not here
        assertFalse(dialog.options.spoilerLimit.isVisible());
        assertFalse(dialog.options.fromPart.isVisible());

        dialog.options.spoilerLimit.setValue(8);
        dialog.options.fromPart.setValue(9);
        dialog.options.userName.setValue("  Pavel ");
        dialog.options.summaries.setValue(false);
        dialog.options.medium.clear();
        dialog.options.ai.setValue(new Choice("ai-2", "Small"));
        click(dialog.save);

        assertEquals(1, saved.size());
        assertNull(saved.getFirst().mediumId());
        assertEquals("Book", saved.getFirst().mediumName(), "an empty choice reads the text as the inherited medium");
        dialog.options.scene.setValue("   ");
        assertNull(saved.getFirst().sceneText());
        assertEquals("An online chat", saved.getFirst().effectiveScene());
        assertEquals("ai-2", saved.getFirst().aiId());
        assertNull(saved.getFirst().protocolId());
        assertEquals(ReplyFormat.TEXT, saved.getFirst().replyFormat());

        // applied to a chat that exists: everything but the medium (a change of that leaves a line in the history)
        saved.getFirst().applyExceptHistoryLines(chat);
        assertEquals("manga", chat.getMedium());
        assertEquals("A rainy bar.", chat.getScene(), "the scene is changed with its own call");
        assertEquals("ai-2", chat.getAi());
        assertEquals("Pavel", chat.getUserName());
        assertEquals(5, chat.getSpoilerLimit(), "the dialog leaves the part range to the panel");
        assertEquals(0, chat.getFromPart());
        assertFalse(chat.isSummaries());
        assertNull(saved.getFirst().problem(), "a first part after the last one does not matter while the fields are not used");
    }
}
