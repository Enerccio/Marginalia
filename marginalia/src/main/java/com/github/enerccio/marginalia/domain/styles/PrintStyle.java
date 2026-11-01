package com.github.enerccio.marginalia.domain.styles;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What a {@link PartStyle} says, worked out for the exporters that do not understand CSS (PDF, Word): sizes in points, colors as RGB
 * numbers, and fonts as a family the format has. A value the style does not set (or sets to something that can't be turned into
 * paper, as a color name) is the default of the exporter, which is the look the exports had before styles; {@link #of} of {@code null}
 * is that look.
 * <p>
 * CSS units: {@code rem}, {@code em} and {@code %} count from the body size of a page, 12 pt ({@code 1rem} is 16 px = 12 pt).
 */
public final class PrintStyle {

    /** The kinds of fonts a PDF has built in (Liberation) and Word knows by name. */
    public enum Family {SERIF, SANS, MONO}

    public enum Align {LEFT, CENTER, RIGHT, JUSTIFY}

    /** The body text size of an unstyled export, and what a relative size is relative to. */
    public static final float BASE_SIZE = 12f;

    private static final Pattern LENGTH = Pattern.compile("^(-?[0-9]*\\.?[0-9]+)\\s*(rem|em|px|pt|%)?$");
    private static final Pattern HEX = Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");
    private static final Pattern RGB = Pattern.compile("^rgba?\\(\\s*(\\d{1,3})\\s*[ ,]\\s*(\\d{1,3})\\s*[ ,]\\s*(\\d{1,3})\\s*(?:[,/].*)?\\)$");

    private final boolean styled;
    private final Family family;
    private final String fontName;
    private final Family headingFamily;
    private final String headingFontName;
    private final Family codeFamily;
    private final float bodySize;
    private final float lineHeight;
    private final boolean lineHeightSet;
    private final Align align;
    private final float paragraphSpacing;
    private final float firstLineIndent;
    private final Integer textColor;
    private final Integer emphasisColor;
    private final Integer strongColor;
    private final Integer quoteColor;
    private final Integer headingColor;
    private final Align headingAlign;
    private final float headingScale;
    private final boolean headingSmallCaps;
    private final String sceneBreak;
    private final Integer sceneBreakColor;

    private PrintStyle(PartStyle style) {
        styled = style != null && style.hasContent();
        Family fontFamily = styled ? familyOf(option(style, PartStyleOption.FONT_FAMILY)) : null;
        family = fontFamily == null ? Family.SERIF : fontFamily;
        fontName = styled ? firstFont(option(style, PartStyleOption.FONT_FAMILY)) : null;
        Family heading = styled ? familyOf(option(style, PartStyleOption.HEADING_FONT_FAMILY)) : null;
        headingFamily = heading == null ? family : heading;
        String headingFont = styled ? firstFont(option(style, PartStyleOption.HEADING_FONT_FAMILY)) : null;
        headingFontName = headingFont == null && heading == null ? fontName : headingFont;
        codeFamily = Family.MONO;

        Float size = styled ? length(option(style, PartStyleOption.FONT_SIZE), BASE_SIZE, BASE_SIZE) : null;
        bodySize = size == null ? BASE_SIZE : Math.max(6f, Math.min(36f, size));
        Float lh = styled ? lineHeightOf(option(style, PartStyleOption.LINE_HEIGHT), bodySize) : null;
        lineHeightSet = lh != null;
        lineHeight = lh == null ? 1.4f : Math.max(1f, Math.min(3f, lh));
        align = styled ? alignOf(option(style, PartStyleOption.TEXT_ALIGN), Align.JUSTIFY) : Align.JUSTIFY;
        Float spacing = styled ? length(option(style, PartStyleOption.PARAGRAPH_SPACING), bodySize, 0f) : null;
        paragraphSpacing = spacing == null ? 8f : Math.max(0f, Math.min(48f, spacing));
        Float indent = styled ? length(option(style, PartStyleOption.FIRST_LINE_INDENT), bodySize, 0f) : null;
        firstLineIndent = indent == null ? 0f : Math.max(0f, Math.min(72f, indent));
        textColor = styled ? color(option(style, PartStyleOption.TEXT_COLOR)) : null;
        emphasisColor = styled ? color(option(style, PartStyleOption.EMPHASIS_COLOR)) : null;
        strongColor = styled ? color(option(style, PartStyleOption.STRONG_COLOR)) : null;
        quoteColor = styled ? color(option(style, PartStyleOption.QUOTE_COLOR)) : null;
        headingColor = styled ? color(option(style, PartStyleOption.HEADING_COLOR)) : null;
        headingAlign = styled ? alignOf(option(style, PartStyleOption.HEADING_ALIGN), Align.LEFT) : Align.LEFT;
        Float headingSize = styled ? length(option(style, PartStyleOption.HEADING_SIZE), BASE_SIZE, BASE_SIZE) : null;
        // the size of a top level heading relative to the body: 2 in the default look
        headingScale = headingSize == null ? 2f : Math.max(1f, Math.min(4f, headingSize / bodySize));
        headingSmallCaps = styled && style.getBoolean(PartStyleOption.HEADING_SMALL_CAPS);
        String breakText = styled ? style.get(PartStyleOption.SCENE_BREAK_TEXT) : null;
        sceneBreak = StringUtils.isBlank(breakText) ? "*   *   *" : breakText;
        sceneBreakColor = styled ? color(option(style, PartStyleOption.SCENE_BREAK_COLOR)) : null;
    }

    /**
     * The print look of the style; the default look of the exporters for {@code null} and for a style that sets nothing.
     */
    public static PrintStyle of(PartStyle style) {
        return new PrintStyle(style);
    }

    private static String option(PartStyle style, PartStyleOption option) {
        String value = style.get(option);
        return value == null ? null : StringUtils.trimToNull(PartStyle.resolveForPrint(value));
    }

    // --- parsing ---

    /**
     * The length in points; a number without a unit counts as {@code unitless} (the multiplier of the base).
     *
     * @param base what em, rem and % are relative to, in points
     * @return null when the text is not a length
     */
    static Float length(String text, float base, float unitless) {
        if (text == null) {
            return null;
        }
        Matcher matcher = LENGTH.matcher(text.trim());
        if (!matcher.matches()) {
            return null;
        }
        float number = Float.parseFloat(matcher.group(1));
        String unit = matcher.group(2);
        if (unit == null) {
            // "0" is a length, other numbers without a unit are not valid CSS lengths: taken as a multiple of the base (0 when none is given)
            return number == 0 ? 0f : number * unitless;
        }
        return switch (unit) {
            case "rem" -> number * BASE_SIZE;
            case "em" -> number * base;
            case "px" -> number * 0.75f;
            case "pt" -> number;
            case "%" -> number / 100f * base;
            default -> null;
        };
    }

    /**
     * The line height as a multiplier of the font size: a number is one, a length is divided by the body size.
     */
    static Float lineHeightOf(String text, float bodySize) {
        if (text == null) {
            return null;
        }
        Matcher matcher = LENGTH.matcher(text.trim());
        if (!matcher.matches()) {
            return null;
        }
        float number = Float.parseFloat(matcher.group(1));
        String unit = matcher.group(2);
        if (unit == null || "em".equals(unit) || "rem".equals(unit)) {
            return number;
        }
        Float points = length(text, bodySize, 0f);
        return points == null ? null : points / bodySize;
    }

    static Align alignOf(String text, Align fallback) {
        if (text == null) {
            return fallback;
        }
        return switch (text.trim().toLowerCase(Locale.ROOT)) {
            case "left", "start" -> Align.LEFT;
            case "right", "end" -> Align.RIGHT;
            case "center" -> Align.CENTER;
            case "justify" -> Align.JUSTIFY;
            default -> fallback;
        };
    }

    /**
     * The RGB of a color written as {@code #rgb}, {@code #rrggbb} or {@code rgb(r, g, b)}; null for anything else (a name, a variable).
     */
    static Integer color(String text) {
        if (text == null) {
            return null;
        }
        String value = text.trim();
        Matcher hex = HEX.matcher(value);
        if (hex.matches()) {
            String digits = hex.group(1);
            if (digits.length() == 3) {
                digits = "" + digits.charAt(0) + digits.charAt(0) + digits.charAt(1) + digits.charAt(1) + digits.charAt(2) + digits.charAt(2);
            }
            return Integer.parseInt(digits, 16);
        }
        Matcher rgb = RGB.matcher(value);
        if (rgb.matches()) {
            int r = Math.min(255, Integer.parseInt(rgb.group(1)));
            int g = Math.min(255, Integer.parseInt(rgb.group(2)));
            int b = Math.min(255, Integer.parseInt(rgb.group(3)));
            return (r << 16) | (g << 8) | b;
        }
        return null;
    }

    /**
     * The built in kind of the first family of the stack that says what it is (a generic family or a well known name), null for none.
     */
    static Family familyOf(String stack) {
        if (stack == null) {
            return null;
        }
        String lower = stack.toLowerCase(Locale.ROOT);
        for (String part : lower.split(",")) {
            String name = part.replace("\"", "").replace("'", "").trim();
            if (name.contains("mono") || name.contains("courier") || name.contains("consolas")) {
                return Family.MONO;
            }
            if (name.contains("sans") || name.equals("helvetica") || name.startsWith("helvetica ") || name.equals("arial")
                    || name.equals("verdana") || name.equals("system-ui") || name.equals("roboto") || name.startsWith("segoe")
                    || name.equals("-apple-system") || name.equals("tahoma") || name.equals("calibri")) {
                return Family.SANS;
            }
            if (name.contains("serif") || name.equals("georgia") || name.equals("garamond") || name.startsWith("times")
                    || name.startsWith("palatino") || name.equals("book antiqua") || name.equals("cambria")) {
                return Family.SERIF;
            }
        }
        return null;
    }

    /**
     * The first family of the stack that is a name of a font and not a generic family, without quotes; null when there is none.
     */
    static String firstFont(String stack) {
        if (stack == null) {
            return null;
        }
        for (String part : stack.split(",")) {
            String name = part.replace("\"", "").replace("'", "").trim();
            String lower = name.toLowerCase(Locale.ROOT);
            if (!name.isEmpty() && !lower.equals("serif") && !lower.equals("sans-serif") && !lower.equals("monospace")
                    && !lower.equals("system-ui") && !lower.equals("-apple-system") && !lower.startsWith("ui-")) {
                return name;
            }
        }
        return null;
    }

    // --- values ---

    /** Whether the style changes anything; an unstyled one is the look the exporters always had. */
    public boolean isStyled() {
        return styled;
    }

    public Family getFamily() {
        return family;
    }

    /** The name of the font of the text for a format that takes any name (Word), null for the font of the family. */
    public String getFontName() {
        return fontName;
    }

    public Family getHeadingFamily() {
        return headingFamily;
    }

    public String getHeadingFontName() {
        return headingFontName;
    }

    public Family getCodeFamily() {
        return codeFamily;
    }

    /** The size of the text in points. */
    public float getBodySize() {
        return bodySize;
    }

    /** The distance between the lines as a multiplier of the font size. */
    public float getLineHeight() {
        return lineHeight;
    }

    /** Whether the style sets the line height; Word keeps its own single spacing when it does not. */
    public boolean isLineHeightSet() {
        return lineHeightSet;
    }

    public Align getAlign() {
        return align;
    }

    /** The space after a paragraph, in points. */
    public float getParagraphSpacing() {
        return paragraphSpacing;
    }

    /** The indent of the first line of a paragraph (not after a heading or a scene break), in points. */
    public float getFirstLineIndent() {
        return firstLineIndent;
    }

    /** RGB of the text, null for the default (black). */
    public Integer getTextColor() {
        return textColor;
    }

    public Integer getEmphasisColor() {
        return emphasisColor;
    }

    public Integer getStrongColor() {
        return strongColor;
    }

    public Integer getQuoteColor() {
        return quoteColor;
    }

    public Integer getHeadingColor() {
        return headingColor;
    }

    public Align getHeadingAlign() {
        return headingAlign;
    }

    /** The size of a top level heading relative to the body text (2 by default); the lower levels keep their ratios. */
    public float getHeadingScale() {
        return headingScale;
    }

    public boolean isHeadingSmallCaps() {
        return headingSmallCaps;
    }

    /** The text of the break between scenes. */
    public String getSceneBreak() {
        return sceneBreak;
    }

    public Integer getSceneBreakColor() {
        return sceneBreakColor;
    }

    /**
     * An RGB as {@code RRGGBB}, for the formats that take hex colors.
     */
    public static String hex(int rgb) {
        return String.format("%06X", rgb & 0xFFFFFF);
    }
}
