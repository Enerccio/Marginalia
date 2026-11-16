package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.model.WatchedPhrase;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Finds the phrases of a watch list in a text, with no model. A phrase is found without regard to case, its white space matches any run
 * of white space (a line break too), a straight and a curly apostrophe or quote match each other, and a phrase that starts (ends) with a
 * letter or a digit is found only as whole words ("tapestry" is not found in "tapestryish"). A regular expression is used as it is,
 * without regard to case. An expression that takes too long (catastrophic backtracking) is given up on.
 */
public final class PhraseMatcher {

    /** The most hits of one part; a list entry that matches everywhere ({@code .}) does not flood the part with notes. */
    public static final int MAX_HITS = 100;

    /** How long all the phrases may take on one text. */
    private static final long BUDGET_NANOS = 2_000_000_000L;

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

    /**
     * One place a phrase is in the text: [start, end) and the words there.
     */
    public record Hit(WatchedPhrase phrase, int start, int end, String match) {
    }

    private PhraseMatcher() {
    }

    /**
     * Why the entry can't be used, null when it can: the phrase is blank, or the regular expression does not compile or matches the
     * empty text.
     */
    public static String validate(WatchedPhrase phrase) {
        if (phrase == null || StringUtils.isBlank(phrase.getPhrase())) {
            return "empty";
        }
        try {
            Pattern pattern = compile(phrase);
            if (pattern.matcher("").matches()) {
                return "matches the empty text";
            }
            return null;
        } catch (PatternSyntaxException e) {
            return e.getDescription();
        }
    }

    /**
     * The pattern the entry is looked for with.
     *
     * @throws PatternSyntaxException when the regular expression of the entry is not valid
     */
    public static Pattern compile(WatchedPhrase phrase) {
        String text = StringUtils.defaultString(phrase.getPhrase()).strip();
        if (phrase.isRegex()) {
            return Pattern.compile(text, FLAGS);
        }
        StringBuilder regex = new StringBuilder();
        if (!text.isEmpty() && Character.isLetterOrDigit(text.codePointAt(0))) {
            regex.append("(?<![\\p{L}\\p{N}])");
        }
        boolean space = false;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isWhitespace(cp)) {
                space = true;
                continue;
            }
            if (space) {
                regex.append("\\s+");
                space = false;
            }
            switch (cp) {
                case '\'', '‘', '’' -> regex.append("['‘’]");
                case '"', '“', '”' -> regex.append("[\"“”]");
                default -> regex.append(Pattern.quote(new String(Character.toChars(cp))));
            }
        }
        if (!text.isEmpty() && Character.isLetterOrDigit(text.codePointBefore(text.length()))) {
            regex.append("(?![\\p{L}\\p{N}])");
        }
        return Pattern.compile(regex.toString(), FLAGS);
    }

    /**
     * Every place of the text a phrase of the list is in, in the order of the text (then of the list); the entries that are off or can't
     * be used are left out, the empty matches too, and at most {@link #MAX_HITS} are given.
     */
    public static List<Hit> find(String text, List<WatchedPhrase> phrases) {
        if (StringUtils.isEmpty(text) || phrases == null || phrases.isEmpty()) {
            return List.of();
        }
        long deadline = System.nanoTime() + BUDGET_NANOS;
        List<Hit> hits = new ArrayList<>();
        for (WatchedPhrase phrase : phrases) {
            if (phrase == null || phrase.isDisabled() || validate(phrase) != null) {
                continue;
            }
            try {
                Matcher matcher = compile(phrase).matcher(new Deadline(text, deadline));
                while (matcher.find() && hits.size() < MAX_HITS * 4) {
                    if (matcher.end() > matcher.start()) {
                        hits.add(new Hit(phrase, matcher.start(), matcher.end(), matcher.group()));
                    }
                }
            } catch (TimeOut e) {
                // the time is up for this text: what was found stays, the rest is not looked for
                break;
            }
        }
        // stable: the same place keeps the order of the list
        hits.sort(Comparator.comparingInt(Hit::start));
        return hits.size() > MAX_HITS ? Collections.unmodifiableList(new ArrayList<>(hits.subList(0, MAX_HITS))) : hits;
    }

    private static final class TimeOut extends RuntimeException {
        private TimeOut() {
            super("The phrases took too long", null, false, false);
        }
    }

    /** The text, which stops a match that runs past the deadline (the regex engine reads it char by char, backtracking too). */
    private static final class Deadline implements CharSequence {
        private final CharSequence text;
        private final long deadline;
        private int reads;

        private Deadline(CharSequence text, long deadline) {
            this.text = text;
            this.deadline = deadline;
        }

        @Override
        public int length() {
            return text.length();
        }

        @Override
        public char charAt(int index) {
            if ((++reads & 0x3FF) == 0 && System.nanoTime() > deadline) {
                throw new TimeOut();
            }
            return text.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new Deadline(text.subSequence(start, end), deadline);
        }

        @Override
        public String toString() {
            return text.toString();
        }
    }
}
