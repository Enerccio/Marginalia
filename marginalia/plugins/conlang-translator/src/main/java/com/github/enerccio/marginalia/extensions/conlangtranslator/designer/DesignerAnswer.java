package com.github.enerccio.marginalia.extensions.conlangtranslator.designer;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * What the designer's model answered: a message for the author, and any of new or rewritten rules sections, new words, findings of a review and
 * the translations of sample sentences. One shape for every action; the action says which parts it wants. Read leniently (a code fence or a
 * word before the object is fine), checked by {@link ProposalBuilder}.
 *
 * @param message  what the model wants the author to read (an explanation, an answer to a question)
 * @param sections new rules sections, or rewrites of existing ones
 * @param words    new words
 * @param findings what a review found
 * @param samples  sample sentences translated into the language
 */
public record DesignerAnswer(String message, List<Section> sections, List<Word> words, List<Finding> findings, List<Sample> samples) {

    private static final Gson GSON = new Gson();
    private static final Pattern FENCE = Pattern.compile("^```(?:json)?\\s*|\\s*```$");

    /**
     * @param title    the title of the section
     * @param text     the section
     * @param audience {@code translator}, {@code designer} or {@code nobody}; empty is the translator
     * @param replaces the title of the section of the language this one rewrites; empty for a new section
     */
    public record Section(String title, String text, String audience, String replaces) {
    }

    /**
     * @param word      the word as the story writes it
     * @param gloss     what it means
     * @param category  the name of a category of the language, or of a new one
     * @param rationale why the word is as it is: the rule or the roots
     * @param register  its register; empty for the neutral one
     * @param kind      root, compound, affix, particle, name, number, phrase or other
     * @param variantOf the word it is another register of; empty for none
     */
    public record Word(String word, String gloss, String category, String rationale, String register, String kind, String variantOf) {
    }

    /**
     * @param severity {@code error}, {@code warning} or {@code note}
     * @param where    the section or the word it is about
     * @param problem  what is wrong
     * @param fix      what to do about it
     */
    public record Finding(String severity, String where, String problem, String fix) {
    }

    /**
     * @param source the sentence of the author
     * @param text   the sentence in the language
     * @param note   the choices of the translation, for the author
     */
    public record Sample(String source, String text, String note) {
    }

    /**
     * Reads the answer.
     *
     * @return the answer; null when no object could be read in the text
     */
    public static DesignerAnswer parse(String raw) {
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
            DesignerAnswer answer = GSON.fromJson(text.substring(from, to + 1), DesignerAnswer.class);
            if (answer == null) {
                return null;
            }
            return new DesignerAnswer(answer.message == null ? "" : answer.message.strip(), clean(answer.sections), clean(answer.words),
                    clean(answer.findings), clean(answer.samples));
        } catch (JsonParseException e) {
            return null;
        }
    }

    /** Whether the answer proposes anything to write into the language. */
    public boolean hasChanges() {
        return !sections.isEmpty() || !words.isEmpty();
    }

    private static <T> List<T> clean(List<T> list) {
        List<T> cleaned = new ArrayList<>();
        if (list != null) {
            list.stream().filter(item -> item != null).forEach(cleaned::add);
        }
        return cleaned;
    }
}
