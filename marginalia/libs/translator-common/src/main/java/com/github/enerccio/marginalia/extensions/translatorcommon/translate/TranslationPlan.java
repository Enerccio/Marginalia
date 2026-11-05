package com.github.enerccio.marginalia.extensions.translatorcommon.translate;


import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * What a checked answer does to the text of a part: the speeches of the English source are replaced by their translation, and every replaced
 * speech becomes a passage (its range in the <b>new</b> text, the translation it holds, the English it was). Pure: nothing is written here.
 *
 * @param source   the part as the model read it, the text the ranges of the answer are in
 * @param stored   the new text of the part: the source with every accepted speech replaced
 * @param passages the passages in the new text, in the order of the text
 */
public record TranslationPlan(String source, String stored, List<Planned> passages) {

    /**
     * A translated speech in the new text.
     *
     * @param start   first character of the translation in the new text
     * @param end     end of the translation (exclusive)
     * @param translated the translation, as it is written into the text
     * @param english the speech as the part had it: the source of the passage
     * @param speech  what the model answered for it
     * @param carried the data of the passage when the speech was translated by an earlier run (another language of the book) and stays as it
     *                is: the passage is made again with it; null for a speech this run translated
     */
    public record Planned(int start, int end, String translated, String english, Speech speech, Span carried) {

        public Planned(int start, int end, String translated, String english, Speech speech) {
            this(start, end, translated, english, speech, null);
        }
    }

    /**
     * A speech that passed the check of the plugin: where its quote is in the source.
     *
     * @param speech what the model answered for it
     * @param start  first character of the quote in the source
     * @param end    end of the quote (exclusive)
     * @param carried the data of the passage of an earlier translation that stays as it is (a speech of another language of the book: its
     *                text is {@code speech.text()}); null for a speech this run translated
     */
    public record Accepted(Speech speech, int start, int end, Span carried) {

        public Accepted(Speech speech, int start, int end) {
            this(speech, start, end, null);
        }
    }

    /**
     * @param source   the part as the model read it
     * @param accepted the translated speeches that passed the check of the plugin, they do not overlap
     */
    public static TranslationPlan make(String source, List<Accepted> accepted) {
        String text = source == null ? "" : source;
        List<Accepted> ordered = new ArrayList<>(accepted);
        ordered.sort(Comparator.comparingInt(Accepted::start));
        StringBuilder stored = new StringBuilder();
        List<Planned> passages = new ArrayList<>();
        int copied = 0;
        for (Accepted item : ordered) {
            if (item.start() < copied || item.end() > text.length() || item.end() <= item.start()) {
                continue;
            }
            String translated = item.speech().text().strip();
            if (translated.isEmpty()) {
                continue;
            }
            stored.append(text, copied, item.start());
            int start = stored.length();
            stored.append(translated);
            passages.add(new Planned(start, stored.length(), translated, text.substring(item.start(), item.end()), item.speech(), item.carried()));
            copied = item.end();
        }
        stored.append(text.substring(copied));
        return new TranslationPlan(text, stored.toString(), passages);
    }

    /**
     * Whether the plan changes anything.
     */
    public boolean isEmpty() {
        return passages.isEmpty();
    }

    /**
     * How many speeches this run translated (not the ones that stay from an earlier run).
     */
    public int translatedCount() {
        return (int) passages.stream().filter(planned -> planned.carried() == null).count();
    }
}
