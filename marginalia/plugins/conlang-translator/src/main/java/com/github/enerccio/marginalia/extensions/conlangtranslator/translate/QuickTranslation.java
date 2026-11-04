package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Speakers;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationAnswer.NewWord;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The quick translation: the author writes a text (a phrase, a name, a line he has not written yet) and the model translates it into the
 * language of the book, with the words it had to invent. Nothing is written into the story or the lexicon: the author uses the translation
 * and imports the words he wants.
 */
public final class QuickTranslation {

    /** The instructions, before the language. {@code %1$s} is the name of the language. */
    static final String INSTRUCTIONS = """
            You translate a text from English into a constructed language. You are given a TEXT (a phrase, a sentence, a name, a short passage) and may be told who says it and to whom; translate the TEXT into %1$s, written strictly by the rules of the language.

            How to translate: translate the meaning, not word for word, with the grammar of the language. Use the words of the lexicon exactly as written; a form may have several senses, pick the one the context needs. Choose the form of a word by the register that the speaker and the situation ask for. Never put an English word into the translation, except the proper names of people and places, which stay as they are written. If you need a word that is not in the lexicon, invent one that follows the sound and word-formation rules of the language (a compound of existing roots is preferred), use it, and report it in "newWords". Do not stretch a word of the lexicon to a meaning it does not have: invent and report instead.

            Answer with one JSON object only, no other text, in this form:
            {"text":"...","choice":"...","newWords":[{"word":"...","gloss":"...","category":"...","rationale":"...","register":"","kind":"","variantOf":""}]}
            "text" is the translation, "choice" is one or two short sentences on the main choices of the translation (for the author). For a new word "category" is an existing category of the lexicon or a new one, "rationale" says why this word (which roots or rule), "kind" is root, compound, affix, particle, name, number, phrase or other, and "variantOf" is the word it is another register of, if it is. When you need no new word, "newWords" is empty.""";

    private static final Gson GSON = new Gson();
    private static final Pattern FENCE = Pattern.compile("^```(?:json)?\\s*|\\s*```$");

    private QuickTranslation() {
    }

    /**
     * What the model answered.
     *
     * @param text     the translation
     * @param choice   one or two sentences on the choices, for the author
     * @param newWords the words invented for it
     */
    public record Answer(String text, String choice, List<NewWord> newWords) {
    }

    /**
     * The request.
     *
     * @param language  the language of the book
     * @param speakers  who speaks it in the book (the notes of the characters named in the text or the context are given to the model)
     * @param text      the text to translate
     * @param context   who says it, to whom, in what situation; may be empty
     * @param maxLexiconChars the size above which the lexicon is cut down
     */
    public static TranslationPrompt.Request build(Language language, Speakers speakers, String text, String context, int maxLexiconChars) {
        return build(language, speakers, text, context, null, maxLexiconChars);
    }

    /**
     * The request for a sentence of the story.
     *
     * @param around the story around the sentence (for who speaks to whom; it is not translated); may be empty
     */
    public static TranslationPrompt.Request build(Language language, Speakers speakers, String text, String context, String around,
                                                  int maxLexiconChars) {
        String system = String.format(INSTRUCTIONS, language.getName()) + "\n\n" + TranslationPrompt.languageBlock(language, text, maxLexiconChars);
        StringBuilder user = new StringBuilder();
        String characters = TranslationPrompt.characters(language, speakers, TranslationPrompt.Scene.none(),
                text + "\n" + (context == null ? "" : context) + "\n" + (around == null ? "" : around));
        if (!characters.isEmpty()) {
            user.append("CHARACTERS:\n").append(characters).append('\n');
        }
        if (context != null && !context.isBlank()) {
            user.append("CONTEXT (who says it, to whom, where): ").append(context.strip()).append("\n\n");
        }
        if (around != null && !around.isBlank()) {
            user.append("THE STORY AROUND IT (for who speaks to whom; a text in [[double square brackets]] is the TEXT; do not translate this):\n").append(around.strip()).append("\n\n");
        }
        user.append("TEXT:\n").append(text == null ? "" : text.strip()).append('\n');
        return new TranslationPrompt.Request(system, user.toString());
    }

    /**
     * Reads the answer, leniently like {@link TranslationAnswer#parse}.
     *
     * @return null when no object with a translation could be read in the text
     */
    public static Answer parse(String raw) {
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
            Answer answer = GSON.fromJson(text.substring(from, to + 1), Answer.class);
            if (answer == null || answer.text() == null || answer.text().isBlank()) {
                return null;
            }
            List<NewWord> words = new ArrayList<>();
            if (answer.newWords() != null) {
                answer.newWords().stream().filter(word -> word != null).forEach(words::add);
            }
            return new Answer(answer.text().strip(), answer.choice() == null ? "" : answer.choice().strip(), words);
        } catch (JsonParseException e) {
            return null;
        }
    }
}
