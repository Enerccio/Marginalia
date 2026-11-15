package com.github.enerccio.marginalia.extensions.arcoutline;

import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ChapterHeadings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The chapter headings written into the story from the parts the arcs and chapters are linked to.
 */
class ChapterHeadingsTest {

    private static Chapter chapter(String title, Integer part, OutlineState state) {
        Chapter chapter = new Chapter();
        chapter.setTitle(title);
        chapter.setPart(part);
        chapter.setState(state);
        return chapter;
    }

    private static Arc arc(String title, Integer part, Chapter... chapters) {
        Arc arc = new Arc();
        arc.setTitle(title);
        arc.setPart(part);
        arc.getChapters().addAll(List.of(chapters));
        return arc;
    }

    private static ArcOutline outline(Arc... arcs) {
        ArcOutline outline = new ArcOutline();
        for (Arc arc : arcs) {
            outline.add(arc);
        }
        return outline;
    }

    private static List<String> lines(ChapterHeadings.Plan plan) {
        return plan.headings().stream().map(h -> h.part() + " " + h.line()).toList();
    }

    @Test
    void chaptersAreNumberedThroughTheBookAndTheArcsAreLeftOut() {
        ArcOutline outline = outline(
                arc("Beginning", 1, chapter("Arrival", 1, OutlineState.FINISHED), chapter("Storm", 4, OutlineState.FINISHED)),
                arc("Middle", null, chapter("Plan", null, OutlineState.PLANNED), chapter("Siege", 9, OutlineState.CURRENT)));

        ChapterHeadings.Plan plan = ChapterHeadings.plan(outline, new HeadingOptions(), "Part", "Chapter");

        assertEquals(List.of("1 # Chapter 1: Arrival", "4 # Chapter 2: Storm", "9 # Chapter 4: Siege"), lines(plan));
        // the planned chapter has no part yet, which is not worth a warning
        assertEquals(0, plan.unlinked());
    }

    @Test
    void arcsGetHeadingsAboveTheirChaptersWithRomanNumbers() {
        ArcOutline outline = outline(
                arc("Beginning", null, chapter("Arrival", 1, OutlineState.FINISHED), chapter("Storm", 4, OutlineState.FINISHED)),
                arc("Middle", 7, chapter("Siege", 9, OutlineState.FINISHED)));
        HeadingOptions options = new HeadingOptions();
        options.setIncludeArcs(true);
        options.setNumbering(HeadingOptions.Numbering.ROMAN);
        options.setRestartInArc(true);

        ChapterHeadings.Plan plan = ChapterHeadings.plan(outline, options, "Part", "Chapter");

        // the first arc is not linked, it goes to the part of its first chapter, before it
        assertEquals(List.of("1 # Part I: Beginning", "1 ## Chapter I: Arrival", "4 ## Chapter II: Storm",
                "7 # Part II: Middle", "9 ## Chapter I: Siege"), lines(plan));
    }

    @Test
    void finishedChaptersWithoutAPartAreCounted() {
        ArcOutline outline = outline(arc("Arc", null, chapter("Lost", null, OutlineState.FINISHED)));

        assertEquals(1, ChapterHeadings.plan(outline, new HeadingOptions(), "", "Chapter").unlinked());
    }

    @Test
    void theTitleFollowsThePrefixTheNumberAndTheOptions() {
        HeadingOptions options = new HeadingOptions();
        assertEquals("4. Storm", ChapterHeadings.title("", 4, options, "Storm"));
        options.setNumbering(HeadingOptions.Numbering.NONE);
        assertEquals("Chapter: Storm", ChapterHeadings.title("Chapter", 4, options, "Storm"));
        assertEquals("Storm", ChapterHeadings.title(" ", 4, options, "Storm"));
        options.setNumbering(HeadingOptions.Numbering.ROMAN);
        options.setWithTitles(false);
        assertEquals("Chapter XIV", ChapterHeadings.title("Chapter", 14, options, "Storm"));
        options.setNumbering(HeadingOptions.Numbering.NONE);
        // nothing at all would leave an empty heading
        assertEquals("14", ChapterHeadings.title("", 14, options, "Storm"));
    }

    @Test
    void romanNumerals() {
        assertEquals("I", ChapterHeadings.roman(1));
        assertEquals("IV", ChapterHeadings.roman(4));
        assertEquals("IX", ChapterHeadings.roman(9));
        assertEquals("XL", ChapterHeadings.roman(40));
        assertEquals("MCMXCIV", ChapterHeadings.roman(1994));
        assertEquals("MMMCMXCIX", ChapterHeadings.roman(3999));
        assertEquals("4000", ChapterHeadings.roman(4000));
        assertEquals("0", ChapterHeadings.roman(0));
    }

    @Test
    void theHeadingsAtTheStartAreReplaced() {
        String text = "# Chapter 1: Old\n\n## Old too\n\nShe ran.\n\n# Inside\n\nOn.";

        assertEquals("# Chapter 1: New\n\nShe ran.\n\n# Inside\n\nOn.", ChapterHeadings.withHeadings(text, List.of("# Chapter 1: New")));
        assertEquals("# A\n\n## B\n\nShe ran.", ChapterHeadings.withHeadings("She ran.", List.of("# A", "## B")));
        assertEquals("# A", ChapterHeadings.withHeadings("", List.of("# A")));
        // a hash without a space is no heading
        assertEquals("#hashtag", ChapterHeadings.stripLeadingHeadings("#hashtag"));
    }

    @Test
    void wipeRemovesEveryHeadingAndItsBlankLine() {
        assertEquals("She ran.\n\nOn.", ChapterHeadings.wipe("# Chapter 1\n\nShe ran.\n\n## Scene\n\nOn."));
        assertEquals("She ran.", ChapterHeadings.wipe("She ran.\n\n# Last"));
        assertEquals("She ran.\n", ChapterHeadings.wipe("# One\nShe ran.\n"));
        String plain = "No headings, #5 is a number.";
        assertSame(plain, ChapterHeadings.wipe(plain));
        assertNull(ChapterHeadings.wipe(null));
    }
}
