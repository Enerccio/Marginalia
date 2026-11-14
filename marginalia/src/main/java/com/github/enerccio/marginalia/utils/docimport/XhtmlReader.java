package com.github.enerccio.marginalia.utils.docimport;

import org.apache.commons.text.StringEscapeUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The text of a page of an EPUB (XHTML, or HTML as it is found), cut into blocks: {@code h1} to {@code h6} are headings, the
 * other block elements (paragraphs, quotes, list items, table cells...) are paragraphs, {@code em}/{@code i} and
 * {@code strong}/{@code b} (and a {@code span} styled that way) are emphasis, {@code hr} is a scene break. A scanner and not
 * an XML parser: HTML entities ({@code &amp;nbsp;}), unclosed tags and a missing DTD do not matter. Scripts, styles, the head,
 * pictures, notes ({@code epub:type} footnote, note references) and the ruby readings ({@code rt}) are left out.
 */
final class XhtmlReader {

    private static final Set<String> BLOCKS = Set.of("p", "div", "blockquote", "li", "ul", "ol", "dl", "dt", "dd", "table", "thead",
            "tbody", "tfoot", "tr", "td", "th", "section", "article", "header", "footer", "main", "figure", "figcaption", "pre",
            "address", "body", "caption", "center", "details", "summary", "hgroup", "fieldset", "form");
    private static final Set<String> VOID = Set.of("br", "hr", "img", "meta", "link", "input", "col", "wbr", "area", "base", "param", "source");
    private static final Set<String> SKIPPED = Set.of("script", "style", "head", "svg", "math", "rt", "rp", "nav", "title", "object", "audio", "video", "canvas");
    private static final Set<String> BOLD = Set.of("b", "strong");
    private static final Set<String> ITALIC = Set.of("i", "em", "cite", "var", "dfn");
    private static final Pattern ATTRIBUTE = Pattern.compile("([\\w:.-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'>]+))");
    private static final Pattern NOTE_TYPE = Pattern.compile("\\b(footnote|endnote|rearnote|noteref|footnotes|endnotes)\\b");
    private static final Pattern ITALIC_STYLE = Pattern.compile("font-style\\s*:\\s*(italic|oblique)");
    private static final Pattern BOLD_STYLE = Pattern.compile("font-weight\\s*:\\s*(bold|[6-9]00)");

    private record Frame(String name, boolean bold, boolean italic) {
    }

    private final String html;
    private final List<TextBlock> blocks = new ArrayList<>();
    private final Deque<Frame> open = new ArrayDeque<>();
    private RichText current = new RichText();
    private int heading;
    private int bold;
    private int italic;

    private XhtmlReader(String html) {
        this.html = html;
    }

    /**
     * @return the blocks of the page
     */
    static List<TextBlock> read(String html) {
        XhtmlReader reader = new XhtmlReader(html);
        reader.scan();
        return reader.blocks;
    }

    private void scan() {
        int length = html.length();
        int position = 0;
        // a page with a body has its text there, a fragment is all text
        boolean inBody = !Pattern.compile("<body[\\s>/]", Pattern.CASE_INSENSITIVE).matcher(html).find();
        while (position < length) {
            int lt = html.indexOf('<', position);
            if (lt < 0) {
                text(html.substring(position), inBody);
                break;
            }
            text(html.substring(position, lt), inBody);
            if (html.startsWith("<!--", lt)) {
                int end = html.indexOf("-->", lt + 4);
                position = end < 0 ? length : end + 3;
                continue;
            }
            if (html.startsWith("<![CDATA[", lt)) {
                int end = html.indexOf("]]>", lt);
                text(html.substring(lt + 9, end < 0 ? length : end), inBody);
                position = end < 0 ? length : end + 3;
                continue;
            }
            int gt = tagEnd(lt);
            if (gt < 0) {
                text(html.substring(lt), inBody);
                break;
            }
            String tag = html.substring(lt + 1, gt);
            position = gt + 1;
            if (tag.startsWith("!") || tag.startsWith("?")) {
                continue;
            }
            boolean closing = tag.startsWith("/");
            String name = name(closing ? tag.substring(1) : tag);
            if (name.isEmpty()) {
                continue;
            }
            if (!closing && name.equals("body")) {
                inBody = true;
            }
            if (closing) {
                close(name);
                if (name.equals("body")) {
                    inBody = false;
                }
                continue;
            }
            if (SKIPPED.contains(name) && (!name.equals("head") || !tag.endsWith("/"))) {
                position = skipTo(name, position);
                continue;
            }
            String attributes = tag.substring(Math.min(tag.length(), name.length()));
            if (name.equals("a") && NOTE_TYPE.matcher(attribute(attributes, "epub:type")).find()
                    || name.equals("aside") && NOTE_TYPE.matcher(attribute(attributes, "epub:type")).find()) {
                position = skipTo(name, position);
                continue;
            }
            if (!inBody) {
                continue;
            }
            open(name, attributes, tag.endsWith("/"));
        }
        flush();
    }

    // the end of the tag at the position, not counting a > in a quoted attribute
    private int tagEnd(int start) {
        char quote = 0;
        for (int i = start + 1; i < html.length(); i++) {
            char c = html.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                }
            } else if (c == '"' || c == '\'') {
                quote = c;
            } else if (c == '>') {
                return i;
            }
        }
        return -1;
    }

    private static String name(String tag) {
        int end = 0;
        while (end < tag.length() && !Character.isWhitespace(tag.charAt(end)) && tag.charAt(end) != '/') {
            end++;
        }
        String name = tag.substring(0, end).toLowerCase(Locale.ROOT);
        int colon = name.indexOf(':');
        return colon >= 0 ? name.substring(colon + 1) : name;
    }

    private static String attribute(String attributes, String name) {
        Matcher matcher = ATTRIBUTE.matcher(attributes);
        while (matcher.find()) {
            if (matcher.group(1).equalsIgnoreCase(name)) {
                for (int group = 2; group <= 4; group++) {
                    if (matcher.group(group) != null) {
                        return matcher.group(group);
                    }
                }
            }
        }
        return "";
    }

    // behind the closing tag of the element that starts here (the end of the page when there is none)
    private int skipTo(String name, int from) {
        Matcher matcher = Pattern.compile("</(?:[\\w-]+:)?" + Pattern.quote(name) + "\\s*>", Pattern.CASE_INSENSITIVE).matcher(html);
        return matcher.find(from) ? matcher.end() : html.length();
    }

    private void text(String raw, boolean inBody) {
        if (!inBody || raw.isEmpty()) {
            return;
        }
        String text = StringEscapeUtils.unescapeHtml4(raw).replace((char) 160, ' ').replaceAll("\\s+", " ");
        current.add(text, bold > 0, italic > 0);
    }

    private void open(String name, String attributes, boolean selfClosing) {
        if (name.length() == 2 && name.charAt(0) == 'h' && name.charAt(1) >= '1' && name.charAt(1) <= '6') {
            flush();
            heading = name.charAt(1) - '0';
            return;
        }
        if (name.equals("br")) {
            current.add("\n", false, false);
            return;
        }
        if (name.equals("hr")) {
            flush();
            blocks.add(new TextBlock(0, "\\* \\* \\*"));
            return;
        }
        if (BLOCKS.contains(name)) {
            flush();
            return;
        }
        if (VOID.contains(name) || selfClosing) {
            return;
        }
        boolean makesBold = BOLD.contains(name);
        boolean makesItalic = ITALIC.contains(name);
        if (name.equals("span") || name.equals("font")) {
            String style = attribute(attributes, "style").toLowerCase(Locale.ROOT);
            makesBold = BOLD_STYLE.matcher(style).find();
            makesItalic = ITALIC_STYLE.matcher(style).find();
        }
        open.push(new Frame(name, makesBold, makesItalic));
        bold += makesBold ? 1 : 0;
        italic += makesItalic ? 1 : 0;
    }

    private void close(String name) {
        if (name.length() == 2 && name.charAt(0) == 'h' && name.charAt(1) >= '1' && name.charAt(1) <= '6') {
            flush();
            heading = 0;
            return;
        }
        if (BLOCKS.contains(name)) {
            flush();
            return;
        }
        if (open.stream().noneMatch(frame -> frame.name.equals(name))) {
            return;
        }
        // an element left open inside (an unclosed tag) is closed with it
        while (!open.isEmpty()) {
            Frame frame = open.pop();
            bold -= frame.bold ? 1 : 0;
            italic -= frame.italic ? 1 : 0;
            if (frame.name.equals(name)) {
                break;
            }
        }
    }

    private void flush() {
        if (!current.isBlank()) {
            blocks.add(heading > 0 ? new TextBlock(heading, current.plain()) : new TextBlock(0, current.markdown()));
        }
        current = new RichText();
    }
}
