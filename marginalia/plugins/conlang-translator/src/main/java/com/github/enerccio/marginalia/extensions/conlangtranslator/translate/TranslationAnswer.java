package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * What the model answered to the translation request: an entry for every direct speech of the part, and the words it had to invent. Read
 * leniently (the model may wrap the object in a code fence or write a word before it), checked by {@link AnswerChecker}.
 *
 * @param translations the speeches, in the order of the part
 * @param newWords     the words invented for them
 */
public record TranslationAnswer(List<Speech> translations, List<NewWord> newWords) {

    private static final Gson GSON = new Gson();
    private static final Pattern FENCE = Pattern.compile("^```(?:json)?\\s*|\\s*```$");

    /**
     * One direct speech of the part.
     *
     * @param quote      the speech copied from the part, without the quotation marks
     * @param speaker    who says it
     * @param language   the name of the language it is spoken in, or {@code none} for the language of the story
     * @param text       the speech in the language; empty for {@code none}
     * @param choice     one sentence on the main choices of the translation; for the author, never used
     * @param understood false when the listener does not understand it; null is read as true
     * @param register   the register the model chose, empty for the neutral one
     */
    public record Speech(String quote, String speaker, String language, String text, String choice, Boolean understood, String register) {

        public boolean isNone() {
            return language == null || language.isBlank() || language.strip().equalsIgnoreCase("none");
        }

        public boolean isUnderstood() {
            return understood == null || understood;
        }
    }

    /**
     * A word the speech needed and the lexicon lacked.
     *
     * @param word      the word
     * @param gloss     what it means
     * @param category  the name of a category of the language, or of a new one
     * @param rationale why this word: which rule or roots
     * @param register  its register, empty for the neutral one
     * @param kind      root, compound, affix, particle, name, number, phrase or other; empty when the model did not say
     * @param variantOf the word it is another register of, empty for none
     */
    public record NewWord(String word, String gloss, String category, String rationale, String register, String kind, String variantOf) {
    }

    /**
     * Reads the answer.
     *
     * @return the answer; null when no object with the translations could be read in the text
     */
    public static TranslationAnswer parse(String raw) {
        if (raw == null) {
            return null;
        }
        String text = FENCE.matcher(raw.strip()).replaceAll("");
        int from = text.indexOf('{');
        int to = text.lastIndexOf('}');
        if (from < 0 || to <= from) {
            return null;
        }
        try {
            TranslationAnswer answer = GSON.fromJson(text.substring(from, to + 1), TranslationAnswer.class);
            if (answer == null || answer.translations == null && answer.newWords == null) {
                return null;
            }
            return new TranslationAnswer(clean(answer.translations), clean(answer.newWords));
        } catch (JsonParseException e) {
            return null;
        }
    }

    private static <T> List<T> clean(List<T> list) {
        List<T> cleaned = new ArrayList<>();
        if (list != null) {
            list.stream().filter(item -> item != null).forEach(cleaned::add);
        }
        return cleaned;
    }
}
