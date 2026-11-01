package com.github.enerccio.marginalia.domain.styles;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * How the text of the parts of a book looks: the options of {@link PartStyleOption} (font, colors, headings, ...) and a block of CSS
 * of the author's own. The style is turned into a stylesheet by {@link #toCss()}, scoped to the class {@link #getCssClass()}, which
 * the story, the reader and the preview of the editor put on the element that holds the parts.
 * <p>
 * The styles of the application ({@link PartStyleRegistry}) are {@link #isBuiltin() built in}: they are shared, never stored and
 * cannot be changed or deleted, only duplicated. The styles of a user are stored in their {@code StylesSetting} as JSON, so this
 * class is plain data (Gson): the fields below are the stored form, {@code builtin} and {@code nameKey} are not stored.
 */
public class PartStyle {

    /** Wraps the custom CSS: the author writes rules as if inside the scope of the style (native CSS nesting). */
    private static final String SCOPE_COMMENT = "/* custom CSS of the style */";

    private String id;
    private String name;
    private Map<String, String> options = new LinkedHashMap<>();
    private String customCss;

    private transient boolean builtin;
    private transient L nameKey;

    public PartStyle() {
    }

    public PartStyle(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * A built in style, called by the text of {@code nameKey} in the language of the user.
     */
    public static PartStyle builtin(String id, String name, L nameKey) {
        PartStyle style = new PartStyle(id, name);
        style.nameKey = nameKey;
        style.builtin = true;
        return style;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The stored name; use {@link #getDisplayName} to show it.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name to show: the text of the language of the user for a built in style, the name the author gave otherwise.
     */
    public String getDisplayName(Localization loc) {
        return nameKey != null ? loc.getValue(nameKey) : StringUtils.defaultString(name);
    }

    public boolean isBuiltin() {
        return builtin;
    }

    public String getCustomCss() {
        return customCss;
    }

    public void setCustomCss(String customCss) {
        this.customCss = StringUtils.isBlank(customCss) ? null : customCss;
    }

    /**
     * The value of the option, null when the style does not set it.
     */
    public String get(PartStyleOption option) {
        String value = options.get(option.getKey());
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    /**
     * Sets the option, a blank value removes it.
     */
    public PartStyle set(PartStyleOption option, String value) {
        if (StringUtils.isBlank(value)) {
            options.remove(option.getKey());
        } else {
            options.put(option.getKey(), value.trim());
        }
        return this;
    }

    public boolean getBoolean(PartStyleOption option) {
        return "true".equalsIgnoreCase(get(option));
    }

    /**
     * The options the style sets.
     */
    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * Whether the style changes anything: a style without options and custom CSS is the look of the application, and adds no class.
     */
    public boolean hasContent() {
        return !options.values().stream().allMatch(StringUtils::isBlank) || StringUtils.isNotBlank(customCss);
    }

    /**
     * A style of the same options and CSS under another id and name, never built in.
     */
    public PartStyle copy(String newId, String newName) {
        PartStyle copy = new PartStyle(newId, newName);
        copy.options = new LinkedHashMap<>(options);
        copy.customCss = customCss;
        return copy;
    }

    /**
     * The class the element that holds the text carries to get the style.
     */
    public String getCssClass() {
        return "part-style-" + StringUtils.defaultString(id).replaceAll("[^A-Za-z0-9_-]", "-");
    }

    /**
     * The id of the {@code <style>} element of the page that holds the stylesheet of the style.
     */
    public String getStyleElementId() {
        return "marginalia-" + getCssClass();
    }

    /**
     * Whether the braces of the custom CSS are balanced; the block is put inside a rule, so a stray closing brace would reach outside it.
     */
    public static boolean isBalanced(String css) {
        int depth = 0;
        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}' && --depth < 0) {
                return false;
            }
        }
        return depth == 0;
    }

    /**
     * The stylesheet of the style, empty for a style without {@link #hasContent content}. The rules reach the markdown of the parts
     * ({@code .chat-message-markdown}) below the element with {@link #getCssClass()}; the custom CSS comes last, nested in the scope of
     * the style, so its rules and declarations are relative to the element: {@code .chat-message-markdown p { color: red }}, or
     * {@code & p { ... }}.
     */
    public String toCss() {
        if (!hasContent()) {
            return "";
        }
        String root = "." + getCssClass();
        String md = root + " ." + CssStyles.CHAT_MESSAGE_MARKDOWN;
        StringBuilder css = new StringBuilder();

        Rule base = new Rule(md)
                .option("font-family", PartStyleOption.FONT_FAMILY)
                .option("font-size", PartStyleOption.FONT_SIZE)
                .option("line-height", PartStyleOption.LINE_HEIGHT)
                .option("letter-spacing", PartStyleOption.LETTER_SPACING)
                .option("color", PartStyleOption.TEXT_COLOR)
                .fixed("width", "100% !important")
                .fixed("max-width", "100% !important")
                .fixed("box-sizing", "border-box !important")
                .fixed("overflow-wrap", "anywhere !important")
                .fixed("word-break", "break-word !important");
        base.appendTo(css);

        // children stay flush-left and inside the boundaries (no stair-casing of dialogue, no horizontal scrolling)
        new Rule(selectors(md, " *", " p", " div", " pre", " code", " blockquote", " ul", " ol", " li"))
                .fixed("text-indent", "0 !important")
                .fixed("max-width", "100% !important")
                .fixed("box-sizing", "border-box !important")
                .fixed("overflow-wrap", "anywhere !important")
                .fixed("word-break", "break-word !important")
                .appendTo(css);

        Rule paragraph = new Rule(md + " p")
                .fixed("margin-top", "0")
                .option("margin-bottom", PartStyleOption.PARAGRAPH_SPACING)
                .option("text-align", PartStyleOption.TEXT_ALIGN)
                .fixed("white-space", "pre-wrap !important");
        if ("justify".equals(get(PartStyleOption.TEXT_ALIGN))) {
            paragraph.fixed("text-justify", "inter-word");
        }
        if (getBoolean(PartStyleOption.HYPHENATION)) {
            // TeX-like hyphenation; the dictionary comes from the lang attribute set from the book language
            paragraph.fixed("hyphens", "auto").fixed("-webkit-hyphens", "auto")
                    .fixed("hyphenate-limit-chars", "6 3 3")
                    .fixed("hyphenate-limit-lines", "2")
                    .fixed("hyphenate-limit-last", "always");
        }
        paragraph.fixed("orphans", "2").fixed("widows", "2");
        paragraph.appendTo(css);

        new Rule(selectors(md, " em", " i"))
                .fixed("font-style", "italic")
                .option("color", PartStyleOption.EMPHASIS_COLOR)
                .appendTo(css);
        new Rule(selectors(md, " strong", " b"))
                .fixed("font-weight", "700")
                .option("color", PartStyleOption.STRONG_COLOR)
                .appendTo(css);

        new Rule(md + " blockquote")
                .fixed("margin", "1em 0")
                .fixed("padding", "0.5em 1em")
                .option("border-left", PartStyleOption.QUOTE_BORDER_COLOR, "3px solid ")
                .fixed("background-color", "var(--lumo-contrast-5pct, rgba(255, 255, 255, 0.03))")
                .fixed("border-radius", "0 4px 4px 0")
                .fixed("font-style", "italic")
                .option("color", PartStyleOption.QUOTE_COLOR)
                .appendTo(css);
        new Rule(md + " blockquote p").fixed("margin-bottom", "0.4em").appendTo(css);
        new Rule(md + " blockquote p:last-child").fixed("margin-bottom", "0").appendTo(css);

        new Rule(md + " pre")
                .fixed("background-color", "var(--lumo-contrast-10pct, rgba(255, 255, 255, 0.05))")
                .fixed("border", "1px solid var(--lumo-contrast-10pct, rgba(255, 255, 255, 0.1))")
                .fixed("border-radius", "4px")
                .fixed("padding", "0.6em 0.8em")
                .fixed("margin", "0.8em 0")
                .fixed("font-family", "var(--lumo-font-family-monospace, monospace)")
                .fixed("font-size", "0.88em")
                .fixed("line-height", "1.5")
                .fixed("white-space", "pre-wrap !important")
                .fixed("overflow-x", "hidden !important")
                .appendTo(css);
        new Rule(md + " code")
                .fixed("font-family", "var(--lumo-font-family-monospace, monospace)")
                .fixed("font-size", "0.88em")
                .fixed("background-color", "var(--lumo-contrast-10pct, rgba(255, 255, 255, 0.08))")
                .fixed("padding", "0.1em 0.3em")
                .fixed("border-radius", "3px")
                .fixed("white-space", "pre-wrap !important")
                .appendTo(css);

        new Rule(selectors(md, " h1", " h2", " h3"))
                .option("font-family", PartStyleOption.HEADING_FONT_FAMILY)
                .fixed("font-weight", "600")
                .option("text-align", PartStyleOption.HEADING_ALIGN)
                .fixed("letter-spacing", "0.05em")
                .fixed("margin-top", "1.5em")
                .fixed("margin-bottom", "0.6em")
                .option("color", PartStyleOption.HEADING_COLOR)
                .appendTo(css);
        Rule h1 = new Rule(md + " h1").fixed("font-size", "1.35rem")
                .fixed("border-bottom", "1px solid var(--lumo-contrast-10pct, rgba(255, 255, 255, 0.1))")
                .fixed("padding-bottom", "0.3em");
        Rule h2 = new Rule(md + " h2").fixed("font-size", "1.2rem");
        if (getBoolean(PartStyleOption.HEADING_SMALL_CAPS)) {
            h1.fixed("font-variant", "small-caps");
            h2.fixed("font-variant", "small-caps");
        }
        h1.appendTo(css);
        h2.appendTo(css);
        new Rule(md + " h3").fixed("font-size", "1.08rem").appendTo(css);

        new Rule(md + " hr").fixed("border", "none").fixed("text-align", "center").fixed("margin", "1.8em 0").appendTo(css);
        new Rule(md + " hr::after")
                .fixed("content", "\"" + cssString(StringUtils.defaultIfBlank(get(PartStyleOption.SCENE_BREAK_TEXT), "•  •  •")) + "\"")
                .fixed("display", "inline-block")
                .fixed("font-size", "1rem")
                .fixed("letter-spacing", "0.5em")
                .option("color", PartStyleOption.SCENE_BREAK_COLOR)
                .appendTo(css);

        if (StringUtils.isNotBlank(customCss)) {
            css.append(SCOPE_COMMENT).append('\n').append(root).append(" {\n").append(customCss.strip()).append("\n}\n");
        }
        return css.toString();
    }

    private static String selectors(String base, String... suffixes) {
        List<String> all = new ArrayList<>();
        for (String suffix : suffixes) {
            all.add(base + suffix);
        }
        return String.join(",\n", all);
    }

    /**
     * Makes text safe to be in a CSS string (the scene break), or a value (anything that could end the declaration or the rule goes).
     */
    private static String cssString(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("[\\r\\n]+", " ");
    }

    static String cssValue(String value) {
        return value.replaceAll("[{};<>\\r\\n]", " ").trim();
    }

    private final class Rule {
        private final String selector;
        private final StringBuilder declarations = new StringBuilder();

        Rule(String selector) {
            this.selector = selector;
        }

        Rule fixed(String property, String value) {
            declarations.append("    ").append(property).append(": ").append(value).append(";\n");
            return this;
        }

        Rule option(String property, PartStyleOption option) {
            return option(property, option, "");
        }

        Rule option(String property, PartStyleOption option, String prefix) {
            String value = option == null ? null : get(option);
            if (value != null) {
                declarations.append("    ").append(property).append(": ").append(prefix).append(cssValue(value)).append(";\n");
            }
            return this;
        }

        void appendTo(StringBuilder css) {
            if (!declarations.isEmpty()) {
                css.append(selector).append(" {\n").append(declarations).append("}\n");
            }
        }
    }
}
