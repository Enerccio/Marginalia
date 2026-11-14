package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.domain.service.TemplateService;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.T;

/** The instruction to the model and the pieces it labels. Changing the text changes what the model answers, so {@link #VERSION} goes into the cache key of the labels. */
public final class LabelPrompt {

    /** Bump it when the way the pieces are written changes (the instruction itself is part of the key of the labels): labels made by the old way are not reused. */
    public static final String VERSION = "3";

    /** At most one sound tag per this many words of the part (the model uses them freely otherwise). */
    private static final int WORDS_PER_TAG = 80;
    /**
     * The same for Higgs, whose sounds (and moods) are the reason to choose it: with 80 a dialogue of 170 words got exactly two tags in 3 of 3 samples, the
     * giggle, the laugh, the moan and the crying the text named were left out.
     */
    private static final int WORDS_PER_TAG_HIGGS = 30;

    private LabelPrompt() {
    }

    /** The key of the inherited setting (book, folders, the user, the built-in {@link AudiobookDefaults#labelTemplate()}). */
    public static final String SETTING = "audiobook.labelPrompt";

    /** The variables of the template for the characters of a part, with the tags of Chatterbox Turbo. */
    public static LabelPromptData data(List<String> characters) {
        return data(characters, null);
    }

    /**
     * The variables of the template for the characters of a part.
     *
     * @param model the speech model of the book: its sound tags are offered (a model without tags gets Turbo's, which it drops, as before)
     */
    public static LabelPromptData data(List<String> characters, String model) {
        LabelPromptData data = new LabelPromptData();
        data.setCharacters(String.join(", ", characters));
        data.setDeliveries(deliveries());
        data.setTags(tags(model));
        data.setWordsPerTag(com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig.MODEL_HIGGS.equals(model) ? WORDS_PER_TAG_HIGGS : WORDS_PER_TAG);
        return data;
    }

    /**
     * The system message: the template (blank: the built-in one) with its variables filled in.
     *
     * @throws Exception when the template is not valid
     */
    public static String system(TemplateService templates, String template, List<String> characters) throws Exception {
        return system(templates, template, characters, null);
    }

    /** The system message for a book spoken by the model (its sound tags are the ones offered). */
    public static String system(TemplateService templates, String template, List<String> characters, String model) throws Exception {
        String text = template == null || template.isBlank() ? T.defaults().labelTemplate() : template;
        return templates.processTemplate(text, "audiobookLabelPrompt", data(characters, model)).strip();
    }

    private static String deliveries() {
        StringBuilder all = new StringBuilder();
        for (Delivery delivery : Delivery.values()) {
            all.append(all.isEmpty() ? "" : ", ").append(delivery.name());
        }
        return all.toString();
    }

    /** The tags the labelling may use for the model; a model without tags gets Turbo's (the script drops them for it). */
    public static List<String> tagsOf(String model) {
        List<String> tags = Tags.forModel(model);
        return tags.isEmpty() ? Tags.TURBO : tags;
    }

    private static String tags(String model) {
        StringBuilder all = new StringBuilder();
        for (String tag : tagsOf(model)) {
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
