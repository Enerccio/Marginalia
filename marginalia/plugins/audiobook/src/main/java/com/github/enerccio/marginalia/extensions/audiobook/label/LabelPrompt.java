package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.domain.service.TemplateService;

import java.util.List;

/** The instruction to the model and the pieces it labels. Changing the text changes what the model answers, so {@link #VERSION} goes into the cache key of the labels. */
public final class LabelPrompt {

    /** Bump it when the way the pieces are written changes (the instruction itself is part of the key of the labels): labels made by the old way are not reused. */
    public static final String VERSION = "3";

    /** At most one sound tag per this many words of the part (the model uses them freely otherwise). */
    private static final int WORDS_PER_TAG = 80;

    private LabelPrompt() {
    }

    /**
     * The built-in instruction, a template: {@code {{characters}}}, {@code {{deliveries}}}, {@code {{tags}}} and {@code {{wordsPerTag}}} are filled in by {@link #data}.
     * A book, a folder or the user can replace it (the inherited setting {@link #SETTING}); the user message with the pieces never changes.
     */
    public static final String DEFAULT_TEMPLATE = """
            You label the pieces of a story for an audiobook with several voices. The pieces are numbered; N is narration, D is direct speech. {{#if characters}}The characters known so far are: {{characters}}. Others may speak; name them as the text does.{{else}}They are not known: name a speaker as the text names them.{{/if}}
            For every piece answer with:
            - "id": the number of the piece;
            - "speaker": for D the character who says it, resolved from the context (a name in the narration around it, a vocative, who answers whom). For N it is "Narrator", except when the context shows that the text is a character's own words: a letter, note, message, sign or inscription that a character wrote and the story quotes, or a character's thoughts. Then name that character (the writer, a signature, the one who thinks), also for several paragraphs in a row. Italics (marks like *...* or _..._) are only a hint: the same markup is also used for emphasis, sounds and foreign words, which the narrator reads. When in doubt it is "Narrator";
            - "delivery": how it should be read ({{deliveries}}) and "intensity" from 0.0 to 1.0;
            - "tagged_text": for N an empty string. For D the piece word for word, unchanged, except that you may insert a sound tag where the context clearly calls for that sound from the speaking character, and rarely (at most one tag per {{wordsPerTag}} words of the whole text). Allowed tags: {{tags}}, written exactly like that. Do not change, add or remove any other word. For every piece that gets no tag use an empty string: never copy the text of a piece that has no tag.
            Pieces marked "context" were labelled already: read them to understand who speaks, do not label them.
            Answer with the JSON only, one entry per piece, in order.""";

    /** The key of the inherited setting (book, folders, the user, the built-in {@link #DEFAULT_TEMPLATE}). */
    public static final String SETTING = "audiobook.labelPrompt";

    /** The variables of the template for the characters of a part. */
    public static LabelPromptData data(List<String> characters) {
        LabelPromptData data = new LabelPromptData();
        data.setCharacters(String.join(", ", characters));
        data.setDeliveries(deliveries());
        data.setTags(tags());
        data.setWordsPerTag(WORDS_PER_TAG);
        return data;
    }

    /**
     * The system message: the template (blank: the built-in one) with its variables filled in.
     *
     * @throws Exception when the template is not valid
     */
    public static String system(TemplateService templates, String template, List<String> characters) throws Exception {
        String text = template == null || template.isBlank() ? DEFAULT_TEMPLATE : template;
        return templates.processTemplate(text, "audiobookLabelPrompt", data(characters)).strip();
    }

    private static String deliveries() {
        StringBuilder all = new StringBuilder();
        for (Delivery delivery : Delivery.values()) {
            all.append(all.isEmpty() ? "" : ", ").append(delivery.name());
        }
        return all.toString();
    }

    private static String tags() {
        StringBuilder all = new StringBuilder();
        for (String tag : Tags.ALLOWED) {
            all.append(all.isEmpty() ? "" : " ").append('[').append(tag).append(']');
        }
        return all.toString();
    }

    /** The pieces of one request: some already labelled ones for the context, then those to label. */
    public static String user(List<Piece> context, List<Piece> pieces) {
        StringBuilder text = new StringBuilder();
        for (Piece piece : context) {
            text.append("(context) ").append(line(piece)).append('\n');
        }
        for (Piece piece : pieces) {
            text.append(line(piece)).append('\n');
        }
        return text.toString().strip();
    }

    private static String line(Piece piece) {
        return "[" + piece.id() + "] " + (piece.kind() == Piece.Kind.NARRATION ? "N" : "D") + ": " + piece.text();
    }
}
