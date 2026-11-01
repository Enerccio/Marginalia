package com.github.enerccio.marginalia.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One way to rename many books: puts a text before or after the name, replaces a text in it, or numbers the books. It only
 * works out the new name of a name, the dialog decides which books and in which order.
 */
public final class MassRename {

    /**
     * The placeholder of the current name in the pattern of {@link #number}.
     */
    public static final String NAME = "{name}";
    /**
     * The placeholder of the number in the pattern of {@link #number}.
     */
    public static final String NUMBER = "{n}";

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{name}|\\{n}");

    private interface Rule {
        String apply(String name, int index);
    }

    private final Rule rule;

    private MassRename(Rule rule) {
        this.rule = rule;
    }

    /**
     * Puts the text before the name.
     */
    public static MassRename prefix(String text) {
        String added = StringUtils.defaultString(text);
        return new MassRename((name, index) -> added + name);
    }

    /**
     * Puts the text after the name.
     */
    public static MassRename postfix(String text) {
        String added = StringUtils.defaultString(text);
        return new MassRename((name, index) -> name + added);
    }

    /**
     * Replaces every occurrence of the text in the name (the text is not a pattern). Nothing is replaced when the text to
     * find is empty.
     */
    public static MassRename replace(String find, String with, boolean matchCase) {
        if (StringUtils.isEmpty(find)) {
            return new MassRename((name, index) -> name);
        }
        Pattern pattern = Pattern.compile(Pattern.quote(find), matchCase ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        String replacement = Matcher.quoteReplacement(StringUtils.defaultString(with));
        return new MassRename((name, index) -> pattern.matcher(name).replaceAll(replacement));
    }

    /**
     * Numbers the books: the new name is the pattern with {@link #NAME} replaced by the current name and {@link #NUMBER} by
     * {@code start + step * index}, left-padded with zeros to the digits.
     *
     * @param pattern the new name, for example {@code "{name} {n}"} or {@code "Chapter {n}"}
     * @param start   the number of the first book
     * @param step    how much the number grows from a book to the next (may be negative)
     * @param digits  the least number of digits, 1 for no padding
     */
    public static MassRename number(String pattern, long start, long step, int digits) {
        String template = StringUtils.defaultString(pattern);
        int width = Math.max(1, digits);
        return new MassRename((name, index) -> {
            String number = pad(start + step * index, width);
            Matcher matcher = PLACEHOLDER.matcher(template);
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group().equals(NAME) ? name : number));
            }
            matcher.appendTail(result);
            return result.toString();
        });
    }

    /**
     * Whether the pattern of a numbering has the placeholder of the number; without it every book would get the same name.
     */
    public static boolean hasNumber(String pattern) {
        return StringUtils.contains(pattern, NUMBER);
    }

    private static String pad(long value, int width) {
        String digits = Long.toString(Math.abs(value));
        return (value < 0 ? "-" : "") + StringUtils.leftPad(digits, width, '0');
    }

    /**
     * @param name  the current name
     * @param index the place of the book among the renamed ones, from 0 (it only matters to a numbering)
     * @return the new name, without the spaces around it
     */
    public String rename(String name, int index) {
        return rule.apply(StringUtils.defaultString(name), index).strip();
    }
}
