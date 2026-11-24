package com.github.enerccio.marginalia.extensions.charactertracker.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.charactertracker.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the characters become in the prompt and where they go, how they are read from the book, and the icons and colors of
 * their cards.
 */
class CharacterTrackerServiceTest {

    private final CharacterTrackerService service = new CharacterTrackerService();

    private static StoryCharacter character(String uuid, String name, String tagline, String note) {
        StoryCharacter character = new StoryCharacter();
        character.setUuid(uuid);
        character.setName(name);
        character.setTagline(tagline);
        character.setNote(note);
        return character;
    }

    private static CharacterRelation relation(StoryCharacter source, StoryCharacter target, RelationDirection direction,
                                              String name, String tagline) {
        CharacterRelation relation = new CharacterRelation();
        relation.setSourceUuid(source.getUuid());
        relation.setTargetUuid(target.getUuid());
        relation.setDirection(direction);
        relation.setName(name);
        relation.setTagline(tagline);
        return relation;
    }

    private static CharacterBook book() {
        CharacterBook book = new CharacterBook();
        StoryCharacter bob = character("b", "Bob  ", "the smith", "Strong.\n  Quiet\r\nand kind.");
        StoryCharacter alice = character("a", "Alice", "", "");
        StoryCharacter unnamed = character("u", "  ", "ignored", "ignored");
        List.of(bob, alice, unnamed).forEach(book::put);
        book.put(relation(alice, bob, RelationDirection.SOURCE_TARGET, "employs", "since spring"));
        book.put(relation(alice, bob, RelationDirection.UNIDIRECTIONAL, "", " "));
        book.put(relation(unnamed, bob, RelationDirection.UNIDIRECTIONAL, "knows", ""));
        return book;
    }

    private static List<LLMChatMessage> payload(LLMRole... roles) {
        AtomicInteger i = new AtomicInteger();
        return java.util.Arrays.stream(roles).map(role -> LLMChatMessage.of(role, "m" + i.getAndIncrement())).toList();
    }

    private static List<String> contents(List<LLMChatMessage> messages) {
        return messages.stream().map(LLMChatMessage::getContent).toList();
    }

    @Test
    void thePromptListsTheNamedCharactersByNameThenTheRelationships() {
        assertEquals("""
                Characters of the story:
                - Alice
                - Bob (the smith): Strong. Quiet and kind.

                Relationships:
                - Alice -> Bob: employs, since spring
                - Alice <-> Bob""", CharacterTrackerService.renderPrompt(book()));
    }

    @Test
    void noNamedCharacterIsNoPrompt() {
        CharacterBook book = new CharacterBook();
        assertEquals("", CharacterTrackerService.renderPrompt(book));
        book.put(character("x", "", "", ""));
        book.setPromptEnabled(true);
        assertEquals("", CharacterTrackerService.renderPrompt(book));
        assertNull(service.promptInsertion(book));
    }

    @Test
    void theInsertionIsMadeOnlyWhenItIsSwitchedOn() {
        CharacterBook book = book();
        assertNull(service.promptInsertion(book));
        book.setPromptEnabled(true);
        book.setPromptDepth(2);
        book.setPromptRole(LLMRole.USER);
        CharacterTrackerService.PromptInsertion insertion = service.promptInsertion(book);
        assertEquals(CharacterTrackerService.renderPrompt(book), insertion.text());
        assertEquals(2, insertion.depth());
        assertEquals(LLMRole.USER, insertion.role());
    }

    @Test
    void theTextGoesDepthMessagesBeforeTheEndButNeverBeforeTheSystemPrompt() {
        List<LLMChatMessage> payload = payload(LLMRole.SYSTEM, LLMRole.USER, LLMRole.ASSISTANT, LLMRole.USER);
        var atEnd = new CharacterTrackerService.PromptInsertion("chars", 0, LLMRole.SYSTEM);
        assertEquals(List.of("m0", "m1", "m2", "m3", "chars"), contents(service.insert(payload, atEnd)));
        var beforeLast = new CharacterTrackerService.PromptInsertion("chars", 1, LLMRole.USER);
        List<LLMChatMessage> inserted = service.insert(payload, beforeLast);
        assertEquals(List.of("m0", "m1", "m2", "chars", "m3"), contents(inserted));
        assertEquals(LLMRole.USER, inserted.get(3).getRole());
        var deep = new CharacterTrackerService.PromptInsertion("chars", 99, LLMRole.SYSTEM);
        assertEquals(List.of("m0", "chars", "m1", "m2", "m3"), contents(service.insert(payload, deep)));
        assertEquals(4, payload.size(), "the payload itself is not changed");
    }

    @Test
    void withoutASystemPromptTheTextMayGoFirst() {
        List<LLMChatMessage> payload = payload(LLMRole.USER, LLMRole.ASSISTANT);
        var deep = new CharacterTrackerService.PromptInsertion("chars", 5, LLMRole.SYSTEM);
        assertEquals(List.of("chars", "m0", "m1"), contents(service.insert(payload, deep)));
        assertEquals(List.of("chars"), contents(service.insert(List.of(), deep)));
        assertSame(payload, service.insert(payload, null));
        assertNull(service.insert(null, deep));
    }

    @Test
    void theBookIsReadFromTheAttributesOfTheManuscript() {
        CharacterBook stored = book();
        stored.setPromptEnabled(true);
        stored.setPromptDepth(3);
        JsonObject attributes = new JsonObject();
        attributes.add(CharacterTrackerData.KEY, new Gson().toJsonTree(stored.toData()));
        Manuscript manuscript = new Manuscript();
        manuscript.setAttributes(attributes);

        CharacterBook read = service.read(manuscript);
        assertEquals(CharacterTrackerService.renderPrompt(stored), CharacterTrackerService.renderPrompt(read));
        assertTrue(read.isPromptEnabled());
        assertEquals(3, read.getPromptDepth());

        Manuscript empty = new Manuscript();
        empty.setAttributes(new JsonObject());
        assertTrue(service.read(empty).sortedCharacters().isEmpty());
    }

    @Test
    void changeListenersAreToldUntilTheyAreRemoved() {
        Manuscript manuscript = new Manuscript();
        manuscript.setUuid("book");
        Manuscript other = new Manuscript();
        other.setUuid("other");
        AtomicInteger calls = new AtomicInteger();
        Runnable remove = service.onChange(manuscript, calls::incrementAndGet);
        service.changed(manuscript);
        service.changed(other);
        assertEquals(1, calls.get());
        remove.run();
        service.changed(manuscript);
        assertEquals(1, calls.get());
    }

    private static byte[] png(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static BufferedImage decode(String base64) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
    }

    @Test
    void iconsAreShrunkToFitKeepingTheirProportions() throws Exception {
        BufferedImage wide = decode(CharacterTrackerService.createIcon(png(1000, 500)));
        assertEquals(CharacterTrackerService.MAX_ICON_SIZE, wide.getWidth());
        assertEquals(CharacterTrackerService.MAX_ICON_SIZE / 2, wide.getHeight());

        BufferedImage tall = decode(CharacterTrackerService.createIcon(png(30, 900)));
        assertEquals(7, tall.getWidth());
        assertEquals(CharacterTrackerService.MAX_ICON_SIZE, tall.getHeight());

        BufferedImage small = decode(CharacterTrackerService.createIcon(png(64, 48)));
        assertEquals(64, small.getWidth());
        assertEquals(48, small.getHeight());
    }

    @Test
    void whatIsNoImageIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> CharacterTrackerService.createIcon("not an image".getBytes()));
        assertThrows(IllegalArgumentException.class, () -> CharacterTrackerService.createIcon(new byte[0]));
    }

    @Test
    void onlyRrggbbColorsAreLetThrough() {
        assertTrue(CharacterTrackerService.isColor("#A0b1C2"));
        assertFalse(CharacterTrackerService.isColor("#abc"));
        assertFalse(CharacterTrackerService.isColor("red"));
        assertFalse(CharacterTrackerService.isColor(null));
        assertEquals("#a0b1c2", CharacterTrackerService.safeColor("#A0B1C2"));
        assertEquals(StoryCharacter.DEFAULT_COLOR, CharacterTrackerService.safeColor("#fff; background: url(x)"));
        assertEquals(StoryCharacter.DEFAULT_COLOR, CharacterTrackerService.safeColor(null));
    }

    @Test
    void theTextIsBlackOnLightAndWhiteOnDarkColors() {
        assertEquals("#000000", CharacterTrackerService.textColor("#ffffff"));
        assertEquals("#000000", CharacterTrackerService.textColor("#ffff00"));
        assertEquals("#ffffff", CharacterTrackerService.textColor("#000000"));
        assertEquals("#ffffff", CharacterTrackerService.textColor("#0000ff"));
        assertEquals("#000000", CharacterTrackerService.textColor("nonsense"), "the default color is light");
    }

    @Test
    void theIconSourceIsADataUriOfValidBase64Only() {
        assertEquals("data:image/png;base64,iVBORw0KGgo=", CharacterTrackerService.iconSource("iVBORw0KGgo="));
        assertNull(CharacterTrackerService.iconSource(null));
        assertNull(CharacterTrackerService.iconSource(" "));
        assertNull(CharacterTrackerService.iconSource("abc\" onerror=\"alert(1)"));
    }
}
