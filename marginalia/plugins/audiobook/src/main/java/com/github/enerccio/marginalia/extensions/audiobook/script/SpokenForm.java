package com.github.enerccio.marginalia.extensions.audiobook.script;

import com.github.enerccio.marginalia.extensions.audiobook.label.Languages;
import com.github.enerccio.marginalia.extensions.audiobook.model.SpokenSettings;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The text of a piece spelled for the ear: what the voice is given instead of what the book says. The book is never changed; the studio shows both.
 * <p>
 * In this order: a scene break (a line of asterisks or dashes) is nothing; links and markdown marks (<code>*</code>, <code>_</code>, backticks) go; the
 * sound tags of the labelling are left alone; the <b>dictionary</b> of the book replaces its words (and what it puts in is not touched by the rules
 * after it); then, for a piece in Latin script and when the English rules are on, a numbered heading ("IV. The Arch" is "Chapter four. The Arch"),
 * abbreviations (Dr., etc.), and numbers (currency, times, ranges, percents, ordinals, years after "in", decimals, plain numbers).
 */
public final class SpokenForm {

    /** Reads the text as it is written (no dictionary, the English rules on): for pieces made without the settings of a book. */
    public static final SpokenForm DEFAULT = of(new SpokenSettings());

    private static final char OPEN = '';
    private static final char CLOSE = '';

    private static final Pattern SCENE_BREAK = Pattern.compile("^[\\s*_\\-–—~=#.•]+$");
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]+)]\\([^)]*\\)");
    private static final Pattern TAG = Pattern.compile("\\[[^\\]\\[]{1,30}]");
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC = Pattern.compile("(?<![\\p{L}\\p{N}*])\\*(?!\\s)(.+?)(?<!\\s)\\*(?![\\p{L}\\p{N}*])");
    private static final Pattern UNDERSCORE = Pattern.compile("(?<![\\p{L}\\p{N}_])_(?!\\s)(.+?)(?<!\\s)_(?![\\p{L}\\p{N}_])");
    private static final Pattern CODE = Pattern.compile("`([^`]+)`");

    private static final Pattern HEADING_NUMBER = Pattern.compile(
            "^(?:(Chapter|Part|Book|Act|Canto)\\s+)?([IVXLCDM]+|\\d{1,3})(\\s*[.:)–—-]\\s*|\\s+|$)(.*)$", Pattern.DOTALL);

    private static final Pattern CURRENCY = Pattern.compile("([$£€])(\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.(\\d{2}))?(?![\\p{L}\\p{N}])");
    private static final Pattern TIME = Pattern.compile("(?<![\\p{L}\\p{N}:.])(\\d{1,2}):(\\d{2})(?![\\p{L}\\p{N}:])");
    private static final Pattern RANGE = Pattern.compile("(?<![\\p{L}\\p{N}])(\\d+)–(\\d+)(?![\\p{L}\\p{N}])");
    private static final Pattern NUMBER = Pattern.compile("(?<![\\p{L}\\p{N}_])(\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.(\\d+))?(%|st|nd|rd|th)?(?![\\p{L}\\p{N}_])");
    private static final Pattern BEFORE_A_YEAR = Pattern.compile("(?i)(?:\\b(?:in|since|from|until|till|by|of|year|during|before|after|circa)|\\bc\\.)\\s+$");

    private static final Map<String, String> ABBREVIATIONS = Map.ofEntries(
            Map.entry("Mr", "Mister"), Map.entry("Mrs", "Missus"), Map.entry("Ms", "Miss"), Map.entry("Dr", "Doctor"), Map.entry("Prof", "Professor"),
            Map.entry("Jr", "Junior"), Map.entry("Sr", "Senior"), Map.entry("Capt", "Captain"), Map.entry("Col", "Colonel"), Map.entry("Gen", "General"),
            Map.entry("Lt", "Lieutenant"), Map.entry("Sgt", "Sergeant"), Map.entry("Mt", "Mount"), Map.entry("vs", "versus"));
    private static final Pattern ABBREVIATION = Pattern.compile("(?<![\\p{L}\\p{N}])(" + String.join("|", ABBREVIATIONS.keySet()) + ")\\.(?![\\p{L}\\p{N}])");
    private static final Pattern ETC_ENDING = Pattern.compile("(?<![\\p{L}\\p{N}])etc\\.(?=\\s+[\\p{Lu}\"“]|\\s*$)");
    private static final Pattern ETC = Pattern.compile("(?<![\\p{L}\\p{N}])etc\\.");
    private static final Pattern EG = Pattern.compile("(?<![\\p{L}\\p{N}])e\\.g\\.,?");
    private static final Pattern IE = Pattern.compile("(?<![\\p{L}\\p{N}])i\\.e\\.,?");

    private final boolean english;
    private final String chapterWord;
    private final List<SpokenSettings.Entry> dictionary;
    private final Pattern dictionaryPattern;
    private final Map<String, String> byWord;

    private SpokenForm(boolean english, String chapterWord, List<SpokenSettings.Entry> dictionary) {
        this.english = english;
        this.chapterWord = chapterWord;
        this.dictionary = dictionary;
        this.byWord = new java.util.HashMap<>();
        for (SpokenSettings.Entry entry : dictionary) {
            byWord.putIfAbsent(entry.getWord(), entry.getSpoken());
        }
        List<String> words = new ArrayList<>(byWord.keySet());
        words.sort(Comparator.comparingInt(String::length).reversed());
        this.dictionaryPattern = words.isEmpty() ? null : Pattern.compile("(?<![\\p{L}\\p{N}_])(?:"
                + String.join("|", words.stream().map(Pattern::quote).toList()) + ")(?![\\p{L}\\p{N}_])");
    }

    public static SpokenForm of(SpokenSettings settings) {
        List<SpokenSettings.Entry> entries = settings.getDictionary().stream().filter(e -> !e.getWord().strip().isEmpty())
                .map(e -> new SpokenSettings.Entry(e.getWord().strip(), e.getSpoken())).toList();
        return new SpokenForm(settings.getRules() == SpokenSettings.Rules.ENGLISH, settings.getChapterWord().strip(), entries);
    }

    /**
     * @param heading whether the piece is a heading (a numbered heading is read as a chapter)
     * @return the text for the voice; empty for a scene break (nothing is said, a longer pause is made by the caller)
     */
    public String apply(String text, boolean heading) {
        if (text == null || text.isBlank() || SCENE_BREAK.matcher(text).matches()) {
            return "";
        }
        List<String> held = new ArrayList<>();
        String work = text.replace(OPEN, ' ').replace(CLOSE, ' ');
        work = LINK.matcher(work).replaceAll("$1");
        work = hold(TAG, work, held);
        work = CODE.matcher(work).replaceAll("$1");
        work = BOLD.matcher(work).replaceAll("$1");
        work = ITALIC.matcher(work).replaceAll("$1");
        work = UNDERSCORE.matcher(work).replaceAll("$1");
        work = work.replace("*", "");
        if (dictionaryPattern != null) {
            work = replaceAll(dictionaryPattern, work, m -> hold(byWord.get(m.group()), held));
        }
        if (english && Languages.ofScript(work) == null) {
            if (heading) {
                work = numberedHeading(work);
            }
            work = abbreviations(work);
            work = numbers(work);
        }
        for (int i = held.size() - 1; i >= 0; i--) {
            work = work.replace(placeholder(i), held.get(i));
        }
        return work.replaceAll("\\s+", " ").strip();
    }

    // --- the places held back from the rules ---

    /** The index is written in private use characters, so that no rule takes it for a number. */
    private static String placeholder(int index) {
        StringBuilder out = new StringBuilder().append(OPEN);
        for (char digit : Integer.toString(index).toCharArray()) {
            out.append((char) ('\uE010' + (digit - '0')));
        }
        return out.append(CLOSE).toString();
    }

    private static String hold(String text, List<String> held) {
        held.add(text);
        return placeholder(held.size() - 1);
    }

    private static String hold(Pattern pattern, String text, List<String> held) {
        return replaceAll(pattern, text, m -> hold(m.group(), held));
    }

    private static String replaceAll(Pattern pattern, String text, java.util.function.Function<Matcher, String> replacement) {
        Matcher m = pattern.matcher(text);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(out, Matcher.quoteReplacement(replacement.apply(m)));
        }
        m.appendTail(out);
        return out.toString();
    }

    // --- headings ---

    /**
     * "IV. The Arch" is "Chapter four. The Arch", "Chapter IV" is "Chapter four", "3. Home" is "Chapter three. Home". A single letter that could be a word
     * ("I remember", "C is for cat") or a number with no mark after it ("3 Men in a Boat") is left alone.
     */
    private String numberedHeading(String text) {
        Matcher m = HEADING_NUMBER.matcher(text.strip());
        if (!m.matches()) {
            return text;
        }
        String label = m.group(1);
        String token = m.group(2);
        String delimiter = m.group(3);
        boolean marked = !delimiter.isBlank();
        boolean atEnd = delimiter.isEmpty();
        boolean digits = Character.isDigit(token.charAt(0));
        long value = digits ? Long.parseLong(token) : roman(token);
        if (value <= 0 || (!digits && value > 100)) {
            return text;
        }
        // no word before it: a number needs a mark after it (or nothing after it), a roman numeral of two letters or more does not
        if (label == null && (digits || token.length() < 2) && !marked && !atEnd) {
            return text;
        }
        String rest = m.group(4).strip();
        String word = label != null ? label : chapterWord;
        String head = (word.isEmpty() ? "" : word + " ") + NumberWords.cardinal(value);
        return rest.isEmpty() ? head + "." : head + ". " + rest;
    }

    /** The value of a well formed roman numeral, 0 for anything else. */
    static int roman(String token) {
        if (!token.matches("M{0,3}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})")) {
            return 0;
        }
        int total = 0;
        int previous = 0;
        for (int i = token.length() - 1; i >= 0; i--) {
            int value = switch (token.charAt(i)) {
                case 'I' -> 1;
                case 'V' -> 5;
                case 'X' -> 10;
                case 'L' -> 50;
                case 'C' -> 100;
                case 'D' -> 500;
                default -> 1000;
            };
            total += value < previous ? -value : value;
            previous = Math.max(previous, value);
        }
        return total;
    }

    // --- abbreviations ---

    private static String abbreviations(String text) {
        String work = replaceAll(ABBREVIATION, text, m -> ABBREVIATIONS.get(m.group(1)));
        work = EG.matcher(work).replaceAll("for example,");
        work = IE.matcher(work).replaceAll("that is,");
        work = ETC_ENDING.matcher(work).replaceAll("et cetera.");
        return ETC.matcher(work).replaceAll("et cetera");
    }

    // --- numbers ---

    private static String numbers(String text) {
        String work = replaceAll(CURRENCY, text, SpokenForm::currency);
        work = replaceAll(TIME, work, SpokenForm::time);
        String ranged = RANGE.matcher(work).replaceAll("$1 to $2");
        return replaceAll(NUMBER, ranged, m -> number(m, text(ranged, m.start())));
    }

    private static String text(String whole, int end) {
        return whole.substring(Math.max(0, end - 14), end);
    }

    private static String currency(Matcher m) {
        String unit = switch (m.group(1)) {
            case "$" -> "dollar";
            case "£" -> "pound";
            default -> "euro";
        };
        boolean pound = m.group(1).equals("\u00a3");
        long whole = Long.parseLong(m.group(2).replace(",", ""));
        StringBuilder out = new StringBuilder(whole > NumberWords.MAX ? NumberWords.digits(m.group(2)) : NumberWords.cardinal(whole));
        out.append(' ').append(unit).append(whole == 1 ? "" : "s");
        if (m.group(3) != null && !m.group(3).equals("00")) {
            int c = Integer.parseInt(m.group(3));
            String small = c == 1 ? (pound ? "penny" : "cent") : (pound ? "pence" : "cents");
            out.append(" and ").append(NumberWords.cardinal(c)).append(' ').append(small);
        }
        return out.toString();
    }

    private static String time(Matcher m) {
        int hour = Integer.parseInt(m.group(1));
        int minute = Integer.parseInt(m.group(2));
        if (hour > 24 || minute > 59) {
            return m.group();
        }
        if (minute == 0) {
            return NumberWords.cardinal(hour) + " o'clock";
        }
        return NumberWords.cardinal(hour) + (minute < 10 ? " oh " : " ") + NumberWords.cardinal(minute);
    }

    private static String number(Matcher m, String before) {
        String integer = m.group(1).replace(",", "");
        String decimals = m.group(2);
        String suffix = m.group(3);
        boolean leadingZero = integer.length() > 1 && integer.startsWith("0");
        boolean tooLong = integer.length() > 15;
        if (leadingZero || tooLong) {
            return NumberWords.digits(integer) + (decimals == null ? "" : " point " + NumberWords.digits(decimals)) + (suffix != null && suffix.equals("%") ? " percent" : "");
        }
        long value = Long.parseLong(integer);
        if (suffix != null && !suffix.equals("%") && decimals == null) {
            return NumberWords.ordinal(value);
        }
        String words;
        if (decimals == null && suffix == null && integer.length() == 4 && value >= 1100 && value <= 2099 && !m.group(1).contains(",")
                && BEFORE_A_YEAR.matcher(before).find()) {
            words = NumberWords.year((int) value);
        } else {
            words = NumberWords.cardinal(value);
            if (decimals != null) {
                words += " point " + NumberWords.digits(decimals);
            }
        }
        return "%".equals(suffix) ? words + " percent" : words;
    }

    /** The entries of the dictionary that were used by the settings, for a message. */
    public int dictionarySize() {
        return dictionary.size();
    }

    @Override
    public String toString() {
        return "SpokenForm[" + (english ? "english" : "none") + ", " + dictionary.size() + " words, " + chapterWord.toLowerCase(Locale.ROOT) + "]";
    }
}
