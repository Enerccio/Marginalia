package com.github.enerccio.marginalia.extensions.stconverter;

import com.github.enerccio.marginalia.extensions.stconverter.model.*;
import com.github.enerccio.marginalia.extensions.stconverter.service.ConverterService;
import com.github.enerccio.marginalia.utils.SillyTavernChats;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the converter does with a chat and with the answer of the model, without the book or the model.
 */
class ConverterLogicTest {

    private static final String CHAT = """
            {"user_name":"Me","character_name":"Narrator","chat_metadata":{}}
            {"name":"Narrator","is_user":false,"is_system":false,"send_date":"2026-01-01T10:00:00.000Z","mes":"You wake up in a tavern."}
            {"name":"Robin","is_user":true,"is_system":false,"send_date":"2026-01-01T10:00:01.000Z","mes":"I look around."}
            {"name":"note","is_user":false,"is_system":true,"mes":"hidden"}
            {"name":"Narrator","is_user":false,"is_system":false,"send_date":"2026-01-01T10:00:02.000Z","mes":"second","swipes":["first","second"],"swipe_id":1}
            {"name":"Robin","is_user":true,"is_system":false,"mes":"   "}
            {"is_user":false,"is_system":false,"mes":"No name."}
            """;

    private static GeneratedTurn turn(int first, int last) {
        return new GeneratedTurn(first, last, " scene ", " Robin ", "Robin, Julia", " do it ");
    }

    private static TurnItem item(int first, int last, boolean reviewed) {
        TurnItem item = new TurnItem(first, last, "s", "p", "c", "i");
        item.setReviewed(reviewed);
        return item;
    }

    @Test
    void chatBecomesLinesWithNamesAndTheShownSwipe() {
        List<ChatLine> lines = ConverterService.toLines(SillyTavernChats.parse(CHAT, "x.jsonl"));

        assertEquals(4, lines.size());
        assertEquals("Narrator", lines.get(0).getName());
        assertFalse(lines.get(0).isUser());
        assertEquals("Robin", lines.get(1).getName());
        assertTrue(lines.get(1).isUser());
        assertEquals("second", lines.get(2).getText());
        // a message without a name is the character of the chat
        assertEquals("Narrator", lines.get(3).getName());
        assertTrue(lines.stream().noneMatch(ChatLine::isReviewed));
    }

    @Test
    void chatTextNumbersTheMessagesOfTheRange() {
        List<ChatLine> lines = ConverterService.toLines(SillyTavernChats.parse(CHAT, "x.jsonl"));

        String text = ConverterService.chatText(lines, 2, 3);

        assertEquals("--- Message 2 (Robin) ---\nI look around.\n\n--- Message 3 (Narrator) ---\nsecond", text);
        assertEquals("", ConverterService.chatText(lines, 1, 0));
        assertTrue(ConverterService.chatText(lines, 3, 99).endsWith("No name."));
    }

    @Test
    void turnsAreClampedToTheRangeAndSorted() {
        GeneratedTurns generated = new GeneratedTurns(List.of(turn(6, 9), turn(1, 3), turn(11, 12), turn(8, 5), turn(4, 20)));

        List<TurnItem> turns = ConverterService.toTurns(generated, 3, 10);

        // 1-3 is cut to 3-3, 11-12 is outside, 8-5 is the wrong way round
        assertEquals(List.of("3", "4-10", "6-9"), turns.stream().map(TurnItem::messages).toList());
        assertEquals("scene", turns.getFirst().getSceneSetting());
        assertEquals("Robin", turns.getFirst().getPovCharacter());
        assertEquals("do it", turns.getFirst().getInstructions());
    }

    @Test
    void newTurnsReplaceTheOnesInTheRangeExceptReviewedOnes() {
        List<TurnItem> existing = new ArrayList<>(List.of(item(1, 5, false), item(6, 10, true), item(11, 15, false), item(16, 20, false)));
        List<TurnItem> added = List.of(item(8, 9, false), item(11, 14, false));

        int replaced = ConverterService.mergeTurns(existing, added, 7, 15);

        // 11-15 is replaced, 6-10 is reviewed and stays, 1-5 and 16-20 are outside of the range
        assertEquals(1, replaced);
        assertEquals(List.of("1-5", "6-10", "8-9", "11-14", "16-20"), existing.stream().map(TurnItem::messages).toList());
    }

    @Test
    void turnDetailsGoToTheNewTurnFormAsTypedValues() {
        TurnItem turn = new TurnItem(1, 2, "A tavern", "Robin", "Robin, Julia", "Robin orders ale");
        turn.setPovCharacter(null);

        JsonObject state = STConverterExtension.toFormState(turn);

        assertEquals("A tavern", state.get("scene").getAsString());
        assertEquals("", state.get("pov").getAsString());
        assertEquals("Robin, Julia", state.get("present").getAsString());
        assertEquals("Robin orders ale", state.get("instructions").getAsString());
        assertTrue(state.get("edited").getAsBoolean());
    }

    @Test
    void nextUncheckedGoesDownAndWrapsAround() {
        boolean[] checked = {true, false, true, true, false, true};

        // after row 1 (index 0): the next unchecked is index 1; after row 2: index 4; after row 5: wraps to index 1
        assertEquals(1, ConverterService.nextUnchecked(6, 0, i -> checked[i]));
        assertEquals(4, ConverterService.nextUnchecked(6, 2, i -> checked[i]));
        assertEquals(1, ConverterService.nextUnchecked(6, 5, i -> checked[i]));
        // the row the author is at is the last one to be found
        assertEquals(4, ConverterService.nextUnchecked(6, 5 - 1, i -> checked[i]));
        assertEquals(-1, ConverterService.nextUnchecked(6, 3, i -> true));
        assertEquals(-1, ConverterService.nextUnchecked(0, 0, i -> false));
    }

    /** A chat from letters: u is a message of the user, a one of the character. */
    private static List<ChatLine> chat(String letters) {
        List<ChatLine> lines = new ArrayList<>();
        for (char c : letters.toCharArray()) {
            lines.add(new ChatLine(c == 'u' ? "Robin" : "Narrator", c == 'u', "text"));
        }
        return lines;
    }

    private static List<String> blocks(List<int[]> blocks) {
        return blocks.stream().map(b -> b[0] + "-" + b[1]).toList();
    }

    @Test
    void alignedRangeStartsAtATurnAndEndsWithTheCharacter() {
        // turns: 1-2, 3, 4-6, 7-8
        List<ChatLine> chat = chat("uaauuaua");

        assertArrayEquals(new int[]{1, 2}, ConverterService.alignRange(chat, 2, 2));
        // the end on a message of the user goes back to the last message of the character, the start back to its turn
        assertArrayEquals(new int[]{4, 6}, ConverterService.alignRange(chat, 5, 7));
        assertArrayEquals(new int[]{4, 6}, ConverterService.alignRange(chat, 6, 7));
        // two messages of the character in a row are two turns
        assertArrayEquals(new int[]{3, 8}, ConverterService.alignRange(chat, 3, 8));
        assertArrayEquals(new int[]{3, 3}, ConverterService.alignRange(chat, 3, 4));
    }

    @Test
    void alignedRangeInsideTheInstructionsOfATurnTakesTheTurn() {
        List<ChatLine> chat = chat("uaauuaua");

        // nothing of the character is left going back, so it goes forward to the end of the turn
        assertArrayEquals(new int[]{4, 6}, ConverterService.alignRange(chat, 4, 5));
        assertArrayEquals(new int[]{4, 6}, ConverterService.alignRange(chat, 5, 5));
    }

    @Test
    void theEndOfTheChatIsABoundaryEvenAfterAMessageOfTheUser() {
        List<ChatLine> chat = chat("uau");

        assertArrayEquals(new int[]{1, 3}, ConverterService.alignRange(chat, 1, 3));
        assertArrayEquals(new int[]{1, 2}, ConverterService.alignRange(chat, 1, 2));
        assertArrayEquals(new int[]{1, 1}, ConverterService.alignRange(List.of(), 5, 9));
    }

    @Test
    void blocksAreMadeOfTheSizeAndAlignedToTurns() {
        List<ChatLine> chat = chat("uaauuaua");

        assertEquals(List.of("1-3", "4-6", "7-8"), blocks(ConverterService.splitBlocks(chat, 1, 8, 3, false)));
        assertEquals(List.of("1-3", "4-6", "7-8"), blocks(ConverterService.splitBlocks(chat, 1, 8, 3, true)));
        assertEquals(List.of("1-2", "3-4", "5-6", "7-8"), blocks(ConverterService.splitBlocks(chat, 1, 8, 2, false)));
        // a turn is not cut: 3-4 would end on the message of the user that belongs to 4-6
        assertEquals(List.of("1-2", "3-3", "4-6", "7-8"), blocks(ConverterService.splitBlocks(chat, 1, 8, 2, true)));
        assertEquals(List.of("4-8"), blocks(ConverterService.splitBlocks(chat, 4, 8, 100, true)));
        assertEquals(8, ConverterService.splitBlocks(chat, 1, 8, 1, false).size());
        assertTrue(ConverterService.splitBlocks(chat, 5, 4, 3, false).isEmpty());
    }

    @Test
    void theBookKeepsNoTextOfTheChat() {
        ConverterData data = new ConverterData();
        data.setResourceUuid("resource-uuid");
        data.setFileName("chat.jsonl");
        data.setChat(List.of(new ChatLine("Robin", true, "a long secret message")));
        data.setReviewed(List.of(1));

        String json = new Gson().toJson(data);
        ConverterData back = new Gson().fromJson(json, ConverterData.class);

        assertFalse(json.contains("secret"));
        assertEquals("resource-uuid", back.getResourceUuid());
        assertEquals(List.of(1), back.getReviewed());
        assertTrue(back.getChat().isEmpty());
    }
}
