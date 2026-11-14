package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Where a text names lorebook entries: the links between entries of the lorebook editor (a click on the name of another entry in the
 * content opens it, the footer lists the entries that name this one).
 * <p>
 * An entry is named by its name and by the words of its filter: each keyword of a keyword filter, the whole filter of a text one,
 * what a regular expression one matches. A name or a word is found ignoring case and only as a whole word (not inside a longer word;
 * in Chinese and Japanese, which do not separate words, anywhere). This is close to what recursion follows, not the same: the
 * activation also looks inside words, needs every keyword of an AND filter and checks the tags and the condition.
 */
public final class LoreMentions {

    /** The longest text a regular expression filter may name (a filter that matches whole paragraphs is no link). */
    private static final int MAX_REGEX_SPAN = 200;

    private LoreMentions() {
    }

    /**
     * One place in the text that names an entry.
     *
     * @param start the first character
     * @param end   after the last character
     */
    public record Mention(LorebookEntry entry, int start, int end) {
        public int length() {
            return end - start;
        }
    }

    /**
     * The places where the text names the entry, in the order of the text (they may overlap: a name and a keyword).
     */
    public static List<Mention> find(LorebookEntry entry, String text) {
        List<Mention> found = new ArrayList<>();
        if (entry == null || StringUtils.isEmpty(text)) {
            return found;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String word : wordsOf(entry)) {
            String needle = word.toLowerCase(Locale.ROOT);
            // the lower case of some characters is longer or shorter: then the text itself is searched
            boolean sameLength = lower.length() == text.length();
            String haystack = sameLength ? lower : text;
            String what = sameLength ? needle : word;
            int from = 0;
            while (from <= haystack.length() - what.length()) {
                int at = sameLength ? haystack.indexOf(what, from) : indexOfIgnoreCase(haystack, what, from);
                if (at < 0) {
                    break;
                }
                int end = at + what.length();
                if (wholeWord(text, at, end)) {
                    found.add(new Mention(entry, at, end));
                }
                from = at + 1;
            }
        }
        if (entry.getFilteringMode() == FilteringMode.REGEX && StringUtils.isNotBlank(entry.getFiltering())) {
            try {
                Matcher matcher = Pattern.compile(entry.getFiltering(),
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(text);
                while (matcher.find()) {
                    int length = matcher.end() - matcher.start();
                    if (length > 0 && length <= MAX_REGEX_SPAN) {
                        found.add(new Mention(entry, matcher.start(), matcher.end()));
                    }
                }
            } catch (PatternSyntaxException | StackOverflowError e) {
                // a broken filter names nothing
            }
        }
        found.sort((a, b) -> a.start() != b.start() ? Integer.compare(a.start(), b.start()) : Integer.compare(b.end(), a.end()));
        return found;
    }

    /**
     * Whether the text names the entry anywhere.
     */
    public static boolean names(String text, LorebookEntry entry) {
        return !find(entry, text).isEmpty();
    }

    /**
     * The entry the text names at the position (a caret: the start, inside or the end of the name), {@code null} when none
     * does. When several do, the longest name wins (the first of the entries for the same length).
     *
     * @param self the entry the text belongs to, never a link to itself (may be null)
     */
    public static LorebookEntry entryAt(String text, int position, Collection<LorebookEntry> entries, LorebookEntry self) {
        if (StringUtils.isEmpty(text) || entries == null || position < 0 || position > text.length()) {
            return null;
        }
        Mention best = null;
        for (LorebookEntry entry : entries) {
            if (entry == null || sameEntry(entry, self)) {
                continue;
            }
            for (Mention mention : find(entry, text)) {
                if (mention.start() <= position && position <= mention.end()
                        && (best == null || mention.length() > best.length())) {
                    best = mention;
                }
            }
        }
        return best == null ? null : best.entry();
    }

    /**
     * The entries whose content names the entry, in the order of the list, without the entry itself.
     */
    public static List<LorebookEntry> mentionedIn(LorebookEntry entry, Collection<LorebookEntry> entries) {
        List<LorebookEntry> found = new ArrayList<>();
        if (entry == null || entries == null) {
            return found;
        }
        for (LorebookEntry other : entries) {
            if (other != null && !sameEntry(other, entry) && names(other.getPayload(), entry)) {
                found.add(other);
            }
        }
        return found;
    }

    /**
     * The name and the plain words of the filter (not a regular expression), each once.
     */
    static List<String> wordsOf(LorebookEntry entry) {
        List<String> words = new ArrayList<>();
        add(words, entry.getName());
        String filter = entry.getFiltering();
        if (StringUtils.isNotBlank(filter) && entry.getFilteringMode() != null) {
            switch (entry.getFilteringMode()) {
                case KEYWORDS_OR, KEYWORDS_AND -> {
                    for (String keyword : filter.split(",")) {
                        add(words, keyword);
                    }
                }
                case TEXT -> add(words, filter);
                case REGEX -> {
                    // found by the pattern itself
                }
            }
        }
        return words;
    }

    private static void add(List<String> words, String word) {
        String stripped = StringUtils.strip(word);
        if (StringUtils.isEmpty(stripped)) {
            return;
        }
        for (String known : words) {
            if (known.equalsIgnoreCase(stripped)) {
                return;
            }
        }
        words.add(stripped);
    }

    private static boolean sameEntry(LorebookEntry a, LorebookEntry b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getUuid() != null || b.getUuid() != null) {
            return Objects.equals(a.getUuid(), b.getUuid());
        }
        return a.getId() != null && Objects.equals(a.getId(), b.getId());
    }

    private static int indexOfIgnoreCase(String text, String what, int from) {
        for (int i = from; i <= text.length() - what.length(); i++) {
            if (text.regionMatches(true, i, what, 0, what.length())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Not part of a longer word: the character before and the one after are no letters or digits, unless the text there is in
     * a script written without spaces.
     */
    private static boolean wholeWord(String text, int start, int end) {
        if (start > 0 && joins(text.codePointBefore(start), text.codePointAt(start))) {
            return false;
        }
        return end >= text.length() || !joins(text.codePointBefore(end), text.codePointAt(end));
    }

    private static boolean joins(int left, int right) {
        return Character.isLetterOrDigit(left) && Character.isLetterOrDigit(right) && spaced(left) && spaced(right);
    }

    private static boolean spaced(int codePoint) {
        Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);
        return script != Character.UnicodeScript.HAN && script != Character.UnicodeScript.HIRAGANA
                && script != Character.UnicodeScript.KATAKANA && script != Character.UnicodeScript.THAI;
    }
}
