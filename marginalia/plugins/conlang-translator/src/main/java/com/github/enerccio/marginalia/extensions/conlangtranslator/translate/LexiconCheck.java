package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.EntryKind;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.LexiconEntry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The check that needs no model: every word of a translation is in the lexicon, a compound of words of the lexicon, a word with the affixes
 * of the language, a word the answer reports as new, or a name from the part. A word that is none of these is one the model invented and did
 * not report. It catches a silent invention; it can not see a wrong meaning (a word stretched to a sense it does not have).
 * <p>
 * Forms are compared as the language says ({@link Language#isHyphenStructural()}). A compound of known parts counts as known even where the
 * hyphen is structural. Retired words are not known: the model is not offered them.
 */
public final class LexiconCheck {

    private static final Pattern TOKEN = Pattern.compile("[\\p{L}'’]+(?:-[\\p{L}'’]+)*");
    private static final int MAX_AFFIXES = 3;

    private final boolean hyphenStructural;
    private final Set<String> forms = new HashSet<>();
    private final List<String> prefixes = new ArrayList<>();
    private final List<String> suffixes = new ArrayList<>();
    private final Set<String> affixes = new HashSet<>();
    private final Set<String> names = new HashSet<>();

    /**
     * @param language  the language
     * @param reported  the forms of the words the answer reports as new (they may take affixes too)
     * @param partText  the part as the model reads it: a capitalized word that is in it is a name
     */
    public LexiconCheck(Language language, List<String> reported, String partText) {
        hyphenStructural = language.isHyphenStructural();
        for (LexiconEntry entry : language.getLexicon()) {
            if (entry.isOffered()) {
                add(entry.getWord(), entry.getKind() == EntryKind.AFFIX);
            }
        }
        reported.forEach(word -> add(word, false));
        Matcher matcher = TOKEN.matcher(partText == null ? "" : partText);
        while (matcher.find()) {
            String token = matcher.group();
            if (Character.isUpperCase(token.codePointAt(0))) {
                names.add(token.toLowerCase(Locale.ROOT));
            }
        }
    }

    private void add(String word, boolean affix) {
        String form = word == null ? "" : word.strip();
        if (form.isEmpty()) {
            return;
        }
        boolean leading = form.startsWith("-");
        boolean trailing = form.endsWith("-");
        if (leading || trailing || affix) {
            String bare = LexiconEntry.normalize(form.replaceAll("^-+|-+$", ""), false);
            if (!bare.isEmpty()) {
                affixes.add(bare);
                // "-is" is a suffix, "Gna-" a prefix; an affix written without a hyphen may be either
                if (trailing || !leading) {
                    prefixes.add(bare);
                }
                if (leading || !trailing) {
                    suffixes.add(bare);
                }
            }
            return;
        }
        forms.add(LexiconEntry.normalize(form, hyphenStructural));
    }

    /**
     * The words of the text that are not known, as they are written, without repeats, in the order they come.
     */
    public List<String> unknown(String text) {
        List<String> unknown = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(text == null ? "" : text);
        while (matcher.find()) {
            String token = matcher.group();
            if (!isKnown(token) && !unknown.contains(token)) {
                unknown.add(token);
            }
        }
        return unknown;
    }

    private boolean isKnown(String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        if (names.contains(lower) && Character.isUpperCase(token.codePointAt(0)) || forms.contains(LexiconEntry.normalize(token, hyphenStructural))) {
            return true;
        }
        if (token.contains("-")) {
            // a compound is known when every part is
            for (String part : token.split("-")) {
                String lowerPart = part.toLowerCase(Locale.ROOT);
                boolean name = names.contains(lowerPart) && Character.isUpperCase(part.codePointAt(0));
                // a part may be an affix written on its own ("Va-elsar", "silva-is")
                if (!part.isEmpty() && !name && !affixes.contains(lowerPart) && !isKnownWord(lowerPart, MAX_AFFIXES)) {
                    return false;
                }
            }
            return true;
        }
        return isKnownWord(lower, MAX_AFFIXES);
    }

    private boolean isKnownWord(String word, int affixesLeft) {
        if (forms.contains(LexiconEntry.normalize(word, false)) || forms.contains(LexiconEntry.normalize(word, true))) {
            return true;
        }
        if (affixesLeft == 0) {
            return false;
        }
        for (String prefix : prefixes) {
            if (word.length() > prefix.length() && word.startsWith(prefix) && isKnownWord(word.substring(prefix.length()), affixesLeft - 1)) {
                return true;
            }
        }
        for (String suffix : suffixes) {
            if (word.length() > suffix.length() && word.endsWith(suffix)
                    && isKnownWord(word.substring(0, word.length() - suffix.length()), affixesLeft - 1)) {
                return true;
            }
        }
        return false;
    }
}
