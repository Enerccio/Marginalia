package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Item;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationAnswer.Speech;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * What a checked answer does to the text of a part: the speeches of the English source are replaced by their translation, and every replaced
 * speech becomes a passage (its range in the <b>new</b> text, the conlang it holds, the English it was). Pure: nothing is written here.
 *
 * @param source   the part as the model read it, the text the ranges of the answer are in
 * @param stored   the new text of the part: the source with every accepted speech replaced
 * @param passages the passages in the new text, in the order of the text
 */
public record TranslationPlan(String source, String stored, List<Planned> passages) {

    /**
     * A translated speech in the new text.
     *
     * @param start   first character of the conlang in the new text
     * @param end     end of the conlang (exclusive)
     * @param conlang the translation, as it is written into the text
     * @param english the speech as the part had it: the source of the passage
     * @param speech  what the model answered for it
     */
    public record Planned(int start, int end, String conlang, String english, Speech speech) {
    }

    /**
     * @param source   the part as the model read it
     * @param accepted the translated speeches that passed the check ({@link AnswerChecker.Checked#accepted()}), they do not overlap
     */
    public static TranslationPlan make(String source, List<Item> accepted) {
        String text = source == null ? "" : source;
        List<Item> ordered = new ArrayList<>(accepted);
        ordered.sort(Comparator.comparingInt(Item::start));
        StringBuilder stored = new StringBuilder();
        List<Planned> passages = new ArrayList<>();
        int copied = 0;
        for (Item item : ordered) {
            if (item.start() < copied || item.end() > text.length() || item.end() <= item.start()) {
                continue;
            }
            String conlang = item.speech().text().strip();
            if (conlang.isEmpty()) {
                continue;
            }
            stored.append(text, copied, item.start());
            int start = stored.length();
            stored.append(conlang);
            passages.add(new Planned(start, stored.length(), conlang, text.substring(item.start(), item.end()), item.speech()));
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
}
