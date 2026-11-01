package com.github.enerccio.marginalia.domain.styles;

import com.github.enerccio.marginalia.loc.L;

import java.util.List;

/**
 * One adjustable aspect of a {@link PartStyle}: its font, its colors, the way headings look... The editor of the styles builds its
 * form from these, {@link PartStyle#toCss()} reads them. A style stores only the options it sets (text), an option it does not
 * set leaves the look of the application.
 */
public enum PartStyleOption {

    FONT_FAMILY(L.LABEL_STYLE_FONT_FAMILY, Group.TEXT, Kind.FONT),
    FONT_SIZE(L.LABEL_STYLE_FONT_SIZE, Group.TEXT, Kind.TEXT),
    LINE_HEIGHT(L.LABEL_STYLE_LINE_HEIGHT, Group.TEXT, Kind.TEXT),
    LETTER_SPACING(L.LABEL_STYLE_LETTER_SPACING, Group.TEXT, Kind.TEXT),
    WORD_SPACING(L.LABEL_STYLE_WORD_SPACING, Group.TEXT, Kind.TEXT),
    TEXT_COLOR(L.LABEL_STYLE_TEXT_COLOR, Group.TEXT, Kind.COLOR),
    TEXT_ALIGN(L.LABEL_STYLE_TEXT_ALIGN, Group.TEXT, Kind.CHOICE, "left", "right", "center", "justify"),
    HYPHENATION(L.LABEL_STYLE_HYPHENATION, Group.TEXT, Kind.BOOLEAN),
    PARAGRAPH_SPACING(L.LABEL_STYLE_PARAGRAPH_SPACING, Group.TEXT, Kind.TEXT),
    FIRST_LINE_INDENT(L.LABEL_STYLE_FIRST_LINE_INDENT, Group.TEXT, Kind.TEXT),
    DROP_CAP(L.LABEL_STYLE_DROP_CAP, Group.TEXT, Kind.BOOLEAN),
    TEXT_WIDTH(L.LABEL_STYLE_TEXT_WIDTH, Group.PAGE, Kind.TEXT),
    BACKGROUND_COLOR(L.LABEL_STYLE_BACKGROUND_COLOR, Group.PAGE, Kind.COLOR),
    EMPHASIS_COLOR(L.LABEL_STYLE_EMPHASIS_COLOR, Group.INLINE, Kind.COLOR),
    STRONG_COLOR(L.LABEL_STYLE_STRONG_COLOR, Group.INLINE, Kind.COLOR),
    LINK_COLOR(L.LABEL_STYLE_LINK_COLOR, Group.INLINE, Kind.COLOR),
    CODE_FONT_FAMILY(L.LABEL_STYLE_CODE_FONT_FAMILY, Group.INLINE, Kind.FONT),
    QUOTE_COLOR(L.LABEL_STYLE_QUOTE_COLOR, Group.QUOTE, Kind.COLOR),
    QUOTE_BORDER_COLOR(L.LABEL_STYLE_QUOTE_BORDER_COLOR, Group.QUOTE, Kind.COLOR),
    HEADING_FONT_FAMILY(L.LABEL_STYLE_HEADING_FONT_FAMILY, Group.HEADINGS, Kind.FONT),
    HEADING_SIZE(L.LABEL_STYLE_HEADING_SIZE, Group.HEADINGS, Kind.TEXT),
    HEADING_COLOR(L.LABEL_STYLE_HEADING_COLOR, Group.HEADINGS, Kind.COLOR),
    HEADING_ALIGN(L.LABEL_STYLE_HEADING_ALIGN, Group.HEADINGS, Kind.CHOICE, "left", "center", "right"),
    HEADING_SMALL_CAPS(L.LABEL_STYLE_HEADING_SMALL_CAPS, Group.HEADINGS, Kind.BOOLEAN),
    SCENE_BREAK_TEXT(L.LABEL_STYLE_SCENE_BREAK_TEXT, Group.SCENE_BREAK, Kind.TEXT),
    SCENE_BREAK_COLOR(L.LABEL_STYLE_SCENE_BREAK_COLOR, Group.SCENE_BREAK, Kind.COLOR);

    /**
     * The section of the editor an option is in.
     */
    public enum Group {
        TEXT(L.LABEL_TEXT),
        PAGE(L.LABEL_STYLE_GROUP_PAGE),
        INLINE(L.LABEL_STYLE_GROUP_INLINE),
        QUOTE(L.LABEL_STYLE_GROUP_QUOTE),
        HEADINGS(L.LABEL_STYLE_GROUP_HEADINGS),
        SCENE_BREAK(L.LABEL_STYLE_GROUP_SCENE_BREAK);

        private final L label;

        Group(L label) {
            this.label = label;
        }

        public L getLabel() {
            return label;
        }
    }

    /**
     * What the value of an option is, which decides the field the editor shows.
     */
    public enum Kind {
        /** Any CSS value. */
        TEXT,
        /** A font stack. */
        FONT,
        /** A CSS color (a name, {@code #rrggbb}, {@code var(...)}, {@code light-dark(...)}). */
        COLOR,
        /** One of {@link PartStyleOption#getChoices()}. */
        CHOICE,
        /** {@code "true"} or {@code "false"}. */
        BOOLEAN
    }

    /**
     * Font stacks the editor offers (any other text may be typed).
     */
    public static final List<String> FONT_SUGGESTIONS = List.of(
            "\"Garamond\", \"Georgia\", \"Times New Roman\", serif",
            "\"Georgia\", \"Times New Roman\", serif",
            "\"Palatino Linotype\", \"Book Antiqua\", Palatino, serif",
            "\"Times New Roman\", Times, serif",
            "system-ui, -apple-system, \"Segoe UI\", Roboto, sans-serif",
            "\"Helvetica Neue\", Helvetica, Arial, sans-serif",
            "\"Courier New\", Courier, monospace");

    private final L label;
    private final Group group;
    private final Kind kind;
    private final List<String> choices;

    PartStyleOption(L label, Group group, Kind kind, String... choices) {
        this.label = label;
        this.group = group;
        this.kind = kind;
        this.choices = List.of(choices);
    }

    public L getLabel() {
        return label;
    }

    public Group getGroup() {
        return group;
    }

    public Kind getKind() {
        return kind;
    }

    public List<String> getChoices() {
        return choices;
    }

    /**
     * The key the option has in the stored style.
     */
    public String getKey() {
        return name();
    }
}
