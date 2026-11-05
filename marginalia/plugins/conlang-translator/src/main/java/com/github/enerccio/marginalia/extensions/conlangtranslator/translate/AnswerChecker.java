package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.LexiconEntry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationAnswer.NewWord;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Speech;
import com.github.enerccio.marginalia.utils.QuoteFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks what the model answered against the part and the language, and turns it into what the plugin can use. Nothing is written here.
 * <ul>
 *   <li>the <b>quote</b> must be found in the part (letter for letter, else as {@link QuoteFinder} finds it); a speech whose quote is not is
 *   dropped. A quote may be a part of a quotation, and entries may adjoin, but two translated speeches never overlap (the later is dropped);</li>
 *   <li>the <b>language</b> must be the one of the book or {@code none}; another name drops the speech; text for {@code none} is ignored, a
 *   speech in the language without text is dropped;</li>
 *   <li>the words of a translation that are not in the lexicon, not reported and not a name are <b>flagged</b> (the model invented them silently);
 *   the speech is kept, the caller decides (the repair run);</li>
 *   <li><b>new words</b> are reported, never dropped for being new: one that is in the lexicon with the same meaning is left out (the entry is
 *   used), the same form with another meaning is a homophone and is added, one without a rationale is added and flagged.</li>
 * </ul>
 */
public final class AnswerChecker {

    private AnswerChecker() {
    }

    /** What is wrong, or worth saying, about a speech or a word. */
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
        /** The translation has words that are not in the lexicon and not reported ({@link Item#unknownWords()}). */
        UNKNOWN_WORDS,
        /** A new word has no rationale. */
        NO_RATIONALE,
        /** A new word has no form or no meaning; dropped. */
        INCOMPLETE_WORD,
        /** The lexicon has this word with this meaning already; the entry is used and the word is left out. */
        WORD_KNOWN
    }

    /**
     * One speech of the answer after the check.
     *
     * @param speech       what the model answered
     * @param start        where the quote is in the part; -1 when it was not found
     * @param end          end of the quote (exclusive); -1 when it was not found
     * @param translated   whether the speech is in the language (it will be written into the story); false for {@code none}
     * @param accepted     whether the speech is usable: a translated one is written into the story, one in the common language only says
     *                     that this passage stays as it is
     * @param problems     what is wrong with it
     * @param unknownWords the words of the translation that are not known (see {@link Problem#UNKNOWN_WORDS})
     */
    public record Item(Speech speech, int start, int end, boolean translated, boolean accepted, List<Problem> problems, List<String> unknownWords) {
    }

    /**
     * A new word after the check.
     */
    public record Word(NewWord word, List<Problem> problems) {
    }

    /**
     * @param speeches the speeches in the order of the answer, dropped ones included (their problems say why)
     * @param words    the new words to add to the lexicon; the ones left out are in {@code leftOut}
     * @param leftOut  the new words that were not kept, with the reason
     */
    public record Checked(List<Item> speeches, List<Word> words, List<Word> leftOut) {

        /**
         * The speeches to write into the story, in the order of the part.
         */
        public List<Item> accepted() {
            return speeches.stream().filter(item -> item.translated() && item.accepted()).sorted((a, b) -> Integer.compare(a.start(), b.start())).toList();
        }

        /**
         * Whether a speech that was kept has a problem that a second try could repair (words that were invented silently).
         */
        public boolean needsRepair() {
            return speeches.stream().anyMatch(item -> item.accepted() && item.problems().contains(Problem.UNKNOWN_WORDS));
        }
    }

    /**
     * @param partText the part as the model read it (the source), which the quotes are looked up in
     * @param answer   the answer, may be null
     * @param language the language of the book
     */
    public static Checked check(String partText, TranslationAnswer answer, Language language) {
        String part = partText == null ? "" : partText;
        List<Word> kept = new ArrayList<>();
        List<Word> leftOut = new ArrayList<>();
        if (answer == null) {
            return new Checked(List.of(), kept, leftOut);
        }
        checkWords(answer.newWords(), language, kept, leftOut);
        LexiconCheck lexicon = new LexiconCheck(language, kept.stream().map(word -> word.word().word()).toList(), part);

        List<Item> items = new ArrayList<>();
        List<int[]> taken = new ArrayList<>();
        for (Speech speech : answer.translations()) {
            List<Problem> problems = new ArrayList<>();
            int found = -1;
            int foundEnd = -1;
            QuoteFinder.Match match = speech.quote() == null || speech.quote().isBlank() ? null
                    : QuoteFinder.find(part, speech.quote(), null, null, -1);
            if (match == null || match.end() <= match.start()) {
                problems.add(Problem.QUOTE_NOT_FOUND);
            } else {
                found = match.start();
                foundEnd = match.end();
            }
            int start = found;
            int end = foundEnd;
            if (speech.isNone()) {
                if (speech.text() != null && !speech.text().isBlank()) {
                    problems.add(Problem.TEXT_FOR_NONE);
                }
                items.add(new Item(speech, start, end, false, start >= 0, problems, List.of()));
                continue;
            }
            boolean sameLanguage = speech.language().strip().equalsIgnoreCase(language.getName().strip());
            if (!sameLanguage) {
                problems.add(Problem.LANGUAGE_UNKNOWN);
            }
            if (speech.text() == null || speech.text().isBlank()) {
                problems.add(Problem.EMPTY_TEXT);
            }
            if (start >= 0 && taken.stream().anyMatch(range -> start < range[1] && end > range[0])) {
                problems.add(Problem.OVERLAP);
            }
            boolean accepted = problems.stream().noneMatch(p -> p == Problem.QUOTE_NOT_FOUND || p == Problem.LANGUAGE_UNKNOWN
                    || p == Problem.EMPTY_TEXT || p == Problem.OVERLAP);
            List<String> unknown = List.of();
            if (accepted) {
                taken.add(new int[]{start, end});
                unknown = lexicon.unknown(speech.text());
                if (!unknown.isEmpty()) {
                    problems.add(Problem.UNKNOWN_WORDS);
                }
            }
            items.add(new Item(speech, start, end, true, accepted, problems, unknown));
        }
        return new Checked(items, kept, leftOut);
    }

    /**
     * Checks only the words an answer reports (a quick translation has no speeches): the same rules as for a translated part.
     */
    public static Checked checkNewWords(List<NewWord> words, Language language) {
        List<Word> kept = new ArrayList<>();
        List<Word> leftOut = new ArrayList<>();
        checkWords(words == null ? List.of() : words, language, kept, leftOut);
        return new Checked(List.of(), kept, leftOut);
    }

    private static void checkWords(List<NewWord> words, Language language, List<Word> kept, List<Word> leftOut) {
        for (NewWord word : words) {
            if (word.word() == null || word.word().isBlank() || word.gloss() == null || word.gloss().isBlank()) {
                leftOut.add(new Word(word, List.of(Problem.INCOMPLETE_WORD)));
                continue;
            }
            boolean known = language.findByForm(word.word()).stream().anyMatch(entry -> entry.hasGloss(word.gloss()))
                    || kept.stream().anyMatch(other -> sameWord(other.word(), word, language));
            if (known) {
                leftOut.add(new Word(word, List.of(Problem.WORD_KNOWN)));
                continue;
            }
            // the same form with another meaning is a homophone, which is normal
            List<Problem> problems = new ArrayList<>();
            if (word.rationale() == null || word.rationale().isBlank()) {
                problems.add(Problem.NO_RATIONALE);
            }
            kept.add(new Word(word, problems));
        }
    }

    private static boolean sameWord(NewWord a, NewWord b, Language language) {
        boolean hyphen = language.isHyphenStructural();
        return LexiconEntry.normalize(a.word(), hyphen).equals(LexiconEntry.normalize(b.word(), hyphen))
                && a.gloss().strip().equalsIgnoreCase(b.gloss().strip());
    }
}
