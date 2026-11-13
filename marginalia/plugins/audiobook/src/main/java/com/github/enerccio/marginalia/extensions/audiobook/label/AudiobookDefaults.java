package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The built-in prompts of the Audiobook, in English; a subclass translates them and {@code AudiobookLocalization.T.defaults()} is the
 * one of the prompt language of the user. The JSON fields and their values ({@code "Narrator"}, N, D, the tags) are the same in every
 * language.
 */
public class AudiobookDefaults {

    /**
     * The built-in instruction of the labelling, a template: {@code {{characters}}}, {@code {{deliveries}}}, {@code {{tags}}} and {@code {{wordsPerTag}}} are filled in by {@link LabelPrompt#data}.
     * A book, a folder or the user can replace it (the inherited setting {@link LabelPrompt#SETTING}); the user message with the pieces never changes.
     */
    public String labelTemplate() {
        return """
            You label the pieces of a story for an audiobook with several voices. The pieces are numbered; N is narration, D is direct speech. {{#if characters}}The characters known so far are: {{characters}}. Others may speak; name them as the text does.{{else}}They are not known: name a speaker as the text names them.{{/if}}
            For every piece answer with:
            - "id": the number of the piece;
            - "speaker": for D the character who says it, resolved from the context (a name in the narration around it, a vocative, who answers whom). For N it is "Narrator", except when the context shows that the text is a character's own words: a letter, note, message, sign or inscription that a character wrote and the story quotes, or a character's thoughts. Then name that character (the writer, a signature, the one who thinks), also for several paragraphs in a row. Italics (marks like *...* or _..._) are only a hint: the same markup is also used for emphasis, sounds and foreign words, which the narrator reads. When in doubt it is "Narrator";
            - "delivery": how it should be read ({{deliveries}}) and "intensity" from 0.0 to 1.0;
            - "tagged_text": for N an empty string. For D the piece word for word, unchanged, except that you may insert a sound tag where the context clearly calls for that sound from the speaking character, and rarely (at most one tag per {{wordsPerTag}} words of the whole text). Allowed tags: {{tags}}, written exactly like that. Do not change, add or remove any other word. For every piece that gets no tag use an empty string: never copy the text of a piece that has no tag.
            Pieces marked "context" were labelled already: read them to understand who speaks, do not label them.
            Answer with the JSON only, one entry per piece, in order.""";
    }
}
