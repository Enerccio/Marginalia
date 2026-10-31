package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.ChatKind;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.extensions.betareaders.model.ReplyFormat;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore.Available;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore.Scope;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The window that starts a chat: the kind decides how the personas are chosen, and what the chat gets is the choice made there; what is
 * left empty is inherited.
 */
class NewChatDialogTest {

    // UI.setCurrent keeps only a weak reference
    private UI ui;

    private final Available athena = new Available(new Persona("Athena", "The Archivist."), Scope.LIBRARY);
    private final Available vesper = new Available(new Persona("Vesper", "The Realist."), Scope.LIBRARY);
    private final Available mara = new Available(new Persona("Mara", "Of this book."), Scope.BOOK);
    private final List<BetaChat> created = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private NewChatDialog dialog() {
        return new NewChatDialog(List.of(athena, vesper, mara), List.of(new Medium("manga", "Manga", "p")),
                List.of(new Choice("ai-1", "Big model")), List.of(new Choice("pr-1", "Cold")),
                new NewChatDialog.Inherited("Book", "Model of the book", null), created::add);
    }

    private static void create(NewChatDialog dialog) {
        ComponentUtil.fireEvent(dialog.create, new ClickEvent<>(dialog.create));
    }

    @Test
    void aGroupIsTheDefaultAndChoosesManyPersonas() {
        NewChatDialog dialog = dialog();

        assertEquals(ChatKind.GROUP, dialog.kind.getValue());
        assertTrue(dialog.groupPersonas.isVisible());
        assertFalse(dialog.singlePersona.isVisible());

        dialog.groupPersonas.select(vesper, athena);
        create(dialog);

        assertEquals(1, created.size());
        BetaChat chat = created.getFirst();
        assertEquals(ChatKind.GROUP, chat.getKind());
        assertEquals(List.of("Athena", "Vesper"), chat.present().stream().map(Persona::getName).toList());
        assertEquals("Athena, Vesper", chat.getTitle());
    }

    @Test
    void aGroupMayStartWithOnePersona() {
        NewChatDialog dialog = dialog();
        dialog.groupPersonas.select(athena);

        create(dialog);

        assertEquals(ChatKind.GROUP, created.getFirst().getKind());
        assertEquals(1, created.getFirst().present().size());
    }

    @Test
    void aChatWithOnePersonaChoosesOne() {
        NewChatDialog dialog = dialog();
        dialog.kind.setValue(ChatKind.SINGLE);

        assertTrue(dialog.singlePersona.isVisible());
        assertFalse(dialog.groupPersonas.isVisible());

        // what was ticked for a group does not count
        dialog.groupPersonas.select(athena, vesper);
        dialog.singlePersona.setValue(mara);
        create(dialog);

        BetaChat chat = created.getFirst();
        assertEquals(ChatKind.SINGLE, chat.getKind());
        assertEquals(List.of("Mara"), chat.present().stream().map(Persona::getName).toList());
    }

    @Test
    void nothingIsCreatedWithoutAPersona() {
        NewChatDialog dialog = dialog();

        create(dialog);
        dialog.kind.setValue(ChatKind.SINGLE);
        create(dialog);

        assertTrue(created.isEmpty());
    }

    @Test
    void whatIsLeftEmptyIsInherited() {
        NewChatDialog dialog = dialog();
        dialog.groupPersonas.select(athena);

        create(dialog);

        BetaChat chat = created.getFirst();
        assertNull(chat.getMedium());
        assertNull(chat.getAi());
        assertNull(chat.getProtocol());
        assertEquals(ReplyFormat.AUTO, chat.getFormat());
        assertEquals("Inherited: Book", dialog.medium.getPlaceholder());
        assertEquals("Inherited: Model of the book", dialog.ai.getPlaceholder());
        assertEquals("Inherited: none", dialog.protocol.getPlaceholder());
    }

    @Test
    void theChoicesOfTheChatAreKept() {
        NewChatDialog dialog = dialog();
        dialog.groupPersonas.select(athena);
        dialog.title.setValue("  Chapter 3  ");
        dialog.medium.setValue(new Medium("manga", "Manga", "p"));
        dialog.ai.setValue(new Choice("ai-1", "Big model"));
        dialog.protocol.setValue(new Choice("pr-1", "Cold"));
        dialog.format.setValue(ReplyFormat.TEXT);

        create(dialog);

        BetaChat chat = created.getFirst();
        assertEquals("Chapter 3", chat.getTitle());
        assertEquals("manga", chat.getMedium());
        assertEquals("ai-1", chat.getAi());
        assertEquals("pr-1", chat.getProtocol());
        assertEquals(ReplyFormat.TEXT, chat.getFormat());
    }

    @Test
    void theChatHasCopiesOfThePersonas() {
        NewChatDialog dialog = dialog();
        dialog.groupPersonas.select(athena);

        create(dialog);
        athena.persona().setDescription("Changed afterwards");

        assertEquals("The Archivist.", created.getFirst().present().getFirst().getDescription());
        assertNotSame(athena.persona(), created.getFirst().present().getFirst());
    }

    @Test
    void theDefaultTitleIsShortened() {
        String title = NewChatDialog.defaultTitle(List.of("Athena the Analytical Archivist", "Vesper the Snarky Realist"));

        assertEquals(40, title.length());
        assertTrue(title.endsWith("…"));
        assertEquals("Athena", NewChatDialog.defaultTitle(List.of("Athena")));
    }

    @Test
    void thePersonasOfTheBookAreMarked() {
        NewChatDialog dialog = dialog();

        assertEquals("Mara (this book)", dialog.groupPersonas.getItemLabelGenerator().apply(mara));
        assertEquals("Athena", dialog.groupPersonas.getItemLabelGenerator().apply(athena));
        assertEquals(Set.of(), dialog.groupPersonas.getSelectedItems());
    }
}
