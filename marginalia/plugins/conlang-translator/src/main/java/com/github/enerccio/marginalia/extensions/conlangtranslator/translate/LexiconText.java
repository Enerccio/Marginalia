package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The lexicon as the translator reads it: under the headings of its categories, as the author writes it ({@code [Military & War]}, then
 * {@code drak - war / fight}), which is shorter than a flat list. Words of one concept in several registers are one line:
 * {@code breast: mela (formal), melis (soft), lelya (vulgar)}. Retired words are not offered. The order is stable (the categories in the
 * author's order, the words as the author keeps them, a new word at the end of its category) so a new word changes the request only from
 * its place on and the prefix stays cacheable.
 * <p>
 * A big lexicon is not sent whole: see {@link #select}.
 */
public final class LexiconText {

    /** The heading of the words that have no category. */
    public static final String NO_CATEGORY = "Other";

    private static final Pattern WORD = Pattern.compile("[\\p{L}]+");

    private LexiconText() {
    }

    /**
     * The whole lexicon, or when it is longer than {@code maxChars} a part of it: the categories flagged <i>always send</i>, the categories of
     * the words the speech needs (a word of a gloss is in the text of the part) and the categories of the new and locked words. Whole
     * categories rather than single words, so the model sees the neighbours of a concept and reuses a word instead of inventing a second one.
     *
     * @param partText the part as the model reads it; null for none
     * @param maxChars the size above which the lexicon is cut down; 0 or less never cuts
     */
    public static String select(Language language, String partText, int maxChars) {
        String all = render(language, null);
        if (maxChars <= 0 || all.length() <= maxChars) {
            return all;
        }
        Set<String> words = new HashSet<>();
        Matcher matcher = WORD.matcher(partText == null ? "" : partText.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            words.add(matcher.group());
        }
        Set<String> wanted = new HashSet<>();
        for (Category category : language.getCategories()) {
            if (category.isAlwaysSend()) {
                wanted.add(category.getId());
            }
        }
        for (LexiconEntry entry : language.getLexicon()) {
            if (!entry.isOffered()) {
                continue;
            }
            String category = categoryOf(language, entry);
            boolean needed = entry.getStatus() == EntryStatus.NEW || entry.isLocked() || entry.getSenses().stream()
                    .anyMatch(sense -> glossWords(sense).stream().anyMatch(words::contains));
            if (needed) {
                wanted.add(category);
            }
        }
        return render(language, wanted);
    }

    /**
     * The lexicon under its headings.
     *
     * @param categories the ids of the categories to write ({@code null} for all; the words without a category are {@link #NO_CATEGORY})
     */
    public static String render(Language language, Set<String> categories) {
        Map<String, List<String>> lines = new LinkedHashMap<>();
        for (Category category : language.getCategories()) {
            lines.put(category.getId(), new ArrayList<>());
        }
        lines.put(NO_CATEGORY, new ArrayList<>());
        Set<String> written = new HashSet<>();
        for (LexiconEntry entry : language.getLexicon()) {
            if (!entry.isOffered() || written.contains(entry.getId()) || entry.getWord().isBlank()) {
                continue;
            }
            String category = categoryOf(language, entry);
            List<LexiconEntry> group = language.variantGroup(entry).stream().filter(LexiconEntry::isOffered).toList();
            if (group.size() > 1) {
                group.forEach(member -> written.add(member.getId()));
                if (categories == null || categories.contains(category)) {
                    lines.get(category).add(variantLine(language, group));
                }
            } else {
                written.add(entry.getId());
                if (categories == null || categories.contains(category)) {
                    lines.get(category).add(line(entry));
                }
            }
        }
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, List<String>> group : lines.entrySet()) {
            if (group.getValue().isEmpty()) {
                continue;
            }
            Category category = language.findCategory(group.getKey());
            text.append('[').append(category == null ? NO_CATEGORY : category.getName()).append("]\n");
            group.getValue().forEach(line -> text.append(line).append('\n'));
            text.append('\n');
        }
        return text.toString().strip();
    }

    private static String categoryOf(Language language, LexiconEntry entry) {
        return entry.getCategory() != null && language.findCategory(entry.getCategory()) != null ? entry.getCategory() : NO_CATEGORY;
    }

    /** {@code word - gloss / gloss (verb)}. */
    private static String line(LexiconEntry entry) {
        return form(entry) + " - " + glosses(entry);
    }

    /** {@code breast: mela (formal), melis (soft)}, the concept named by the first gloss of the first word. */
    private static String variantLine(Language language, List<LexiconEntry> group) {
        StringBuilder text = new StringBuilder(glosses(group.getFirst())).append(": ");
        for (int i = 0; i < group.size(); i++) {
            LexiconEntry member = group.get(i);
            if (i > 0) {
                text.append(", ");
            }
            text.append(form(member));
            Register register = language.findRegister(member.getRegister());
            if (register != null) {
                text.append(" (").append(register.getName()).append(')');
            }
        }
        return text.toString();
    }

    private static String form(LexiconEntry entry) {
        return entry.getScript().isBlank() ? entry.getWord() : entry.getWord() + " (" + entry.getScript() + ")";
    }

    private static String glosses(LexiconEntry entry) {
        List<String> senses = new ArrayList<>();
        for (Sense sense : entry.getSenses()) {
            if (!sense.getGloss().isBlank()) {
                senses.add(sense.getPartOfSpeech().isBlank() ? sense.getGloss() : sense.getGloss() + " (" + sense.getPartOfSpeech() + ")");
            }
        }
        return String.join(" / ", senses);
    }

    private static List<String> glossWords(Sense sense) {
        List<String> words = new ArrayList<>();
        Matcher matcher = WORD.matcher(sense.getGloss().toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words;
    }
}
