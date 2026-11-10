package com.github.enerccio.marginalia.utils.docimport;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a Rich Text Format file ({@code .rtf}, what Word, LibreOffice, WordPad and TextEdit write). A paragraph ends with
 * {@code \par}; a heading is a paragraph of the style {@code heading 1} to {@code heading 6} of the stylesheet (by name, so
 * whatever the language) or one with an outline level, {@code Title} is the title. Bold and italic are kept, the letters
 * come from {@code \'hh} (in the code page of {@code \ansicpg}) and {@code &#92;uN}. Fonts, colors, headers, footers,
 * footnotes, pictures, field codes and the other destinations are left out.
 */
final class RtfReader {

    private static final Pattern HEADING_STYLE = Pattern.compile("^heading\\s*(\\d)$");

    // destinations that hold no text of the body (or text that would be read twice)
    private static final Set<String> SKIPPED = Set.of("fonttbl", "colortbl", "info", "pict", "header", "headerl", "headerr", "headerf",
            "footer", "footerl", "footerr", "footerf", "footnote", "annotation", "listtable", "listoverridetable", "rsidtbl",
            "generator", "themedata", "colorschememapping", "datastore", "latentstyles", "fldinst", "object", "objdata", "shppict",
            "bkmkstart", "bkmkend", "xe", "tc", "private", "mmath", "pgdsctbl", "revtbl", "userprops",
            "filetbl", "pnseclvl", "wgrffmtfilter", "template", "xmlnstbl", "fchars", "lchars");

    private static final Map<String, String> SYMBOLS = Map.ofEntries(Map.entry("emdash", "—"), Map.entry("endash", "–"),
            Map.entry("lquote", "‘"), Map.entry("rquote", "’"), Map.entry("ldblquote", "“"),
            Map.entry("rdblquote", "”"), Map.entry("bullet", "•"), Map.entry("emspace", " "), Map.entry("enspace", " "),
            Map.entry("qmspace", " "), Map.entry("tab", " "), Map.entry("line", "\n"));

    private RtfReader() {
    }

    static TextDocument read(byte[] data) {
        String text = new String(data, StandardCharsets.ISO_8859_1);
        if (!text.stripLeading().startsWith("{\\rtf")) {
            throw new IllegalArgumentException("Not an RTF document");
        }
        try {
            return new Parser(text).parse();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Not an RTF document: " + e.getMessage(), e);
        }
    }

    private static final class Group {
        boolean skip;
        boolean sheet;
        boolean bold;
        boolean italic;
        int uc = 1;

        Group copy() {
            Group copy = new Group();
            copy.skip = skip;
            copy.sheet = sheet;
            copy.bold = bold;
            copy.italic = italic;
            copy.uc = uc;
            return copy;
        }
    }

    private static final class Parser {
        private final String source;
        private final Deque<Group> groups = new ArrayDeque<>();
        private final List<TextBlock> blocks = new ArrayList<>();
        private final Map<Integer, String> styleNames = new HashMap<>();
        private final Map<Integer, Integer> styleOutlines = new HashMap<>();
        private final ByteArrayOutputStream pending = new ByteArrayOutputStream();

        private Charset codepage = Charset.forName("windows-1252");
        private RichText paragraph = new RichText();
        private StringBuilder styleName = new StringBuilder();
        private String title;
        private int sheetDepth = -1;
        private int definedStyle = -1;
        private int definedOutline = -1;
        private int style;
        private int outline = -1;
        private int skipChars;
        private int pos;

        Parser(String source) {
            this.source = source;
        }

        TextDocument parse() {
            while (pos < source.length()) {
                char c = source.charAt(pos++);
                switch (c) {
                    case '{' -> groups.push(groups.isEmpty() ? new Group() : groups.peek().copy());
                    case '}' -> {
                        flushBytes();
                        if (groups.size() == sheetDepth) {
                            sheetDepth = -1;
                        }
                        if (!groups.isEmpty()) {
                            groups.pop();
                        }
                    }
                    case '\\' -> control();
                    case '\r', '\n' -> {
                    }
                    default -> character(c);
                }
            }
            flushBytes();
            endParagraph();
            return new TextDocument(title, blocks);
        }

        private Group group() {
            return groups.isEmpty() ? new Group() : groups.peek();
        }

        private boolean inStyleDefinition() {
            return sheetDepth >= 0 && groups.size() == sheetDepth + 1;
        }

        private void character(char c) {
            flushBytes();
            if (skipChars > 0) {
                skipChars--;
                return;
            }
            if (inStyleDefinition()) {
                if (c == ';') {
                    if (definedStyle >= 0) {
                        styleNames.put(definedStyle, styleName.toString().strip());
                        if (definedOutline >= 0) {
                            styleOutlines.put(definedStyle, definedOutline);
                        }
                    }
                    styleName = new StringBuilder();
                    definedStyle = -1;
                    definedOutline = -1;
                } else {
                    styleName.append(c);
                }
                return;
            }
            text(String.valueOf(c));
        }

        private void text(String text) {
            Group group = group();
            if (!group.skip && !group.sheet) {
                paragraph.add(text, group.bold, group.italic);
            }
        }

        private void control() {
            if (pos >= source.length()) {
                return;
            }
            char c = source.charAt(pos++);
            if (Character.isLetter(c) && c < 128) {
                word(c);
                return;
            }
            switch (c) {
                case '\'' -> {
                    int value = pos + 2 <= source.length() ? parseHex(source.substring(pos, pos + 2)) : -1;
                    pos += 2;
                    if (skipChars > 0) {
                        skipChars--;
                    } else if (value >= 0 && !group().skip && !group().sheet) {
                        pending.write(value);
                    }
                }
                case '*' -> group().skip = true;
                case '\\', '{', '}' -> character(c);
                case '~' -> text(" ");
                case '_' -> text("-");
                case '\r', '\n' -> endParagraph();
                default -> {
                    // \- (optional hyphen), \: and the other symbols hold no text
                }
            }
        }

        private void word(char first) {
            flushBytes();
            int start = pos - 1;
            while (pos < source.length() && Character.isLetter(source.charAt(pos)) && source.charAt(pos) < 128) {
                pos++;
            }
            String name = source.substring(start, pos);
            Integer parameter = null;
            int numberStart = pos;
            if (pos < source.length() && (source.charAt(pos) == '-' || Character.isDigit(source.charAt(pos)))) {
                pos++;
                while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
                    pos++;
                }
                try {
                    parameter = Integer.parseInt(source.substring(numberStart, pos));
                } catch (NumberFormatException e) {
                    parameter = 0;
                }
            }
            // one space ends a control word and belongs to it
            if (pos < source.length() && source.charAt(pos) == ' ') {
                pos++;
            }
            apply(name, parameter);
        }

        private void apply(String name, Integer parameter) {
            Group group = group();
            if (SKIPPED.contains(name)) {
                group.skip = true;
                return;
            }
            switch (name) {
                case "stylesheet" -> {
                    group.sheet = true;
                    sheetDepth = groups.size();
                }
                case "ansicpg" -> codepage = forCodepage(parameter == null ? 1252 : parameter);
                case "mac" -> codepage = orCurrent(named("x-MacRoman"));
                case "pc", "pca" -> codepage = orCurrent(named("IBM437"));
                case "b" -> group.bold = parameter == null || parameter != 0;
                case "i" -> group.italic = parameter == null || parameter != 0;
                case "plain" -> {
                    group.bold = false;
                    group.italic = false;
                }
                case "uc" -> group.uc = parameter == null ? 1 : Math.max(0, parameter);
                case "u" -> {
                    if (parameter != null) {
                        if (skipChars > 0) {
                            skipChars--;
                        }
                        text(String.valueOf((char) (parameter < 0 ? parameter + 65536 : parameter)));
                        skipChars = group.uc;
                    }
                }
                case "bin" -> pos = Math.min(source.length(), pos + (parameter == null ? 0 : Math.max(0, parameter)));
                case "s" -> {
                    if (parameter != null) {
                        if (inStyleDefinition()) {
                            definedStyle = parameter;
                        } else {
                            style = parameter;
                        }
                    }
                }
                case "outlinelvl" -> {
                    if (parameter != null) {
                        if (inStyleDefinition()) {
                            definedOutline = parameter;
                        } else {
                            outline = parameter;
                        }
                    }
                }
                case "pard" -> {
                    if (!group.sheet) {
                        style = 0;
                        outline = -1;
                    }
                }
                case "par", "cell", "row", "sect", "page" -> {
                    if (!group.skip && !group.sheet) {
                        endParagraph();
                    }
                }
                default -> {
                    String symbol = SYMBOLS.get(name);
                    if (symbol != null) {
                        text(symbol);
                    }
                }
            }
        }

        private Charset orCurrent(Charset charset) {
            return charset == null ? codepage : charset;
        }

        private void flushBytes() {
            if (pending.size() > 0) {
                String decoded = new String(pending.toByteArray(), codepage);
                pending.reset();
                text(decoded);
            }
        }

        private void endParagraph() {
            flushBytes();
            if (paragraph.isBlank()) {
                paragraph = new RichText();
                return;
            }
            RichText text = paragraph;
            paragraph = new RichText();
            int level = headingLevel();
            if (level < 0) {
                if (title == null) {
                    title = text.plain();
                }
            } else if (level == 0) {
                blocks.add(new TextBlock(0, text.markdown()));
            } else {
                blocks.add(new TextBlock(level, text.plain()));
            }
        }

        // -1 title, 0 paragraph, 1 to 6 heading
        private int headingLevel() {
            String name = styleNames.get(style);
            if (name != null) {
                String lower = name.toLowerCase();
                if (lower.equals("title")) {
                    return -1;
                }
                Matcher matcher = HEADING_STYLE.matcher(lower);
                if (matcher.matches()) {
                    return Math.max(1, Math.min(TextBlock.MAX_LEVEL, Integer.parseInt(matcher.group(1))));
                }
            }
            int level = outline >= 0 ? outline : styleOutlines.getOrDefault(style, -1);
            return level >= 0 && level < TextBlock.MAX_LEVEL ? level + 1 : 0;
        }
    }

    private static int parseHex(String hex) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Charset forCodepage(int codepage) {
        for (String name : new String[]{"windows-" + codepage, "Cp" + codepage, "MS" + codepage}) {
            Charset charset = named(name);
            if (charset != null) {
                return charset;
            }
        }
        return Charset.forName("windows-1252");
    }

    private static Charset named(String name) {
        try {
            return Charset.forName(name);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
