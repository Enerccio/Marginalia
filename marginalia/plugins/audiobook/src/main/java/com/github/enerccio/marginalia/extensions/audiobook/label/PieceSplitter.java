package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits the text of a part into pieces of narration and pieces of direct speech, paragraph by paragraph. Direct speech is what is between
 * straight ({@code "..."}) or curly ({@code “...”}) double quotation marks or the Japanese corner brackets ({@code 「...」}, {@code 『...』}); a markdown heading is narration with its {@code #} removed. A quotation
 * mark that is not closed in its paragraph stays in the narration. Words are never changed or dropped: {@link #normalize} of the pieces joined is
 * {@link #normalize} of the text.
 */
public final class PieceSplitter {

    private static final Pattern SPEECH = Pattern.compile("\"([^\"]+)\"|“([^”]+)”|「([^」]+)」|『([^』]+)』");
    private static final Pattern PARAGRAPH_BREAK = Pattern.compile("\\R\\s*\\R");

    private PieceSplitter() {
    }

    public static List<Piece> split(String text) {
        List<Piece> pieces = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return pieces;
        }
        int paragraphNumber = 0;
        for (String raw : PARAGRAPH_BREAK.split(text.strip())) {
            boolean heading = raw.stripLeading().startsWith("#");
            String paragraph = clean(raw.strip().replaceFirst("^#+\\s*", ""));
            if (paragraph.isEmpty()) {
                continue;
            }
            int position = 0;
            Matcher speech = SPEECH.matcher(paragraph);
            while (speech.find()) {
                add(pieces, Piece.Kind.NARRATION, paragraph.substring(position, speech.start()), paragraphNumber, heading);
                add(pieces, Piece.Kind.SPEECH, spoken(speech), paragraphNumber, false);
                position = speech.end();
            }
            add(pieces, Piece.Kind.NARRATION, paragraph.substring(position), paragraphNumber, heading);
            paragraphNumber++;
        }
        return pieces;
    }

    private static String spoken(Matcher speech) {
        for (int group = 1; group <= speech.groupCount(); group++) {
            if (speech.group(group) != null) {
                return speech.group(group);
            }
        }
        return "";
    }

    private static void add(List<Piece> pieces, Piece.Kind kind, String text, int paragraph, boolean heading) {
        String words = clean(text);
        if (!words.isEmpty()) {
            pieces.add(new Piece(pieces.size(), kind, words, paragraph, heading));
        }
    }

    /** Line breaks inside a paragraph are spaces. */
    private static String clean(String text) {
        return text.replaceAll("\\s*\\R\\s*", " ").strip();
    }

    /**
     * The words of a text for comparing: no quotation marks, no {@code #}, no sound tags, the white space as single spaces. Two texts with the same
     * normal form have the same words.
     */
    public static String normalize(String text) {
        return Tags.strip(text == null ? "" : text).replaceAll("[\"“”「」『』#]", "").replaceAll("\\s+", " ").strip();
    }
}
