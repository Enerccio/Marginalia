package com.github.enerccio.marginalia.extensions.audiobook.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClipLibraryTest {

    @Test
    void aClipKeepsANameAndAFolderAndForgetsThemWhenBlank() {
        AudiobookSettings settings = new AudiobookSettings();
        settings.renameClip("a", "Interview");
        settings.moveClip("a", "John Smith");
        assertEquals("Interview", settings.clipInfo("a").getName());
        assertEquals("John Smith", settings.folderOf("a"));

        settings.renameClip("a", " ");
        settings.moveClip("a", "");
        assertNull(settings.clipInfo("a"), "an entry with nothing in it goes");
    }

    @Test
    void aVersionFollowsTheFolderOfItsOriginalAndTakesItsWords() {
        AudiobookSettings settings = new AudiobookSettings();
        settings.moveClip("orig", "John Smith");
        settings.setClipText("orig", "the words");
        settings.addVersion("orig", "v1", "Film", "MelBand");
        settings.addVersion("v1", "v2", "ffmpeg", "ffmpeg");

        assertEquals("orig", settings.rootOf("v2"));
        assertEquals("John Smith", settings.folderOf("v2"));
        assertEquals("the words", settings.clipText("v2"));

        // moving a version moves its original with every version
        settings.moveClip("v2", "Narrator");
        assertEquals("Narrator", settings.folderOf("orig"));
        assertEquals("Narrator", settings.folderOf("v1"));
    }

    @Test
    void deletingAClipMovesItsVersionsUp() {
        AudiobookSettings settings = new AudiobookSettings();
        settings.moveClip("orig", "John Smith");
        settings.addVersion("orig", "v1", "Film", "MelBand");
        settings.addVersion("v1", "v2", "ffmpeg", "ffmpeg");

        settings.forgetClip("v1");
        assertEquals("orig", settings.clipInfo("v2").getParent());

        settings.forgetClip("orig");
        assertNull(settings.clipInfo("v2").getParent());
        assertEquals("John Smith", settings.folderOf("v2"), "an orphan keeps the folder of its original");
    }

    @Test
    void aShiftedCopyIsAHiddenVersionFoundByItsShiftAndGoesWithItsBase() {
        AudiobookSettings settings = new AudiobookSettings();
        settings.setClipText("men", "The words of the clip.");
        settings.addShifted("men", "p-2.f+0.t100.eF", "men-low", "shift -2 st");

        assertEquals("men-low", settings.shiftedClip("men", "p-2.f+0.t100.eF"));
        assertNull(settings.shiftedClip("men", "p+1.f+0.t100.eF"));
        assertTrue(settings.isShifted("men-low"));
        assertFalse(settings.isShifted("men"));
        assertEquals("The words of the clip.", settings.clipText("men-low"), "it says the words of its base");
        assertEquals(java.util.List.of("men-low"), settings.shiftedOf("men"));

        // a deleted copy is forgotten by its base (it is made again when needed)
        settings.forgetClip("men-low");
        assertNull(settings.shiftedClip("men", "p-2.f+0.t100.eF"));

        // a deleted base takes its copies with it (they are deleted with it)
        settings.addShifted("men", "p-1.f+0.t100.eF", "men-1", "shift -1 st");
        settings.forgetClip("men");
        assertNull(settings.clipInfo("men-1"));
        assertTrue(settings.folders().isEmpty());
    }

    @Test
    void theShiftedCopiesSurviveTheJsonOfTheSettings() {
        AudiobookSettings settings = new AudiobookSettings();
        settings.addShifted("men", "p-2.f+0.t100.eF", "men-low", "shift -2 st");
        com.google.gson.Gson gson = new com.google.gson.Gson();
        AudiobookSettings back = gson.fromJson(gson.toJson(settings), AudiobookSettings.class);
        assertEquals("men-low", back.shiftedClip("men", "p-2.f+0.t100.eF"));
        assertTrue(back.isShifted("men-low"));
    }

    @Test
    void aCircleOfParentsEnds() {
        AudiobookSettings settings = new AudiobookSettings();
        settings.addVersion("a", "b", "x", null);
        settings.addVersion("b", "a", "y", null);
        assertNotNull(settings.rootOf("a"));
    }

    @Test
    void theFoldersAreSortedAsTheUserReadsThem() {
        AudiobookSettings settings = new AudiobookSettings();
        settings.moveClip("a", "zeta");
        settings.moveClip("b", "Alpha");
        settings.moveClip("c", "émile");
        assertEquals(List.of("Alpha", "émile", "zeta"), settings.folders());
    }
}
