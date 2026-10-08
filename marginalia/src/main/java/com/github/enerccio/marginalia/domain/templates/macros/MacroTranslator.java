package com.github.enerccio.marginalia.domain.templates.macros;

import com.github.enerccio.marginalia.domain.templates.macros.Macros.MacroDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates SillyTavern macro syntax into Handlebars syntax, so a single Handlebars render evaluates both.
 * <p>
 * Supported SillyTavern syntax:
 * <ul>
 *     <li>case-insensitive macro names: {{User}}, {{USER}}</li>
 *     <li>arguments: {{macro arg}}, {{macro::a::b}}, legacy {{macro:arg}}</li>
 *     <li>nesting: {{getvar::{{char}}_mood}}</li>
 *     <li>scoped macros: {{setvar name}}content{{/setvar}}, with the # flag to keep whitespace</li>
 *     <li>conditionals: {{if cond}}...{{else}}...{{/if}}, {{if !cond}}, conditions may be macros, variable shorthands
 *     or template variables</li>
 *     <li>comments {{// ...}} and {{//}}...{{///}}</li>
 *     <li>variable shorthands {{.local}}, {{$global}} with all operators (=, ++, --, +=, -=, ||, ??, ||=, ??=, ==, !=, &gt;, &gt;=, &lt;, &lt;=)</li>
 *     <li>escaping \{\{notAMacro\}\}</li>
 *     <li>legacy markers &lt;USER&gt;, &lt;BOT&gt;, &lt;CHAR&gt;, &lt;GROUP&gt;, &lt;CHARIFNOTGROUP&gt;</li>
 * </ul>
 * Everything that is not a known macro is left untouched for Handlebars ({{variable}}, {{#section}}...{{/section}},
 * {{#if}}, {{#each}}, {{.}}, comments, partials...). Unknown SillyTavern-looking macros with :: arguments are kept as
 * literal text, same as SillyTavern does.
 * <p>
 * Arguments are never inlined into the Handlebars source; they are stored in a literal table and referenced through the
 * {@code st_lit} helper, so no user text can break the generated template.
 */
public final class MacroTranslator {

    public static final String HELPER_PREFIX = "st_";
    static final String LIT = HELPER_PREFIX + "lit";
    static final String CONCAT = HELPER_PREFIX + "concat";
    static final String PROP = HELPER_PREFIX + "prop";
    static final String COND = HELPER_PREFIX + "cond";
    static final String VAR = HELPER_PREFIX + "var";
    static final String IF = HELPER_PREFIX + Macros.IF;

    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z][A-Za-z0-9_-]*");
    private static final Pattern PLAIN_NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9_-]*$");
    private static final Pattern PATH = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");
    private static final Pattern SHORTHAND = Pattern.compile("^([.$])([A-Za-z](?:[A-Za-z0-9_-]*[A-Za-z0-9])?)\\s*(.*)$", Pattern.DOTALL);
    private static final Pattern COMMENT_CLOSE = Pattern.compile("^/\\s*//$");
    private static final String[] OPERATORS = {"||=", "??=", "++", "--", "+=", "-=", "||", "??", "==", "!=", ">=", "<=", "=", ">", "<"};
    private static final String[][] LEGACY = {
            {"<CHARIFNOTGROUP>", "charIfNotGroup"},
            {"<USER>", "user"},
            {"<BOT>", "char"},
            {"<CHAR>", "char"},
            {"<GROUP>", "group"},
    };

    /**
     * @param source   handlebars template source
     * @param literals literal table referenced by {@code st_lit} helper calls
     */
    public record Result(String source, List<String> literals) {
    }

    public static Result translate(String template) {
        MacroTranslator translator = new MacroTranslator();
        String source = translator.translateText(template == null ? "" : template);
        return new Result(source, List.copyOf(translator.literals));
    }

    private final List<String> literals = new ArrayList<>();
    private int site = 0;

    private MacroTranslator() {
    }

    // ------------------------------------------------------------------------------------------------------------
    // Scanning
    // ------------------------------------------------------------------------------------------------------------

    private enum SegmentKind {
        TEXT, VERBATIM, ESCAPED, LEGACY, TAG
    }

    /**
     * @param body tag body for TAG, literal value for ESCAPED, macro name for LEGACY
     */
    private record Segment(SegmentKind kind, int start, int end, String body) {
    }

    private static List<Segment> scan(String s) {
        List<Segment> segments = new ArrayList<>();
        int n = s.length();
        int i = 0;
        int textStart = 0;
        while (i < n) {
            Segment special = null;
            if (s.startsWith("\\{\\{", i)) {
                special = new Segment(SegmentKind.ESCAPED, i, i + 4, "{{");
            } else if (s.startsWith("\\}\\}", i)) {
                special = new Segment(SegmentKind.ESCAPED, i, i + 4, "}}");
            } else if (s.startsWith("\\{{", i)) {
                // handlebars escape, handlebars will output the mustache literally
                special = new Segment(SegmentKind.VERBATIM, i, i + 3, null);
            } else if (s.startsWith("{{!--", i)) {
                int end = s.indexOf("--}}", i + 5);
                special = new Segment(SegmentKind.VERBATIM, i, end < 0 ? n : end + 4, null);
            } else if (s.startsWith("{{{", i)) {
                int end = s.indexOf("}}}", i + 3);
                special = new Segment(SegmentKind.VERBATIM, i, end < 0 ? n : end + 3, null);
            } else if (s.startsWith("{{", i)) {
                int end = findTagEnd(s, i);
                if (end < 0) {
                    break; // unterminated, rest is text
                }
                special = new Segment(SegmentKind.TAG, i, end + 2, s.substring(i + 2, end));
            } else if (s.charAt(i) == '<') {
                for (String[] legacy : LEGACY) {
                    if (s.regionMatches(true, i, legacy[0], 0, legacy[0].length())) {
                        special = new Segment(SegmentKind.LEGACY, i, i + legacy[0].length(), legacy[1]);
                        break;
                    }
                }
            }

            if (special == null) {
                i++;
                continue;
            }
            if (textStart < special.start()) {
                segments.add(new Segment(SegmentKind.TEXT, textStart, special.start(), null));
            }
            segments.add(special);
            i = special.end();
            textStart = i;
        }
        if (textStart < n) {
            segments.add(new Segment(SegmentKind.TEXT, textStart, n, null));
        }
        return segments;
    }

    /**
     * @return index of the closing "}}" of tag starting at {@code start}, respecting nested tags, or -1
     */
    private static int findTagEnd(String s, int start) {
        int depth = 0;
        int j = start;
        int n = s.length();
        while (j < n - 1) {
            if (s.startsWith("{{", j)) {
                depth++;
                j += 2;
            } else if (s.startsWith("}}", j)) {
                depth--;
                if (depth == 0) {
                    return j;
                }
                j += 2;
            } else {
                j++;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Tag parsing
    // ------------------------------------------------------------------------------------------------------------

    private enum TagKind {
        /**
         * not ours, leave for handlebars
         */
        PASS,
        COMMENT,
        COMMENT_BLOCK,
        COMMENT_CLOSE,
        ELSE,
        /**
         * looks like SillyTavern macro with arguments, but is unknown - output literally
         */
        UNKNOWN,
        SHORTHAND,
        MACRO,
        CLOSE
    }

    private record Shorthand(String scope, String name, String operator, String value) {

        boolean needsValue() {
            return !operator.isEmpty() && !operator.equals("++") && !operator.equals("--");
        }
    }

    private record TagInfo(TagKind kind, String raw, String lname, boolean preserve, boolean closing,
                           boolean handlebarsOpen, MacroDefinition definition, List<String> args,
                           Shorthand shorthand) {

        static TagInfo of(TagKind kind, String raw) {
            return new TagInfo(kind, raw, null, false, false, false, null, List.of(), null);
        }

        boolean opensScope() {
            if (kind != TagKind.MACRO) {
                return false;
            }
            if (Macros.IF.equals(lname)) {
                return args.size() < 2;
            }
            return definition.acceptsArguments() && args.size() < definition.minArgs();
        }
    }

    private static TagInfo parseTag(String body) {
        String raw = "{{" + body + "}}";
        String t = body.strip();

        if (COMMENT_CLOSE.matcher(t).matches()) {
            return TagInfo.of(TagKind.COMMENT_CLOSE, raw);
        }
        if (t.startsWith("//")) {
            return TagInfo.of(t.equals("//") ? TagKind.COMMENT_BLOCK : TagKind.COMMENT, raw);
        }
        if (t.isEmpty() || "!>&~".indexOf(t.charAt(0)) >= 0) {
            return TagInfo.of(TagKind.PASS, raw);
        }
        if (t.equalsIgnoreCase("else")) {
            return TagInfo.of(TagKind.ELSE, raw);
        }

        boolean preserve = false;
        boolean closing = false;
        boolean inverse = false;
        int p = 0;
        while (p < t.length()) {
            char c = t.charAt(p);
            if (c == '#') {
                preserve = true;
            } else if (c == '/') {
                closing = true;
            } else if (c == '^') {
                inverse = true;
            } else if (c != '?' && !Character.isWhitespace(c)) {
                break;
            }
            p++;
        }
        String rest = t.substring(p);
        boolean handlebarsOpen = (preserve || inverse) && !closing;

        if (!closing && !inverse && !rest.isEmpty() && (rest.charAt(0) == '.' || rest.charAt(0) == '$')) {
            Shorthand shorthand = parseShorthand(rest);
            if (shorthand != null) {
                return new TagInfo(TagKind.SHORTHAND, raw, null, preserve, false, false, null, List.of(), shorthand);
            }
            return TagInfo.of(TagKind.PASS, raw);
        }

        Matcher m = IDENTIFIER.matcher(rest);
        if (!m.find()) {
            return TagInfo.of(TagKind.PASS, raw);
        }
        String name = m.group();
        String lname = name.toLowerCase(Locale.ROOT);
        String argsPart = rest.substring(name.length());
        MacroDefinition definition = Macros.find(lname);

        if (definition == null || inverse) {
            TagKind kind = !closing && !inverse && definition == null && argsPart.stripLeading().startsWith(":") ? TagKind.UNKNOWN : TagKind.PASS;
            return new TagInfo(kind, raw, lname, preserve, closing, handlebarsOpen, null, List.of(), null);
        }
        if (closing) {
            return new TagInfo(TagKind.CLOSE, raw, lname, false, true, false, definition, List.of(), null);
        }
        if (preserve && (Macros.IF.equals(lname) || !definition.acceptsArguments())) {
            // handlebars block ({{#if}}) or section over a value
            return new TagInfo(TagKind.PASS, raw, lname, true, false, true, null, List.of(), null);
        }
        List<String> args = parseArgs(argsPart);
        if (args == null) {
            return new TagInfo(TagKind.PASS, raw, lname, preserve, false, handlebarsOpen, null, List.of(), null);
        }
        return new TagInfo(TagKind.MACRO, raw, lname, preserve, false, false, definition, args, null);
    }

    private static Shorthand parseShorthand(String text) {
        Matcher m = SHORTHAND.matcher(text);
        if (!m.matches()) {
            return null;
        }
        String scope = m.group(1).equals("$") ? "global" : "local";
        String rest = m.group(3);
        if (rest.isEmpty()) {
            return new Shorthand(scope, m.group(2), "", "");
        }
        for (String op : OPERATORS) {
            if (rest.startsWith(op)) {
                String value = rest.substring(op.length()).strip();
                Shorthand shorthand = new Shorthand(scope, m.group(2), op, value);
                if (!shorthand.needsValue() && !value.isEmpty()) {
                    return null;
                }
                return shorthand;
            }
        }
        return null;
    }

    /**
     * @return arguments or null if the text after macro name is not a valid argument list
     */
    private static List<String> parseArgs(String argsPart) {
        if (argsPart.isBlank()) {
            return new ArrayList<>();
        }
        String inner;
        String stripped = argsPart.stripLeading();
        if (stripped.startsWith("::")) {
            return splitTop(stripped.substring(2));
        } else if (argsPart.startsWith(":")) {
            inner = argsPart.substring(1);
        } else if (Character.isWhitespace(argsPart.charAt(0))) {
            inner = argsPart.strip();
        } else {
            return null;
        }
        if (indexOfTop(inner, "::", 0) >= 0) {
            return splitTop(inner);
        }
        List<String> result = new ArrayList<>();
        result.add(inner.strip());
        return result;
    }

    private static int indexOfTop(String s, String needle, int from) {
        int depth = 0;
        for (int i = from; i < s.length(); i++) {
            if (s.startsWith("{{", i)) {
                depth++;
                i++;
            } else if (s.startsWith("}}", i) && depth > 0) {
                depth--;
                i++;
            } else if (depth == 0 && s.startsWith(needle, i)) {
                return i;
            }
        }
        return -1;
    }

    private static List<String> splitTop(String s) {
        List<String> result = new ArrayList<>();
        int from = 0;
        while (true) {
            int idx = indexOfTop(s, "::", from);
            if (idx < 0) {
                result.add(s.substring(from).strip());
                return result;
            }
            result.add(s.substring(from, idx).strip());
            from = idx + 2;
        }
    }

    /**
     * SillyTavern scoped content trimming: dedent by indentation of first non-empty line and trim.
     */
    static String dedentTrim(String content) {
        if (content == null) {
            return null;
        }
        String[] lines = content.split("\n", -1);
        String indent = null;
        for (String line : lines) {
            if (!line.isBlank()) {
                int k = 0;
                while (k < line.length() && (line.charAt(k) == ' ' || line.charAt(k) == '\t')) {
                    k++;
                }
                indent = line.substring(0, k);
                break;
            }
        }
        if (indent == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!indent.isEmpty() && line.startsWith(indent)) {
                line = line.substring(indent.length());
            }
            sb.append(line);
            if (i < lines.length - 1) {
                sb.append('\n');
            }
        }
        return sb.toString().strip();
    }

    // ------------------------------------------------------------------------------------------------------------
    // Emitting
    // ------------------------------------------------------------------------------------------------------------

    private int nextSite() {
        return site++;
    }

    private String lit(String value) {
        literals.add(value == null ? "" : value);
        return "(" + LIT + " " + (literals.size() - 1) + ")";
    }

    private String litStatement(String value) {
        literals.add(value == null ? "" : value);
        return "{{" + LIT + " " + (literals.size() - 1) + "}}";
    }

    private String translateText(String s) {
        List<Segment> segments = scan(s);
        StringBuilder out = new StringBuilder();
        for (int idx = 0; idx < segments.size(); idx++) {
            Segment seg = segments.get(idx);
            switch (seg.kind()) {
                case TEXT, VERBATIM -> out.append(s, seg.start(), seg.end());
                case ESCAPED -> out.append(litStatement(seg.body()));
                case LEGACY -> out.append("{{").append(Macros.helperName(Macros.find(seg.body()).name()))
                        .append(' ').append(nextSite()).append("}}");
                case TAG -> {
                    TagInfo info = parseTag(seg.body());
                    switch (info.kind()) {
                        case PASS, CLOSE -> out.append(info.raw());
                        case COMMENT, COMMENT_CLOSE -> {
                        }
                        case COMMENT_BLOCK -> {
                            for (int j = idx + 1; j < segments.size(); j++) {
                                Segment candidate = segments.get(j);
                                if (candidate.kind() == SegmentKind.TAG && parseTag(candidate.body()).kind() == TagKind.COMMENT_CLOSE) {
                                    idx = j;
                                    break;
                                }
                            }
                        }
                        case ELSE -> out.append("{{else}}");
                        case UNKNOWN -> out.append(litStatement(info.raw()));
                        case SHORTHAND -> out.append(shorthandStatement(info.shorthand()));
                        case MACRO -> idx = emitMacro(s, segments, idx, info, out);
                    }
                }
            }
        }
        return out.toString();
    }

    private int findClose(List<Segment> segments, int from, String lname) {
        int depth = 0;
        for (int j = from; j < segments.size(); j++) {
            Segment seg = segments.get(j);
            if (seg.kind() != SegmentKind.TAG) {
                continue;
            }
            TagInfo info = parseTag(seg.body());
            if (!lname.equals(info.lname())) {
                continue;
            }
            if (info.kind() == TagKind.CLOSE || (info.kind() == TagKind.PASS && info.closing())) {
                if (depth == 0) {
                    return info.kind() == TagKind.CLOSE ? j : -1;
                }
                depth--;
            } else if (info.handlebarsOpen() || info.opensScope()) {
                depth++;
            }
        }
        return -1;
    }

    private int emitMacro(String s, List<Segment> segments, int idx, TagInfo info, StringBuilder out) {
        MacroDefinition def = info.definition();
        List<String> args = info.args();
        String helper = Macros.helperName(def.name());

        if (Macros.IF.equals(def.name())) {
            if (args.isEmpty() || args.getFirst().isEmpty()) {
                out.append(litStatement(info.raw()));
                return idx;
            }
            if (args.size() >= 2) {
                String content = String.join("::", args.subList(1, args.size()));
                emitIf(args.getFirst(), content, true, out);
                return idx;
            }
            int close = findClose(segments, idx + 1, def.name());
            if (close < 0) {
                out.append(litStatement(info.raw()));
                return idx;
            }
            emitIf(args.getFirst(), s.substring(segments.get(idx).end(), segments.get(close).start()), info.preserve(), out);
            return close;
        }

        if (info.opensScope()) {
            int close = findClose(segments, idx + 1, def.name());
            if (close >= 0) {
                String content = s.substring(segments.get(idx).end(), segments.get(close).start());
                if (!info.preserve()) {
                    content = dedentTrim(content);
                }
                out.append("{{#").append(helper).append(' ').append(nextSite());
                for (String arg : args) {
                    out.append(' ').append(argExpr(arg));
                }
                out.append("}}").append(translateText(content)).append("{{/").append(helper).append("}}");
                return close;
            }
        }

        out.append("{{").append(helper).append(' ').append(nextSite());
        for (String arg : normalizeArgs(def, args)) {
            out.append(' ').append(argExpr(arg));
        }
        out.append("}}");
        return idx;
    }

    /**
     * Extra arguments are merged into the last one (so {{setvar::x::a::b}} sets "a::b").
     */
    private static List<String> normalizeArgs(MacroDefinition def, List<String> args) {
        if (def.maxArgs() < 1 || args.size() <= def.maxArgs()) {
            return def.maxArgs() == 0 ? List.of() : args;
        }
        List<String> result = new ArrayList<>(args.subList(0, def.maxArgs() - 1));
        result.add(String.join("::", args.subList(def.maxArgs() - 1, args.size())));
        return result;
    }

    private void emitIf(String condition, String content, boolean preserve, StringBuilder out) {
        String[] branches = splitElse(content);
        String whenTrue = preserve ? branches[0] : dedentTrim(branches[0]);
        String whenFalse = branches[1] == null ? null : (preserve ? branches[1] : dedentTrim(branches[1]));

        out.append("{{#").append(IF).append(' ').append(nextSite()).append(' ').append(conditionExpr(condition)).append("}}");
        out.append(translateText(whenTrue));
        if (whenFalse != null) {
            out.append("{{else}}").append(translateText(whenFalse));
        }
        out.append("{{/").append(IF).append("}}");
    }

    /**
     * Splits if content on top level {{else}}.
     */
    private static String[] splitElse(String content) {
        List<Segment> segments = scan(content);
        int depth = 0;
        for (Segment seg : segments) {
            if (seg.kind() != SegmentKind.TAG) {
                continue;
            }
            TagInfo info = parseTag(seg.body());
            if (info.kind() == TagKind.ELSE && depth == 0) {
                return new String[]{content.substring(0, seg.start()), content.substring(seg.end())};
            } else if (info.handlebarsOpen() || info.opensScope()) {
                depth++;
            } else if (info.kind() == TagKind.CLOSE || (info.kind() == TagKind.PASS && info.closing())) {
                depth = Math.max(0, depth - 1);
            }
        }
        return new String[]{content, null};
    }

    private String conditionExpr(String condition) {
        String c = condition.strip();
        boolean negate = false;
        while (c.startsWith("!")) {
            negate = !negate;
            c = c.substring(1).stripLeading();
        }
        String expr;
        Shorthand shorthand = c.startsWith(".") || c.startsWith("$") ? parseShorthand(c) : null;
        if (shorthand != null) {
            expr = shorthandExpr(shorthand);
        } else if (PLAIN_NAME.matcher(c).matches()) {
            expr = "(" + COND + " " + nextSite() + " " + lit(c) + ")";
        } else {
            expr = argExpr(c);
        }
        return expr + " " + negate;
    }

    private String shorthandStatement(Shorthand shorthand) {
        String head = VAR + " " + nextSite() + " \"" + shorthand.scope() + "\" \"" + shorthand.name() + "\" \"" + shorthand.operator() + "\"";
        if (!shorthand.needsValue()) {
            return "{{" + head + "}}";
        }
        return "{{#" + head + "}}" + translateText(shorthand.value()) + "{{/" + VAR + "}}";
    }

    private String shorthandExpr(Shorthand shorthand) {
        String head = "(" + VAR + " " + nextSite() + " \"" + shorthand.scope() + "\" \"" + shorthand.name() + "\" \"" + shorthand.operator() + "\"";
        if (!shorthand.needsValue()) {
            return head + ")";
        }
        return head + " " + argExpr(shorthand.value()) + ")";
    }

    /**
     * Translates macro argument (text possibly containing nested macros) into a handlebars parameter expression.
     */
    private String argExpr(String raw) {
        List<Segment> segments = scan(raw);
        List<String> parts = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        for (Segment seg : segments) {
            switch (seg.kind()) {
                case TEXT, VERBATIM -> text.append(raw, seg.start(), seg.end());
                case ESCAPED -> text.append(seg.body());
                case LEGACY -> {
                    flush(parts, text);
                    parts.add("(" + Macros.helperName(Macros.find(seg.body()).name()) + " " + nextSite() + ")");
                }
                case TAG -> {
                    TagInfo info = parseTag(seg.body());
                    String expr = subExpr(info);
                    if (expr == null) {
                        if (info.kind() != TagKind.COMMENT) {
                            text.append(info.raw());
                        }
                    } else {
                        flush(parts, text);
                        parts.add(expr);
                    }
                }
            }
        }
        flush(parts, text);
        if (parts.isEmpty()) {
            return lit("");
        }
        if (parts.size() == 1) {
            return parts.getFirst();
        }
        return "(" + CONCAT + " " + nextSite() + " " + String.join(" ", parts) + ")";
    }

    private void flush(List<String> parts, StringBuilder text) {
        if (!text.isEmpty()) {
            parts.add(lit(text.toString()));
            text.setLength(0);
        }
    }

    /**
     * @return expression or null if the tag should be kept as literal text
     */
    private String subExpr(TagInfo info) {
        switch (info.kind()) {
            case SHORTHAND:
                return shorthandExpr(info.shorthand());
            case MACRO: {
                MacroDefinition def = info.definition();
                if (Macros.IF.equals(def.name())) {
                    if (info.args().size() < 2) {
                        return null;
                    }
                    String content = String.join("::", info.args().subList(1, info.args().size()));
                    return "(" + IF + " " + nextSite() + " " + conditionExpr(info.args().getFirst()) + " " + argExpr(content) + ")";
                }
                StringBuilder sb = new StringBuilder("(").append(Macros.helperName(def.name())).append(' ').append(nextSite());
                for (String arg : normalizeArgs(def, info.args())) {
                    sb.append(' ').append(argExpr(arg));
                }
                return sb.append(')').toString();
            }
            case PASS: {
                String body = info.raw().substring(2, info.raw().length() - 2).strip();
                if (PATH.matcher(body).matches()) {
                    return "(" + PROP + " " + nextSite() + " " + lit(body) + ")";
                }
                return null;
            }
            default:
                return null;
        }
    }
}
