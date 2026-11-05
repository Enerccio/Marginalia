package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Speech;
import com.github.enerccio.marginalia.utils.QuoteFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks what the model answered against the part and the language, and turns it into what the plugin can use. Nothing is written here.
 * <ul>
 *   <li>the <b>quote</b> must be found in the part (letter for letter, else as {@link QuoteFinder} finds it); a speech whose quote is not is dropped;
 *   two translated speeches never overlap (the later is dropped);</li>
 *   <li>the <b>language</b> must be the one of the book or {@code none}; another name drops the speech; a speech in the language without text is dropped;</li>
 *   <li>the <b>script</b> is checked ({@link ScriptCheck}); a speech in the wrong script is kept and flagged, the author decides.</li>
 * </ul>
 */
public final class AnswerChecker {

    private AnswerChecker() {
    }

    /** What is wrong, or worth saying, about a speech. */
    public enum Problem {
        /** The quote is not in the part; the speech is dropped. */
        QUOTE_NOT_FOUND,
        /** The language is not the book's; the speech is dropped. */
        LANGUAGE_UNKNOWN,
        /** A speech in the language has no text; dropped. */
        EMPTY_TEXT,
        /** A speech in the common language came with a text; the text is ignored. */
        TEXT_FOR_NONE,
        /** The quote overlaps the speech before it; dropped. */
        OVERLAP,
        /** The text is not in the script that was asked for ({@link Item#script()}); the speech is kept. */
        WRONG_SCRIPT
    }

    /**
     * One speech of the answer after the check.
     *
     * @param entry      what the model answered
     * @param speech     the same as a {@link Speech} (the object the plan holds)
     * @param start      where the quote is in the part; -1 when it was not found
     * @param end        end of the quote (exclusive); -1 when it was not found
     * @param translated whether the speech is in the language (it will be written into the story); false for {@code none}
     * @param accepted   whether the speech is usable
     * @param problems   what is wrong with it
     * @param script     what is wrong with the script, null for nothing
     */
    public record Item(TranslationAnswer.Entry entry, Speech speech, int start, int end, boolean translated, boolean accepted, List<Problem> problems,
                       ScriptCheck.Problem script) {
    }

    /**
     * @param speeches the speeches in the order of the answer, dropped ones included (their problems say why)
     */
    public record Checked(List<Item> speeches) {

        /**
         * The speeches to write into the story, in the order of the part.
         */
        public List<Item> accepted() {
            return speeches.stream().filter(item -> item.translated() && item.accepted()).sorted((a, b) -> Integer.compare(a.start(), b.start())).toList();
        }

        /**
         * How many speeches that were kept are not in the script that was asked for.
         */
        public int wrongScript() {
            return (int) accepted().stream().filter(item -> item.script() != null).count();
        }
    }

    /**
     * @param partText the part as the model read it (the source), which the quotes are looked up in
     * @param answer   the answer, may be null
     * @param language the language of the book
     */
    public static Checked check(String partText, TranslationAnswer answer, RealLanguage language) {
        String part = partText == null ? "" : partText;
        List<Item> items = new ArrayList<>();
        if (answer == null) {
            return new Checked(items);
        }
        List<int[]> taken = new ArrayList<>();
        for (TranslationAnswer.Entry entry : answer.translations()) {
            Speech speech = entry.speech();
            List<Problem> problems = new ArrayList<>();
            int start = -1;
            int end = -1;
            QuoteFinder.Match match = speech.quote() == null || speech.quote().isBlank() ? null
                    : QuoteFinder.find(part, speech.quote(), null, null, -1);
            if (match == null || match.end() <= match.start()) {
                problems.add(Problem.QUOTE_NOT_FOUND);
            } else {
                start = match.start();
                end = match.end();
            }
            if (speech.isNone()) {
                if (speech.text() != null && !speech.text().isBlank()) {
                    problems.add(Problem.TEXT_FOR_NONE);
                }
                items.add(new Item(entry, speech, start, end, false, start >= 0, problems, null));
                continue;
            }
            if (!isTheLanguage(speech.language(), language)) {
                problems.add(Problem.LANGUAGE_UNKNOWN);
            }
            if (speech.text() == null || speech.text().isBlank()) {
                problems.add(Problem.EMPTY_TEXT);
            }
            int from = start;
            int to = end;
            if (start >= 0 && taken.stream().anyMatch(range -> from < range[1] && to > range[0])) {
                problems.add(Problem.OVERLAP);
            }
            boolean accepted = problems.stream().noneMatch(p -> p == Problem.QUOTE_NOT_FOUND || p == Problem.LANGUAGE_UNKNOWN
                    || p == Problem.EMPTY_TEXT || p == Problem.OVERLAP);
            ScriptCheck.Problem script = null;
            if (accepted) {
                taken.add(new int[]{start, end});
                script = ScriptCheck.problem(language.getScript(), speech.text());
                if (script != null) {
                    problems.add(Problem.WRONG_SCRIPT);
                }
            }
            items.add(new Item(entry, speech, start, end, true, accepted, problems, script));
        }
        return new Checked(items);
    }

    /** The answer names the language as the model was told ({@code tongue}), or as the user calls it ({@code name}). */
    private static boolean isTheLanguage(String answered, RealLanguage language) {
        String wanted = answered == null ? "" : answered.strip();
        return wanted.equalsIgnoreCase(language.getTongue().strip()) || wanted.equalsIgnoreCase(language.getName().strip());
    }
}
