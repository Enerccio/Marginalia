package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.translatorcommon.translate.ModelJson;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Speech;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * What the model answered to the translation request: an entry for every direct speech of the part. Read leniently (the model may wrap the object in
 * a code fence or write a word before it), checked by {@link AnswerChecker}.
 *
 * @param translations the speeches, in the order of the part
 * @param names         the names and special terms the model had to choose a written form for (proposed for the glossary); never null
 */
public record TranslationAnswer(List<Entry> translations, List<Name> names) {

    private static final Gson GSON = new Gson();

    /**
     * One direct speech as the model answered for it; the fields of {@link Speech} and the reading.
     *
     * @param reading how the translation is read (romaji), empty for none
     */
    public record Entry(String quote, String speaker, String language, String text, String reading, String choice, Boolean understood, String register) {

        public Speech speech() {
            return new Speech(quote, speaker, language, text, choice, understood, register);
        }
    }

    /**
     * A name or a special term the model chose a written form for.
     *
     * @param source  the word as the English writes it
     * @param written how the model wrote it
     * @param reading how it is read, may be empty
     * @param note    a few words, may be empty
     */
    public record Name(String source, String written, String reading, String note) {
    }

    /** An answer with no names, for the code that makes one by hand. */
    public TranslationAnswer(List<Entry> translations) {
        this(translations, List.of());
    }

    /**
     * @return the answer; null when no object with the translations could be read in the text
     */
    public static TranslationAnswer parse(String raw) {
        String json = ModelJson.object(raw);
        if (json == null) {
            return null;
        }
        try {
            TranslationAnswer answer = GSON.fromJson(json, TranslationAnswer.class);
            if (answer == null || answer.translations == null) {
                return null;
            }
            List<Entry> cleaned = new ArrayList<>();
            answer.translations.stream().filter(item -> item != null).forEach(cleaned::add);
            List<Name> names = new ArrayList<>();
            if (answer.names != null) {
                answer.names.stream().filter(name -> name != null && name.source() != null && !name.source().isBlank()
                        && name.written() != null && !name.written().isBlank()).forEach(names::add);
            }
            return new TranslationAnswer(cleaned, names);
        } catch (JsonParseException e) {
            return null;
        }
    }
}
