package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryEntry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.ModelJson;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.List;

/**
 * The quick translation: the author writes a text (a phrase, a name, a line not written yet, or a sentence selected in the story) and the
 * model translates it into the language of the book. Nothing is written by the request itself.
 */
public final class QuickTranslation {

    static final String INSTRUCTIONS = """
            You translate a text from English into %1$s. You are given a TEXT (a phrase, a sentence, a name, a short passage) and may be told who says it and to whom; translate the TEXT into %1$s, %2$s

            Write it the way a native author would write that person saying it, not word for word: the level of speech follows the relationship of the speaker and the listener and the voice of the speaker.

            Answer with one JSON object only, no other text, in this form:
            {"text":"...","reading":"","choice":"..."}
            "text" is the translation, "reading": %3$s "choice" is one or two short sentences on the main choices of the translation (for the author).""";

    private static final Gson GSON = new Gson();

    private QuickTranslation() {
    }

    /**
     * What the model answered.
     *
     * @param text    the translation
     * @param reading how it is read, empty for none
     * @param choice  one or two sentences on the choices, for the author
     */
    public record Answer(String text, String reading, String choice) {
    }

    /**
     * The request.
     *
     * @param context who says it, to whom, in what situation; may be empty
     */
    public static TranslationPrompt.Request build(RealLanguage language, Speakers speakers, String text, String context) {
        return build(language, speakers, text, context, null);
    }

    /**
     * The request for a sentence of the story.
     *
     * @param around the story around the sentence (for who speaks to whom; it is not translated); may be empty
     */
    public static TranslationPrompt.Request build(RealLanguage language, Speakers speakers, String text, String context, String around) {
        return build(language, List.of(), speakers, text, context, around);
    }

    /**
     * @param glossary the active entries of the merged glossary; only the ones the text (and what is around it) has are sent
     */
    public static TranslationPrompt.Request build(RealLanguage language, List<GlossaryEntry> glossary, Speakers speakers, String text, String context, String around) {
        String system = String.format(INSTRUCTIONS, language.getTongue(), TranslationPrompt.scriptRule(language.getScript()),
                TranslationPrompt.readingRule(language)) + "\n\n" + TranslationPrompt.languageBlock(language)
                + TranslationPrompt.glossaryBlock(Glossary.relevant(glossary, text + "\n" + (context == null ? "" : context)));
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
     * @return null when no object with a translation could be read in the text
     */
    public static Answer parse(String raw) {
        String json = ModelJson.object(raw);
        if (json == null) {
            return null;
        }
        try {
            Answer answer = GSON.fromJson(json, Answer.class);
            if (answer == null || answer.text() == null || answer.text().isBlank()) {
                return null;
            }
            return new Answer(answer.text().strip(), answer.reading() == null ? "" : answer.reading().strip(),
                    answer.choice() == null ? "" : answer.choice().strip());
        } catch (JsonParseException e) {
            return null;
        }
    }
}
