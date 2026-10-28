package com.github.enerccio.marginalia.extensions.arcoutline;

import com.github.enerccio.marginalia.extensions.arcoutline.model.Arc;
import com.github.enerccio.marginalia.extensions.arcoutline.model.ArcOutline;
import com.github.enerccio.marginalia.extensions.arcoutline.model.Chapter;
import com.github.enerccio.marginalia.extensions.arcoutline.model.OutlineState;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Which chapter the story is at, and the text about it the model gets when it drafts the instructions of the next turn.
 */
class CurrentChapterTest {

    private static Chapter chapter(String title, String description, String note, OutlineState state) {
        Chapter chapter = new Chapter();
        chapter.setTitle(title);
        chapter.setDescription(description);
        chapter.setNote(note);
        chapter.setState(state);
        return chapter;
    }

    private static Arc arc(String title, String description, OutlineState state, Chapter... chapters) {
        Arc arc = new Arc();
        arc.setTitle(title);
        arc.setDescription(description);
        arc.setState(state);
        arc.getChapters().addAll(java.util.List.of(chapters));
        // setState(FINISHED) finishes the chapters, the tests set the chapters' states themselves afterwards
        return arc;
    }

    private static ArcOutline outline(Arc... arcs) {
        ArcOutline outline = new ArcOutline();
        for (Arc arc : arcs) {
            outline.add(arc);
        }
        return outline;
    }

    @Test
    void theChapterMarkedCurrentIsTheOne() {
        Chapter first = chapter("One", "", "", OutlineState.FINISHED);
        Chapter second = chapter("Two", "", "", OutlineState.CURRENT);
        Chapter third = chapter("Three", "", "", OutlineState.PLANNED);
        Arc arc = arc("Arc", "", OutlineState.CURRENT, first, second, third);

        ArcOutline.Current current = outline(arc).current();

        assertSame(arc, current.arc());
        assertSame(second, current.chapter());
        assertSame(third, current.next());
    }

    @Test
    void withoutACurrentChapterTheFirstOneNotFinishedIs() {
        Chapter first = chapter("One", "", "", OutlineState.FINISHED);
        Chapter second = chapter("Two", "", "", OutlineState.PLANNED);
        Chapter third = chapter("Three", "", "", OutlineState.PLANNED);

        ArcOutline.Current current = outline(arc("Arc", "", OutlineState.CURRENT, first, second, third)).current();

        assertSame(second, current.chapter());
        assertSame(third, current.next());
    }

    @Test
    void theNextChapterCanBeInTheNextArc() {
        Chapter last = chapter("Last", "", "", OutlineState.CURRENT);
        Chapter nextArcFirst = chapter("Next arc first", "", "", OutlineState.PLANNED);

        ArcOutline.Current current = outline(arc("A", "", OutlineState.CURRENT, last), arc("B", "", OutlineState.PLANNED, nextArcFirst)).current();

        assertSame(last, current.chapter());
        assertSame(nextArcFirst, current.next());
    }

    @Test
    void theLastChapterHasNoNextOne() {
        Chapter only = chapter("Only", "", "", OutlineState.CURRENT);

        assertNull(outline(arc("A", "", OutlineState.CURRENT, only)).current().next());
    }

    @Test
    void anArcWithoutChaptersStandsAloneWhenItIsCurrent() {
        Arc empty = arc("Empty", "d", OutlineState.CURRENT);

        ArcOutline.Current current = outline(arc("Done", "", OutlineState.FINISHED), empty).current();

        assertSame(empty, current.arc());
        assertNull(current.chapter());
        assertNull(current.next());
    }

    @Test
    void aFinishedOrEmptyOutlineHasNoCurrentPlace() {
        assertNull(new ArcOutline().current());
        Arc done = arc("Done", "", OutlineState.FINISHED, chapter("One", "", "", OutlineState.FINISHED));
        assertNull(outline(done).current());
        assertNull(ArcOutlineService.currentChapterText(outline(done)));
    }

    @Test
    void theTextHasTheArcTheChapterWithTheirNotesAndTheNextChapterWithoutItsNote() {
        Chapter now = chapter("The siege", "The city is surrounded.", "Mara betrays the gate.", OutlineState.CURRENT);
        Chapter next = chapter("The fall", "The city falls.", "SECRET-NOTE", OutlineState.PLANNED);
        Arc arc = arc("War", "The kingdom at war.", OutlineState.CURRENT, now, next);
        arc.setNote("Arc note.");

        String text = ArcOutlineService.currentChapterText(outline(arc));

        assertTrue(text.contains("Arc: War [Current]\n   The kingdom at war.\n   Notes:\n     Arc note."), text);
        assertTrue(text.contains("Current chapter: The siege [Current]\n   The city is surrounded.\n   Notes:\n     Mara betrays the gate."), text);
        assertTrue(text.contains("Next chapter (the story must not get there yet): The fall [Planned]\n   The city falls."), text);
        assertFalse(text.contains("SECRET-NOTE"), text);
    }

    @Test
    void anArcAloneHasNoChapterSections() {
        String text = ArcOutlineService.currentChapterText(outline(arc("Empty", "Nothing yet.", OutlineState.CURRENT)));

        assertEquals("Arc: Empty [Current]\n   Nothing yet.", text);
    }
}
