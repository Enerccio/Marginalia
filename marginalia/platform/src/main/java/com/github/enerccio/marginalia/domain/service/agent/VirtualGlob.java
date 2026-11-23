package com.github.enerccio.marginalia.domain.service.agent;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A pattern of file names for the tools of the model: {@code *} (any characters but {@code /}), {@code **} (also
 * {@code /}), {@code ?} (one character), {@code [abc]} / {@code [!a-c]} and {@code {md,txt}}. A pattern without a
 * {@code /} is matched against the name of a file or folder at any depth below the folder asked for ({@code *.md}); one
 * with a {@code /} against the path below that folder ({@code chapters/*.md}, {@code **}{@code /draft-*}). The case of the letters
 * does not matter, the workspace tells no two names apart by case either.
 * <p>
 * Made on {@link VirtualPath}s only, never on real paths.
 */
public final class VirtualGlob {

    public static final int MAX_LENGTH = 200;

    private final Pattern pattern;
    private final boolean byName;

    private VirtualGlob(Pattern pattern, boolean byName) {
        this.pattern = pattern;
        this.byName = byName;
    }

    /**
     * @return the pattern, null for a blank text (no filter)
     * @throws VirtualFileException when the pattern is not valid
     */
    public static VirtualGlob parse(String glob) {
        if (glob == null || glob.isBlank()) {
            return null;
        }
        String text = glob.strip();
        if (text.length() > MAX_LENGTH) {
            throw new VirtualFileException("The pattern is longer than " + MAX_LENGTH + " characters");
        }
        boolean byName = text.indexOf('/') < 0;
        if (text.startsWith("/")) {
            text = text.substring(1);
        }
        try {
            return new VirtualGlob(Pattern.compile(regex(text), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), byName);
        } catch (PatternSyntaxException e) {
            throw new VirtualFileException("The pattern \"" + VirtualPath.printable(glob) + "\" is not valid");
        }
    }

    /**
     * Whether a file or folder below {@code base} is matched.
     */
    public boolean matches(VirtualPath base, VirtualPath path) {
        if (path.getDepth() <= base.getDepth() || !path.startsWith(base)) {
            return false;
        }
        String text = byName ? path.getName()
                : String.join("/", path.getSegments().subList(base.getDepth(), path.getDepth()));
        return pattern.matcher(text).matches();
    }

    private static String regex(String glob) {
        StringBuilder out = new StringBuilder();
        int braces = 0;
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*' -> {
                    if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                        i++;
                        if (i + 1 < glob.length() && glob.charAt(i + 1) == '/') {
                            i++;
                            out.append("(?:.*/)?");
                        } else {
                            out.append(".*");
                        }
                    } else {
                        out.append("[^/]*");
                    }
                }
                case '?' -> out.append("[^/]");
                case '[' -> {
                    int end = glob.indexOf(']', i + 2);
                    if (end < 0) {
                        throw new VirtualFileException("The pattern \"" + VirtualPath.printable(glob) + "\" has a [ without a ]");
                    }
                    String set = glob.substring(i + 1, end);
                    out.append('[');
                    if (set.startsWith("!") || set.startsWith("^")) {
                        out.append('^');
                        set = set.substring(1);
                    }
                    out.append(set.replace("\\", "\\\\").replace("[", "\\[").replace("&", "\\&")).append(']');
                    i = end;
                }
                case '{' -> {
                    braces++;
                    out.append("(?:");
                }
                case '}' -> {
                    if (braces == 0) {
                        out.append("\\}");
                    } else {
                        braces--;
                        out.append(')');
                    }
                }
                case ',' -> out.append(braces > 0 ? "|" : ",");
                case '\\' -> {
                    if (i + 1 < glob.length()) {
                        i++;
                        out.append(Pattern.quote(String.valueOf(glob.charAt(i))));
                    }
                }
                default -> {
                    if ("\\.[]{}()*+-?^$|".indexOf(c) >= 0) {
                        out.append('\\');
                    }
                    out.append(c);
                }
            }
        }
        if (braces > 0) {
            throw new VirtualFileException("The pattern \"" + VirtualPath.printable(glob) + "\" has a { without a }");
        }
        return out.toString();
    }

}
