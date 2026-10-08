package com.github.enerccio.marginalia.domain.templates;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts moment.js format strings (used by SillyTavern's {{datetimeformat}}) to {@link java.time.format.DateTimeFormatter} patterns.
 */
final class MomentFormat {

    private static final Map<String, String> TOKENS = new LinkedHashMap<>();

    static {
        // longest tokens first
        TOKENS.put("YYYY", "yyyy");
        TOKENS.put("MMMM", "MMMM");
        TOKENS.put("dddd", "EEEE");
        TOKENS.put("SSS", "SSS");
        TOKENS.put("MMM", "MMM");
        TOKENS.put("ddd", "EEE");
        TOKENS.put("DDDD", "DDD");
        TOKENS.put("YY", "yy");
        TOKENS.put("MM", "MM");
        TOKENS.put("DD", "dd");
        TOKENS.put("HH", "HH");
        TOKENS.put("hh", "hh");
        TOKENS.put("mm", "mm");
        TOKENS.put("ss", "ss");
        TOKENS.put("Do", "d"); // ordinal not supported by java.time, plain day number instead
        TOKENS.put("ZZ", "xx");
        TOKENS.put("M", "M");
        TOKENS.put("D", "d");
        TOKENS.put("H", "H");
        TOKENS.put("h", "h");
        TOKENS.put("m", "m");
        TOKENS.put("s", "s");
        TOKENS.put("A", "a");
        TOKENS.put("a", "a");
        TOKENS.put("Z", "xxx");
    }

    private MomentFormat() {
    }

    static String toJavaPattern(String moment) {
        StringBuilder out = new StringBuilder();
        StringBuilder literal = new StringBuilder();
        int i = 0;
        while (i < moment.length()) {
            char c = moment.charAt(i);
            if (c == '[') {
                int end = moment.indexOf(']', i + 1);
                if (end > 0) {
                    literal.append(moment, i + 1, end);
                    i = end + 1;
                    continue;
                }
            }
            String matched = null;
            for (String token : TOKENS.keySet()) {
                if (moment.startsWith(token, i)) {
                    matched = token;
                    break;
                }
            }
            if (matched != null) {
                flushLiteral(out, literal);
                out.append(TOKENS.get(matched));
                i += matched.length();
            } else {
                literal.append(c);
                i++;
            }
        }
        flushLiteral(out, literal);
        return out.toString();
    }

    private static void flushLiteral(StringBuilder out, StringBuilder literal) {
        if (literal.isEmpty()) {
            return;
        }
        String text = literal.toString();
        boolean plain = text.chars().noneMatch(Character::isLetter) && text.indexOf('\'') < 0
                && text.indexOf('[') < 0 && text.indexOf(']') < 0 && text.indexOf('#') < 0
                && text.indexOf('{') < 0 && text.indexOf('}') < 0;
        if (plain) {
            out.append(text);
        } else {
            out.append('\'').append(text.replace("'", "''")).append('\'');
        }
        literal.setLength(0);
    }
}
