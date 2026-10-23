package com.github.enerccio.marginalia.extensions.stconverter;

import com.github.enerccio.marginalia.extensions.stconverter.model.ChatLine;
import com.github.enerccio.marginalia.extensions.stconverter.model.GeneratedTurn;
import com.github.enerccio.marginalia.extensions.stconverter.model.GeneratedTurns;
import com.github.enerccio.marginalia.extensions.stconverter.model.TurnItem;
import com.github.enerccio.marginalia.extensions.stconverter.service.ConverterService;
import com.github.enerccio.marginalia.utils.SillyTavernChats;
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
}
