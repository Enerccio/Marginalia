package com.github.enerccio.marginalia.domain.service.manual;

import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a page of the manual (Markdown with Retype front matter) into a {@link ManualPage}: removes the front matter,
 * the pictures and the Retype admonition markers, rewrites relative links between pages to manual paths and splits the
 * page by its headings.
 */
public final class ManualParser {

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*?)\\s*#*\\s*$");
    private static final Pattern IMAGE = Pattern.compile("!\\[[^\\]]*]\\([^)]*\\)");
    private static final Pattern PAGE_LINK = Pattern.compile("\\[([^\\]]*)]\\(([^)#:\\s]+\\.md)(#[^)\\s]*)?\\)");
    private static final Pattern ADMONITION = Pattern.compile("^!!!\\s*(\\S*)\\s*(.*)$");
    private static final int DESCRIPTION_LENGTH = 240;

    private ManualParser() {
    }

    public static ManualPage parse(String path, String source) {
        String text = source.replace("\r\n", "\n").replace('\r', '\n');
        String label = null;
        if (text.startsWith("---\n")) {
            int end = text.indexOf("\n---", 3);
            if (end > 0) {
                label = readLabel(text.substring(4, end));
                text = text.substring(Math.min(text.length(), end + 4)).stripLeading();
            }
        }
        List<String> lines = clean(path, text);

        String title = null;
        List<ManualSection> sections = new ArrayList<>();
        List<String> current = new ArrayList<>();
        String heading = null;
        String anchor = "";
        String parent = null;
        boolean fenced = false;
        String firstH1 = null;
        for (String line : lines) {
            if (isFence(line)) {
                fenced = !fenced;
            }
            Matcher m = fenced ? null : HEADING.matcher(line);
            if (m != null && m.matches()) {
                int level = m.group(1).length();
                String headingText = stripMarkup(m.group(2));
                if (level == 1 && firstH1 == null) {
                    firstH1 = headingText;
                    continue;
                }
                if (level == 2 || level == 3) {
                    flush(sections, path, title(firstH1, label, path), heading, anchor, current);
                    current = new ArrayList<>();
                    parent = level == 2 ? headingText : parent;
                    heading = level == 3 && parent != null ? parent + " > " + headingText : headingText;
                    anchor = slug(headingText);
                    continue;
                }
            }
            current.add(line);
        }
        title = title(firstH1, label, path);
        flush(sections, path, title, heading, anchor, current);

        String whole = String.join("\n", lines).strip();
        return new ManualPage(path, title, describe(sections), whole, List.copyOf(sections));
    }

    /** Heading anchor as Retype writes it: lower case, punctuation dropped, spaces turned to hyphens. */
    public static String slug(String heading) {
        StringBuilder sb = new StringBuilder();
        for (char c : heading.toLowerCase(Locale.ROOT).toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                sb.append(c);
            } else if (c == ' ') {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    private static String title(String h1, String label, String path) {
        if (h1 != null && !h1.isBlank()) {
            return h1;
        }
        return label != null && !label.isBlank() ? label : path;
    }

    private static String readLabel(String frontMatter) {
        try {
            Object data = new Yaml().load(frontMatter);
            if (data instanceof Map<?, ?> map && map.get("label") != null) {
                return String.valueOf(map.get("label"));
            }
        } catch (RuntimeException ignored) {
            // a broken front matter only costs the label
        }
        return null;
    }

    private static boolean isFence(String line) {
        String t = line.stripLeading();
        return t.startsWith("```") || t.startsWith("~~~");
    }

    /** Removes pictures and admonition markers outside code blocks and rewrites the links to other pages. */
    private static List<String> clean(String path, String text) {
        List<String> out = new ArrayList<>();
        boolean fenced = false;
        boolean admonition = false;
        boolean blank = true;
        for (String line : text.split("\n", -1)) {
            if (isFence(line)) {
                fenced = !fenced;
                out.add(line);
                blank = false;
                continue;
            }
            if (!fenced) {
                Matcher a = ADMONITION.matcher(line.stripTrailing());
                if (a.matches()) {
                    String type = a.group(1);
                    String title = a.group(2);
                    if (admonition && type.isEmpty()) {
                        admonition = false;
                        continue;
                    }
                    admonition = true;
                    String label = type.isEmpty() ? "Note" : Character.toUpperCase(type.charAt(0)) + type.substring(1);
                    line = "**" + label + (title.isEmpty() ? "" : ": " + title) + "**";
                } else {
                    line = IMAGE.matcher(line).replaceAll("").stripTrailing();
                    line = rewriteLinks(path, line);
                }
            }
            if (line.isBlank() && blank) {
                continue;
            }
            blank = line.isBlank();
            out.add(line);
        }
        return out;
    }

    /** Pages of the developer manual are not packed, a link to one stays as its plain text. */
    private static String rewriteLinks(String path, String line) {
        Matcher m = PAGE_LINK.matcher(line);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String target = resolve(path, m.group(2));
            String anchor = m.group(3) == null ? "" : m.group(3);
            String replacement = target.startsWith("developer/")
                    ? m.group(1)
                    : "[" + m.group(1) + "](" + target + anchor + ")";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** Resolves a link written in the page {@code from} to a path in the manual. */
    static String resolve(String from, String href) {
        List<String> parts = new ArrayList<>();
        int slash = from.lastIndexOf('/');
        if (slash >= 0) {
            parts.addAll(List.of(from.substring(0, slash).split("/")));
        }
        for (String part : href.split("/")) {
            if (part.equals("..")) {
                if (!parts.isEmpty()) {
                    parts.remove(parts.size() - 1);
                }
            } else if (!part.equals(".") && !part.isEmpty()) {
                parts.add(part);
            }
        }
        return String.join("/", parts);
    }

    private static void flush(List<ManualSection> sections, String path, String pageTitle, String heading,
                              String anchor, List<String> lines) {
        String text = String.join("\n", lines).strip();
        if (text.isEmpty()) {
            return;
        }
        sections.add(new ManualSection(path, pageTitle, heading == null ? pageTitle : heading, anchor, text));
    }

    /** First paragraph of the introduction as plain text, shortened. */
    private static String describe(List<ManualSection> sections) {
        if (sections.isEmpty()) {
            return "";
        }
        String first = sections.get(0).text().split("\n\n", 2)[0];
        String plain = stripMarkup(first.replace('\n', ' ')).replaceAll("\\s+", " ").strip();
        return plain.length() <= DESCRIPTION_LENGTH ? plain : plain.substring(0, DESCRIPTION_LENGTH - 1).stripTrailing() + "…";
    }

    /** Drops emphasis, code ticks and link syntax, keeping the visible words. */
    private static String stripMarkup(String s) {
        return s.replaceAll("\\[([^\\]]*)]\\([^)]*\\)", "$1").replaceAll("[*_`]", "").strip();
    }
}
