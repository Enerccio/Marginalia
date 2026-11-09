package com.github.enerccio.marginalia.extensions.storynotes;

import com.github.enerccio.marginalia.extensions.storynotes.model.NoteBook;
import com.github.enerccio.marginalia.extensions.storynotes.model.NotePage;
import com.github.enerccio.marginalia.extensions.storynotes.service.StoryNotesService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The wiki side of the notes: titles, links, backlinks, renaming, the search, tags and what the assistants may see.
 */
class NoteBookTest {

    private static NotePage page(NoteBook book, String title, String content, String... tags) {
        NotePage page = new NotePage();
        page.setTitle(title);
        page.setContent(content);
        page.setTags(List.of(tags));
        book.put(page);
        return page;
    }

    @Test
    void titlesAreFoundIgnoringCaseAndSpaces() {
        NoteBook book = new NoteBook();
        NotePage betrayal = page(book, "The Betrayal", "");

        assertSame(betrayal, book.byTitle("  the betrayal "));
        assertNull(book.byTitle("The Betrayals"));
        assertNull(book.byTitle(null));
        assertTrue(book.titleTaken("THE BETRAYAL", null));
        assertFalse(book.titleTaken("THE BETRAYAL", betrayal.getUuid()));
    }

    @Test
    void linksAreListedOnceInTheOrderTheyAppear() {
        assertEquals(List.of("Magic", "Queen"),
                NoteBook.linksIn("See [[Magic]] and [[ Queen ]], again [[magic]]. [[]] and [not] a [[link"));
        assertEquals(List.of(), NoteBook.linksIn(null));
    }

    @Test
    void linksOfThePreviewAreMarkdownLinksThatCarryTheTitle() {
        String text = "See [[Language barrier]] and [[Mizuki's secret]], [[  ]] stays, [[Čeština (Vrakht) 50%]].";

        String linked = NoteBook.linkify(text);

        assertEquals("See [Language barrier](#note:Language%20barrier) and [Mizuki's secret](#note:Mizuki%27s%20secret), "
                + "[[  ]] stays, [Čeština (Vrakht) 50%](#note:%C4%8Ce%C5%A1tina%20%28Vrakht%29%2050%25).", linked);
        assertEquals("Language barrier", NoteBook.titleOfHref("#note:Language%20barrier"));
        assertEquals("Mizuki's secret", NoteBook.titleOfHref("#note:Mizuki%27s%20secret"));
        assertEquals("Čeština (Vrakht) 50%", NoteBook.titleOfHref("#note:%C4%8Ce%C5%A1tina%20%28Vrakht%29%2050%25"));
        assertEquals("a+b", NoteBook.titleOfHref("#note:a%2Bb"));
    }

    @Test
    void onlyTheAddressesOfTheNotesAreFollowed() {
        assertNull(NoteBook.titleOfHref(null));
        assertNull(NoteBook.titleOfHref(""));
        assertNull(NoteBook.titleOfHref("#note:"));
        assertNull(NoteBook.titleOfHref("https://example.com/#note:x"));
        assertEquals("", NoteBook.linkify(null));
        assertEquals("no links here", NoteBook.linkify("no links here"));
    }

    @Test
    void aLinkWithABarNamesThePageBeforeItAndShowsTheTextAfterIt() {
        assertEquals(List.of("Language barrier", "Queen"),
                NoteBook.linksIn("[[Language barrier|how they talk]], [[ Queen | the queen ]], [[language barrier|again]], [[|nothing]], [[Empty|]]")
                        .stream().filter(t -> !t.equals("Empty")).toList());
        assertTrue(NoteBook.linksIn("[[Empty|]]").contains("Empty"), "a bar with no text still links");

        assertEquals("She met [how they talk](#note:Language%20barrier), [the queen](#note:Queen), [Empty](#note:Empty) and [[|nothing]].",
                NoteBook.linkify("She met [[Language barrier|how they talk]], [[ Queen | the queen ]], [[Empty|]] and [[|nothing]]."));
        // only the first bar separates: the rest is text
        assertEquals("[b|c](#note:a)", NoteBook.linkify("[[a|b|c]]"));
    }

    @Test
    void aLinkWithShownTextCountsAsALinkToThePage() {
        NoteBook book = new NoteBook();
        NotePage magic = page(book, "Magic", "");
        page(book, "Queen", "She overuses [[magic|the old craft]].");

        assertEquals(List.of("Queen"), book.backlinks(magic).stream().map(NotePage::getTitle).toList());
    }

    @Test
    void renamingAPageKeepsTheTextItsLinksShow() {
        NoteBook book = new NoteBook();
        NotePage magic = page(book, "Magic", "");
        NotePage queen = page(book, "Queen", "[[Magic|the old craft]], [[magic]], [[Magic System|other]].");

        book.rename(magic, "Arcana");

        assertEquals("[[Arcana|the old craft]], [[Arcana]], [[Magic System|other]].", queen.getContent());
    }

    @Test
    void backlinksAreThePagesThatLinkByTitle() {
        NoteBook book = new NoteBook();
        NotePage magic = page(book, "Magic", "Costs memory.");
        NotePage queen = page(book, "Queen", "Uses [[magic]] too much.");
        page(book, "Plot", "The [[Queen]] falls. [[Magic]] returns.");
        page(book, "Unrelated", "Nothing.");
        magic.setContent("Costs memory, see [[Magic]] itself.");

        assertEquals(List.of("Plot", "Queen"), book.backlinks(magic).stream().map(NotePage::getTitle).toList());
        assertEquals(List.of("Plot"), book.backlinks(queen).stream().map(NotePage::getTitle).toList());
    }

    @Test
    void renamingChangesTheLinksInTheOtherPages() {
        NoteBook book = new NoteBook();
        NotePage magic = page(book, "Magic", "");
        NotePage queen = page(book, "Queen", "Uses [[magic]] and [[Magic System]]; [[MAGIC]]!");
        NotePage plot = page(book, "Plot", "Nothing.");
        long before = queen.getModified();

        List<NotePage> changed = book.rename(magic, "Arcana");

        assertEquals("Arcana", magic.getTitle());
        assertEquals(List.of(queen), changed);
        assertEquals("Uses [[Arcana]] and [[Magic System]]; [[Arcana]]!", queen.getContent());
        assertTrue(queen.getModified() > before);
        assertEquals("Nothing.", plot.getContent());
    }

    @Test
    void renamingToATakenTitleIsRefused() {
        NoteBook book = new NoteBook();
        NotePage magic = page(book, "Magic", "");
        page(book, "Queen", "");

        assertThrows(IllegalArgumentException.class, () -> book.rename(magic, "queen"));
        assertEquals("Magic", magic.getTitle());
        // the same title in another case is the page itself
        book.rename(magic, "MAGIC");
        assertEquals("MAGIC", magic.getTitle());
    }

    @Test
    void titlesCannotBreakALink() {
        assertEquals("Magic", NoteBook.cleanTitle("  Magic "));
        assertThrows(IllegalArgumentException.class, () -> NoteBook.cleanTitle("  "));
        assertThrows(IllegalArgumentException.class, () -> NoteBook.cleanTitle("a [b]"));
        assertThrows(IllegalArgumentException.class, () -> NoteBook.cleanTitle("a\nb"));
        assertThrows(IllegalArgumentException.class, () -> NoteBook.cleanTitle("a|b"), "the bar separates the title from the shown text in a link");
        assertThrows(IllegalArgumentException.class, () -> NoteBook.cleanTitle("x".repeat(NoteBook.MAX_TITLE + 1)));
    }

    @Test
    void searchPutsTitlesBeforeTagsBeforeText() {
        NoteBook book = new NoteBook();
        page(book, "Chapter plan", "The dragon sleeps.");
        page(book, "Dragon lore", "Old.");
        page(book, "Bestiary", "Old.", "dragons");
        page(book, "Other", "Nothing here.");

        assertEquals(List.of("Dragon lore", "Bestiary", "Chapter plan"),
                book.search("DRAGON", false).stream().map(NotePage::getTitle).toList());
        assertEquals(List.of(), book.search("  ", false));
    }

    @Test
    void searchCanLeaveOutThePagesHiddenFromAssistants() {
        NoteBook book = new NoteBook();
        page(book, "Public", "secret plan");
        NotePage hidden = page(book, "Private", "secret plan");
        hidden.setAiVisible(false);

        assertEquals(2, book.search("secret", false).size());
        assertEquals(List.of("Public"), book.search("secret", true).stream().map(NotePage::getTitle).toList());
        assertEquals(List.of("Public"), book.visible().stream().map(NotePage::getTitle).toList());
    }

    @Test
    void tagsAreStrippedAndNotRepeated() {
        assertEquals(List.of("plot", "magic"), NoteBook.parseTags(" plot, magic ,,Magic\nplot "));
        assertEquals(List.of(), NoteBook.parseTags(null));
        assertThrows(IllegalArgumentException.class, () -> NoteBook.parseTags("x".repeat(NoteBook.MAX_TAG + 1)));
        String many = String.join(",", java.util.stream.IntStream.range(0, NoteBook.MAX_TAGS + 1).mapToObj(i -> "t" + i).toList());
        assertThrows(IllegalArgumentException.class, () -> NoteBook.parseTags(many));

        NoteBook book = new NoteBook();
        page(book, "A", "", "Plot", "magic");
        page(book, "B", "", "plot");
        assertEquals(List.of("magic", "Plot"), book.allTags());
        assertTrue(NoteBook.hasTag(book.byTitle("B"), "PLOT"));
    }

    @Test
    void theSnippetShowsTheMatchAndTheFirstLineSkipsHeadings() {
        String text = "# \n## The queen\n" + "word ".repeat(80) + "needle " + "word ".repeat(80);
        String snippet = NoteBook.snippet(text, "NEEDLE", 60);
        assertTrue(snippet.contains("needle"));
        assertTrue(snippet.startsWith("...") && snippet.endsWith("..."));
        assertEquals("The queen", NoteBook.firstLine(text, 100));
        assertEquals("", NoteBook.firstLine(null, 100));
        assertEquals("abcd…", NoteBook.firstLine("abcdefgh", 5));
    }

    @Test
    void aPieceOfTextIsReplacedOnceWhenItIsThereOnce() {
        NoteBook.Replacement result = NoteBook.replace("The queen rules. The King obeys.", "King", "Regent", false);

        assertEquals("The queen rules. The Regent obeys.", result.content());
        assertEquals(1, result.count());
    }

    @Test
    void aPieceThatIsThereMoreThanOnceIsRefusedUnlessAllIsAsked() {
        String text = "Mara waits. Mara leaves. Tomas stays.";

        IllegalArgumentException refused = assertThrows(IllegalArgumentException.class, () -> NoteBook.replace(text, "Mara", "Mira", false));
        assertTrue(refused.getMessage().contains("2 times"));

        NoteBook.Replacement all = NoteBook.replace(text, "Mara", "Mira", true);
        assertEquals("Mira waits. Mira leaves. Tomas stays.", all.content());
        assertEquals(2, all.count());
    }

    @Test
    void replacingTextThatIsNotThereOrEmptyChangesNothing() {
        assertThrows(IllegalArgumentException.class, () -> NoteBook.replace("abc", "x", "y", true));
        assertThrows(IllegalArgumentException.class, () -> NoteBook.replace("abc", "", "y", true));
        assertThrows(IllegalArgumentException.class, () -> NoteBook.replace("abc", "B", "y", false), "the case matters");
        assertEquals("ac", NoteBook.replace("abc", "b", "", false).content(), "an empty replacement deletes the piece");
    }

    @Test
    void overlappingPiecesAreCountedAsTheyAreReplaced() {
        // "aa" in "aaaa": two pieces that do not overlap, the way String.replace takes them
        NoteBook.Replacement result = NoteBook.replace("aaaa", "aa", "b", true);

        assertEquals("bb", result.content());
        assertEquals(2, result.count());
    }

    @Test
    void theTextForAssistantsHasOnlyTheVisiblePages() {
        NoteBook book = new NoteBook();
        page(book, "Magic", "Costs memory.\nSecond line.", "rules");
        NotePage secret = page(book, "Ending", "The twist.");
        secret.setAiVisible(false);

        String index = StoryNotesService.indexText(book);
        assertTrue(index.contains("- Magic [rules]: Costs memory."));
        assertFalse(index.contains("Ending"));

        String full = StoryNotesService.fullText(book);
        assertTrue(full.contains("## Magic\nTags: rules\n\nCosts memory.\nSecond line."));
        assertFalse(full.contains("twist"));

        secret.setAiVisible(true);
        book.remove(book.byTitle("Magic"));
        book.byTitle("Ending").setAiVisible(false);
        assertEquals("", StoryNotesService.indexText(book));
        assertEquals("", StoryNotesService.fullText(book));
    }

    @Test
    void touchAlwaysMovesTheModifiedTimeForward() {
        NotePage page = new NotePage();
        long before = page.getModified();
        page.touch();
        long first = page.getModified();
        page.touch();

        assertTrue(first > before);
        assertTrue(page.getModified() > first);
    }

    @Test
    void theBookSurvivesItsStoredForm() {
        NoteBook book = new NoteBook();
        NotePage a = page(book, "A", "text [[B]]", "t");
        a.setAiVisible(false);
        a.setChangedByCommand(true);
        page(book, "B", "");

        NoteBook again = new NoteBook(book.toData());

        assertEquals(2, again.size());
        NotePage copy = again.get(a.getUuid());
        assertEquals("text [[B]]", copy.getContent());
        assertFalse(copy.isAiVisible());
        assertTrue(copy.isChangedByCommand());
        assertEquals(List.of("t"), copy.getTags());
    }
}
