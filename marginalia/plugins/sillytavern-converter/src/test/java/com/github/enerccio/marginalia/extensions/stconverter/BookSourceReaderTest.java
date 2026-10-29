package com.github.enerccio.marginalia.extensions.stconverter;

import com.github.enerccio.marginalia.extensions.stconverter.model.BookSource;
import com.github.enerccio.marginalia.extensions.stconverter.model.ChatLine;
import com.github.enerccio.marginalia.extensions.stconverter.model.TurnItem;
import com.github.enerccio.marginalia.extensions.stconverter.service.BookSourceReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the converter makes of a backup of a book: the branch the book was on, stripped to the instructions and the text.
 */
class BookSourceReaderTest {

    private static JsonObject message(String uuid, String parent, String instructions, String response) {
        JsonObject message = new JsonObject();
        message.addProperty("uuid", uuid);
        if (parent != null) {
            message.addProperty("parentUuid", parent);
        }
        JsonObject content = new JsonObject();
        content.addProperty("instructions", instructions);
        content.addProperty("sceneSetting", "scene of " + uuid);
        content.addProperty("povCharacter", "pov of " + uuid);
        content.addProperty("presentCharacters", "present in " + uuid);
        content.addProperty("response", response);
        // what a backup carries besides, which the source must not keep
        content.addProperty("responseReasoning", "thinking about " + uuid);
        content.addProperty("modelUsed", "some-model");
        message.add("extendedContent", content);
        message.addProperty("tokenCount", 123);
        return message;
    }

    /** a - b - c, with b2 as a branch off a that the book is not on. */
    private static JsonObject book(String leaf) {
        JsonObject backup = new JsonObject();
        backup.addProperty("name", "The Book");
        backup.addProperty("uuid", "book-uuid");
        if (leaf != null) {
            backup.addProperty("activeLeafUuid", leaf);
        }
        JsonArray messages = new JsonArray();
        messages.add(message("c", "b", "go on", "Third."));
        messages.add(message("b2", "a", "other way", "Elsewhere."));
        messages.add(message("a", null, "begin", "First."));
        messages.add(message("b", "a", "continue", "Second."));
        backup.add("messages", messages);
        return backup;
    }

    @Test
    void followsTheActiveBranchFromTheFirstPart() {
        BookSource source = BookSourceReader.fromBackup(book("c"), BookSource.ORIGIN_BOOK, "ignored");

        assertEquals(List.of("First.", "Second.", "Third."), source.getParts().stream().map(BookSource.Part::getText).toList());
        assertEquals(List.of("begin", "continue", "go on"), source.getParts().stream().map(BookSource.Part::getInstructions).toList());
        assertEquals("The Book", source.getName());
        assertEquals(BookSource.ORIGIN_BOOK, source.getOrigin());
    }

    @Test
    void anotherLeafGivesAnotherBranch() {
        BookSource source = BookSourceReader.fromBackup(book("b2"), BookSource.ORIGIN_BACKUP, null);

        assertEquals(List.of("First.", "Elsewhere."), source.getParts().stream().map(BookSource.Part::getText).toList());
    }

    @Test
    void keepsTheDetailsOfTheTurnAndNothingElse() {
        BookSource.Part part = BookSourceReader.fromBackup(book("a"), BookSource.ORIGIN_BOOK, null).getParts().get(0);

        assertEquals("scene of a", part.getSceneSetting());
        assertEquals("pov of a", part.getPovCharacter());
        assertEquals("present in a", part.getPresentCharacters());
        String stored = new String(BookSourceReader.toBytes(BookSourceReader.fromBackup(book("a"), BookSource.ORIGIN_BOOK, null)),
                StandardCharsets.UTF_8);
        assertFalse(stored.contains("thinking"), "the reasoning is not kept");
        assertFalse(stored.contains("some-model"), "the model is not kept");
        assertFalse(stored.contains("tokenCount"), "the counts are not kept");
    }

    @Test
    void anEmptyBookGivesNothing() {
        JsonObject backup = book(null);
        backup.add("messages", new JsonArray());

        assertTrue(BookSourceReader.fromBackup(backup, BookSource.ORIGIN_BOOK, null).getParts().isEmpty());
        assertTrue(BookSourceReader.fromBackup(book(null), BookSource.ORIGIN_BOOK, null).getParts().isEmpty(), "no leaf, no parts");
    }

    @Test
    void aBackupWithoutNameTakesTheGivenOne() {
        JsonObject backup = book("a");
        backup.remove("name");

        assertEquals("Fallback", BookSourceReader.fromBackup(backup, BookSource.ORIGIN_BOOK, "Fallback").getName());
    }

    @Test
    void aDamagedChainIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.fromBackup(book("missing"), BookSource.ORIGIN_BOOK, null));

        JsonObject brokenParent = book("c");
        brokenParent.getAsJsonArray("messages").add(message("d", "gone", "x", "y"));
        brokenParent.addProperty("activeLeafUuid", "d");
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.fromBackup(brokenParent, BookSource.ORIGIN_BOOK, null));

        JsonObject loop = new JsonObject();
        JsonArray messages = new JsonArray();
        messages.add(message("x", "y", "i", "t"));
        messages.add(message("y", "x", "i", "t"));
        loop.add("messages", messages);
        loop.addProperty("activeLeafUuid", "x");
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.fromBackup(loop, BookSource.ORIGIN_BOOK, null));
    }

    @Test
    void somethingThatIsNotABackupIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.fromBackup(new JsonObject(), BookSource.ORIGIN_BOOK, null));
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.fromBackupFile("not json at all {".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.fromBackupFile("[1,2]".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void readsABareBackupFile() {
        BookSource source = BookSourceReader.fromBackupFile(book("c").toString().getBytes(StandardCharsets.UTF_8));

        assertEquals(3, source.getParts().size());
        assertEquals(BookSource.ORIGIN_BACKUP, source.getOrigin());
    }

    @Test
    void readsTheBackupStoredWithABook() {
        JsonObject stored = new JsonObject();
        stored.addProperty("manuscriptName", "Name on the backup");
        stored.add("backup", book("c"));
        JsonObject unnamed = stored.deepCopy();
        unnamed.getAsJsonObject("backup").remove("name");

        assertEquals(3, BookSourceReader.fromBackupFile(stored.toString().getBytes(StandardCharsets.UTF_8)).getParts().size());
        assertEquals("Name on the backup",
                BookSourceReader.fromBackupFile(unnamed.toString().getBytes(StandardCharsets.UTF_8)).getName());
    }

    @Test
    void readsTheArchiveWithTheImages() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("resources.json"));
            zip.write("{}".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("backup.json"));
            zip.write(book("b").toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        BookSource source = BookSourceReader.fromBackupFile(bytes.toByteArray());

        assertEquals(List.of("First.", "Second."), source.getParts().stream().map(BookSource.Part::getText).toList());
    }

    @Test
    void anArchiveWithoutABackupIsRefused() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("something.txt"));
            zip.write("x".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.fromBackupFile(bytes.toByteArray()));
    }

    @Test
    void theStoredFileIsReadBack() {
        BookSource source = BookSourceReader.fromBackup(book("c"), BookSource.ORIGIN_BOOK, null);

        BookSource back = BookSourceReader.read(BookSourceReader.toBytes(source));

        assertEquals(BookSource.FORMAT, back.getFormat());
        assertEquals(BookSource.VERSION, back.getVersion());
        assertEquals("The Book", back.getName());
        assertEquals(3, back.getParts().size());
        assertEquals("go on", back.getParts().get(2).getInstructions());
        assertEquals("Third.", back.getParts().get(2).getText());
    }

    @Test
    void aFileThatIsNotASourceIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.read("{\"name\":\"x\"}".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.read("nonsense".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.read(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> BookSourceReader.read(
                ("{\"format\":\"" + BookSource.FORMAT + "\",\"version\":" + (BookSource.VERSION + 1) + ",\"parts\":[]}")
                        .getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void thePartsAreTheMessagesAndTheirTurns() {
        BookSource source = BookSourceReader.fromBackup(book("c"), BookSource.ORIGIN_BOOK, null);

        List<ChatLine> lines = BookSourceReader.toLines(source);
        List<TurnItem> turns = BookSourceReader.toTurns(source);

        assertEquals(3, lines.size());
        assertEquals("Second.", lines.get(1).getText());
        assertFalse(lines.get(1).isUser());
        assertEquals(3, turns.size());
        // the turn of a part covers that part only
        assertEquals("2", turns.get(1).messages());
        assertEquals("continue", turns.get(1).getInstructions());
        assertEquals("scene of b", turns.get(1).getSceneSetting());
        assertEquals("pov of b", turns.get(1).getPovCharacter());
        assertEquals("present in b", turns.get(1).getPresentCharacters());
        assertFalse(turns.get(1).isReviewed());
    }
}
