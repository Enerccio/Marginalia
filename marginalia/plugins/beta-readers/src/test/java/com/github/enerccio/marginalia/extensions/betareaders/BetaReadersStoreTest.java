package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore.Available;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore.Scope;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The settings and the personas are stored in the attributes of the user settings and of the book and read back, and a book can use
 * its own personas and the ones of the library, its own winning by name.
 */
class BetaReadersStoreTest {

    private static Persona persona(String name, String description) {
        return new Persona(name, description);
    }

    @Test
    void nothingStoredIsNoSettingsAndAnEmptyBook() {
        assertNull(BetaReadersStore.readSettings(null));
        assertNull(BetaReadersStore.readSettings(new JsonObject()));
        assertTrue(BetaReadersStore.readBook(null).getPersonas().isEmpty());
        assertTrue(BetaReadersStore.readBook(new JsonObject()).getPersonas().isEmpty());
    }

    @Test
    void theSettingsComeBackAsTheyWereWritten() {
        BetaReadersSettings settings = BetaReadersDefaults.newSettings();
        settings.setSinglePrompt("one on one");
        settings.setGroupPrompt("group");
        settings.setMedium("manga");
        settings.setAi("ai-uuid");
        settings.setProtocol("protocol-uuid");
        settings.getPersonas().get(0).setIcon("aWNvbg==");
        JsonObject attributes = new JsonObject();

        BetaReadersStore.writeSettings(attributes, settings);
        BetaReadersSettings read = BetaReadersStore.readSettings(attributes);

        assertEquals("one on one", read.getSinglePrompt());
        assertEquals("group", read.getGroupPrompt());
        assertEquals("manga", read.getMedium());
        assertEquals("ai-uuid", read.getAi());
        assertEquals("protocol-uuid", read.getProtocol());
        assertEquals(settings.getPersonas().size(), read.getPersonas().size());
        assertEquals(settings.getPersonas().get(0).getUuid(), read.getPersonas().get(0).getUuid());
        assertEquals("aWNvbg==", read.getPersonas().get(0).getIcon());
        assertEquals(settings.getMediums().size(), read.getMediums().size());
        assertEquals("movie", read.getMediums().get(1).getId());
        assertEquals(settings.getMediums().get(1).getPrompt(), read.getMediums().get(1).getPrompt());
    }

    @Test
    void writingReplacesWhatWasThereAndKeepsTheOtherPlugins() {
        JsonObject attributes = JsonParser.parseString("{\"another.plugin\": {\"x\": 1}}").getAsJsonObject();
        BetaReadersSettings first = new BetaReadersSettings();
        first.setMedium("movie");
        BetaReadersStore.writeSettings(attributes, first);
        BetaReadersSettings second = new BetaReadersSettings();
        second.setMedium("manga");

        BetaReadersStore.writeSettings(attributes, second);

        assertEquals("manga", BetaReadersStore.readSettings(attributes).getMedium());
        assertEquals(1, attributes.getAsJsonObject("another.plugin").get("x").getAsInt());
    }

    @Test
    void whatAStoredObjectLacksIsFilledIn() {
        JsonObject attributes = JsonParser.parseString("{\"" + BetaReadersSettings.KEY + "\": {\"medium\": \"movie\"}}").getAsJsonObject();

        BetaReadersSettings settings = BetaReadersStore.readSettings(attributes);

        assertEquals("movie", settings.getMedium());
        assertEquals("", settings.getSinglePrompt());
        assertEquals("", settings.getAi());
        assertNotNull(settings.getPersonas());
        assertNotNull(settings.getMediums());
        assertTrue(settings.getPersonas().isEmpty());
    }

    @Test
    void theBookPersonasComeBackAsTheyWereWritten() {
        BookData book = new BookData();
        Persona mara = persona("Mara", "Loves the villain");
        mara.setIcon("aWNvbg==");
        book.getPersonas().add(mara);
        JsonObject attributes = new JsonObject();

        BetaReadersStore.writeBook(attributes, book);
        BookData read = BetaReadersStore.readBook(attributes);

        assertEquals(1, read.getPersonas().size());
        assertEquals(mara.getUuid(), read.getPersonas().get(0).getUuid());
        assertEquals("Mara", read.getPersonas().get(0).getName());
        assertEquals("Loves the villain", read.getPersonas().get(0).getDescription());
        assertEquals("aWNvbg==", read.getPersonas().get(0).getIcon());
    }

    @Test
    void theBookAndTheSettingsDoNotShareAKey() {
        assertNotEquals(BetaReadersSettings.KEY, BookData.KEY);
    }

    @Test
    void aBookUsesItsOwnPersonasAndTheLibraryOnes() {
        BetaReadersSettings settings = new BetaReadersSettings();
        settings.getPersonas().add(persona("Athena", "library"));
        settings.getPersonas().add(persona("Vesper", "library"));
        BookData book = new BookData();
        book.getPersonas().add(persona("Mara", "book"));

        List<Available> available = BetaReadersStore.available(settings, book);

        assertEquals(List.of("Mara", "Athena", "Vesper"), available.stream().map(a -> a.persona().getName()).toList());
        assertEquals(List.of(Scope.BOOK, Scope.LIBRARY, Scope.LIBRARY), available.stream().map(Available::scope).toList());
    }

    @Test
    void thePersonaOfTheBookWinsWhenTheNamesAreTheSame() {
        BetaReadersSettings settings = new BetaReadersSettings();
        settings.getPersonas().add(persona("Athena", "library"));
        settings.getPersonas().add(persona("Vesper", "library"));
        BookData book = new BookData();
        book.getPersonas().add(persona("  athena ", "book"));

        List<Available> available = BetaReadersStore.available(settings, book);

        assertEquals(2, available.size());
        assertEquals("book", available.get(0).persona().getDescription());
        assertEquals(Scope.BOOK, available.get(0).scope());
        assertEquals("Vesper", available.get(1).persona().getName());
    }

    @Test
    void personasWithoutANameAreLeftOut() {
        BetaReadersSettings settings = new BetaReadersSettings();
        settings.getPersonas().add(persona("  ", "library"));
        settings.getPersonas().add(persona(null, "library"));
        BookData book = new BookData();
        book.getPersonas().add(persona("", "book"));

        assertTrue(BetaReadersStore.available(settings, book).isEmpty());
    }

    @Test
    void withoutSettingsOrABookThereIsNothing() {
        assertTrue(BetaReadersStore.available(null, null).isEmpty());
        assertEquals(1, BetaReadersStore.available(null, bookWith("Mara")).size());
        assertEquals(6, BetaReadersStore.available(BetaReadersDefaults.newSettings(), null).size());
    }

    private static BookData bookWith(String name) {
        BookData book = new BookData();
        book.getPersonas().add(persona(name, "book"));
        return book;
    }

    @Test
    void theChatsOfABookComeBackWithTheirHistory() {
        BookData data = new BookData();
        BetaChat chat = new BetaChat("Chapter 3", ChatKind.GROUP);
        chat.addPersona(persona("Athena", "Archivist"));
        chat.setMedium("manga");
        chat.setAi("ai-uuid");
        chat.setFormat(ReplyFormat.TEXT);
        BetaMessage question = BetaMessage.user("Thoughts?");
        question.getSentParts().add(new SentPart("part-2", 2));
        chat.getMessages().add(question);
        chat.getMessages().add(BetaMessage.assistant("short", List.of(new BetaSpoken("Athena",
                List.of(new BetaPart(PartType.ACTION, "nods"), new BetaPart(PartType.SPEECH, "Good."))))));
        chat.getMessages().add(BetaMessage.joined(persona("Vesper", "Realist"), "Late."));
        data.getChats().add(chat);
        JsonObject attributes = new JsonObject();

        BetaReadersStore.writeBook(attributes, data);
        BetaChat read = BetaReadersStore.readBook(attributes).getChats().getFirst();

        assertEquals("Chapter 3", read.getTitle());
        assertEquals(ChatKind.GROUP, read.getKind());
        assertEquals(ReplyFormat.TEXT, read.getFormat());
        assertEquals("manga", read.getMedium());
        assertEquals("ai-uuid", read.getAi());
        assertNull(read.getProtocol());
        assertEquals("Athena", read.present().getFirst().getName());
        assertEquals(3, read.getMessages().size());
        assertEquals("part-2", read.getMessages().get(0).getSentParts().getFirst().getId());
        assertEquals("short", read.getMessages().get(1).getReasoning());
        BetaPart speech = read.getMessages().get(1).getSpoken().getFirst().getParts().get(1);
        assertEquals(PartType.SPEECH, speech.getType());
        assertEquals("Good.", speech.getText());
        assertEquals(MessageKind.JOIN, read.getMessages().get(2).getKind());
        assertEquals("Vesper", read.getMessages().get(2).getPersonaName());
    }

    @Test
    void aChatStoredWithoutListsIsRepaired() {
        JsonObject attributes = JsonParser.parseString(
                "{\"" + BookData.KEY + "\": {\"chats\": [{\"title\": \"Old\", \"messages\": [{\"text\": \"Hi\"}]}]}}").getAsJsonObject();

        BetaChat chat = BetaReadersStore.readBook(attributes).getChats().getFirst();

        assertEquals(ChatKind.GROUP, chat.getKind());
        assertEquals(ReplyFormat.AUTO, chat.getFormat());
        assertTrue(chat.getPersonas().isEmpty());
        assertEquals(MessageKind.USER, chat.getMessages().getFirst().getKind());
        assertTrue(chat.getMessages().getFirst().getSentParts().isEmpty());
    }

    @Test
    void aPersonaThatLeftIsNotPresentButStaysInTheChat() {
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        Persona vesper = persona("Vesper", "Realist");
        chat.addPersona(persona("Athena", "Archivist"));
        chat.addPersona(vesper);

        chat.removePersona(vesper.getUuid());

        assertEquals(1, chat.present().size());
        assertEquals(2, chat.getPersonas().size());
        assertEquals("Vesper", chat.findPersona("  VESPER ").getName());

        chat.addPersona(vesper);

        assertEquals(2, chat.present().size());
        assertEquals(2, chat.getPersonas().size());
    }

    @Test
    void theChatKeepsACopyOfThePersona() {
        BetaChat chat = new BetaChat("Chat", ChatKind.SINGLE);
        Persona original = persona("Athena", "Archivist");

        chat.addPersona(original);
        original.setDescription("Changed in the library");

        assertEquals("Archivist", chat.present().getFirst().getDescription());
    }

    @Test
    void theStoryOptionsOfAChatComeBackAndTheDefaultsAreTheWholeStoryWithSummaries() {
        BookData data = new BookData();
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        assertEquals(0, chat.getSpoilerLimit());
        assertEquals(0, chat.getFromPart());
        assertTrue(chat.isSummaries());
        chat.setSpoilerLimit(12);
        chat.setFromPart(4);
        chat.setSummaries(false);
        data.getChats().add(chat);
        JsonObject attributes = new JsonObject();

        BetaReadersStore.writeBook(attributes, data);
        BetaChat read = BetaReadersStore.readBook(attributes).getChats().getFirst();

        assertEquals(12, read.getSpoilerLimit());
        assertEquals(4, read.getFromPart());
        assertFalse(read.isSummaries());
    }

    @Test
    void aChatStoredBeforeTheStoryOptionsReadsTheWholeStoryWithSummaries() {
        JsonObject attributes = JsonParser.parseString(
                "{\"" + BookData.KEY + "\": {\"chats\": [{\"title\": \"Old\"}]}}").getAsJsonObject();

        BetaChat chat = BetaReadersStore.readBook(attributes).getChats().getFirst();

        assertEquals(0, chat.getSpoilerLimit());
        assertEquals(0, chat.getFromPart());
        assertTrue(chat.isSummaries());
    }

    @Test
    void theMostMessagesOfAChatAndOfTheUserComeBackAndAreNotSetByDefault() {
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        assertEquals(0, chat.getMaxMessages());
        chat.setMaxMessages(7);
        BookData data = new BookData();
        data.getChats().add(chat);
        JsonObject attributes = new JsonObject();
        BetaReadersStore.writeBook(attributes, data);

        assertEquals(7, BetaReadersStore.readBook(attributes).getChats().getFirst().getMaxMessages());

        BetaReadersSettings settings = new BetaReadersSettings();
        assertEquals(0, settings.getMaxMessages());
        settings.setMaxMessages(9);
        BetaReadersStore.writeSettings(attributes, settings);

        assertEquals(9, BetaReadersStore.readSettings(attributes).getMaxMessages());
    }

    @Test
    void aChatAndSettingsStoredBeforeTheMostMessagesHaveNoLimit() {
        JsonObject attributes = JsonParser.parseString("{\"" + BookData.KEY + "\": {\"chats\": [{\"title\": \"Old\"}]}, \""
                + BetaReadersSettings.KEY + "\": {\"scene\": \"x\"}}").getAsJsonObject();

        assertEquals(0, BetaReadersStore.readBook(attributes).getChats().getFirst().getMaxMessages());
        assertEquals(0, BetaReadersStore.readSettings(attributes).getMaxMessages());
    }

    @Test
    void aNegativeLimitIsNoLimit() {
        BetaChat chat = new BetaChat("Chat", ChatKind.GROUP);
        chat.setSpoilerLimit(-3);
        chat.setFromPart(-1);
        chat.setMaxMessages(-2);

        assertEquals(0, chat.getMaxMessages());
        assertEquals(0, chat.getSpoilerLimit());
        assertEquals(0, chat.getFromPart());
    }
}
