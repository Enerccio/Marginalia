package com.github.enerccio.marginalia.extensions.translatorcommon.text;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.UnderstandingLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * The text that tells the story model (and the model that reviews a part) which characters do not follow which language, made from the levels of the
 * {@link CharacterNote}s of the languages of a book. Only a level that says something is written: not set and {@link UnderstandingLevel#FLUENT} are left out.
 * The model decides what to do with it; the author can say more in the instructions.
 */
public final class UnderstandingBlock {

    /** The heading of the block and of the section of facts. */
    public static final String HEADER = "Language understanding";

    private static final String INTRO = "How well the characters follow the languages spoken around them. A character reacts only to what they can follow; "
            + "what they do not follow they hear as sounds (they may still read tone, gestures and the names they know).";

    private UnderstandingBlock() {
    }

    /**
     * A language of the book with the people of it.
     *
     * @param languageName what the language is called in the story
     * @param speakers     the speakers and notes of the language in this book
     */
    public record Entry(String languageName, Speakers speakers) {
    }

    /**
     * The block for these languages, empty when no character has a level that says something.
     *
     * @param entries    the languages of the book
     * @param characters the names to keep (a name or an alias, separated by commas, semicolons or lines); all characters with a level when blank
     */
    public static String build(List<Entry> entries, String characters) {
        List<String> wanted = characters == null ? List.of() : Speakers.namesOf(characters, null);
        List<String> lines = new ArrayList<>();
        for (Entry entry : entries == null ? List.<Entry>of() : entries) {
            if (entry == null || entry.speakers() == null) {
                continue;
            }
            for (CharacterNote note : entry.speakers().getCharacters()) {
                String what = phrase(note.getLevel());
                if (what != null && !note.getName().isBlank() && (wanted.isEmpty() || wanted.stream().anyMatch(note::isCalled))) {
                    lines.add("- " + note.getName() + ", " + entry.languageName() + ": " + what);
                }
            }
        }
        return lines.isEmpty() ? "" : HEADER + "\n" + INTRO + "\n" + String.join("\n", lines);
    }

    /** What a level says, null for the ones that say nothing. */
    static String phrase(UnderstandingLevel level) {
        if (level == null) {
            return null;
        }
        return switch (level) {
            case NONE -> "understands nothing.";
            case FEW_WORDS -> "knows only a few words, cannot follow it.";
            case FOLLOWS_MOST -> "follows most of it, misses details.";
            case FLUENT -> null;
        };
    }
}
