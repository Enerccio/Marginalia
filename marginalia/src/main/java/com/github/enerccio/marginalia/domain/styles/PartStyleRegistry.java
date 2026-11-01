package com.github.enerccio.marginalia.domain.styles;

import com.github.enerccio.marginalia.loc.L;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * The built in {@link PartStyle}s: the looks the application brings, shared by all users, which cannot be changed or deleted (the
 * author duplicates one to get a style of their own, see {@code PartStyleService}). The application registers the plain look
 * ({@link #NORMAL}) and the {@link #BOOK book style} when it starts; an extension can register more when it is loaded and unregisters
 * them in {@code onExtensionUnload} ({@link #unregisterAll}, a bundle that is unloaded loses the rest by its name).
 * <p>
 * Ids of built in styles must not look like the uuids of the styles of the users: use a short readable one, prefixed with the name of the
 * extension.
 */
public class PartStyleRegistry implements InitializingBean {

    /** The look of the application: no stylesheet at all. */
    public static final String NORMAL = "normal";
    /** The style of a printed book: serif font, justified and hyphenated text, centered headings, a dotted scene break. */
    public static final String BOOK = "book";

    /** The look of the HTML export: a page of a web browser, Georgia, justified, a light sheet. */
    public static final String EXPORT_HTML = "export-html";
    /** The look of the EPUB export: the font of the reader, justified, spaced paragraphs. */
    public static final String EXPORT_EPUB = "export-epub";
    /** The look of the PDF export: Liberation Serif 12 pt, justified, headings to the left. */
    public static final String EXPORT_PDF = "export-pdf";
    /** The look of the Word export: Times New Roman 12 pt, justified, headings to the left. */
    public static final String EXPORT_DOCX = "export-docx";

    private static final String APPLICATION = "marginalia";

    private final Map<String, PartStyle> styles = new LinkedHashMap<>();
    private final Map<String, String> owners = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        register(APPLICATION, PartStyle.builtin(NORMAL, "Normal", L.LABEL_STYLE_NORMAL));
        register(APPLICATION, createBookStyle());
        register(APPLICATION, htmlExportStyle());
        register(APPLICATION, epubExportStyle());
        register(APPLICATION, pdfExportStyle());
        register(APPLICATION, docxExportStyle());
    }

    /**
     * The look the HTML export had before styles existed (the text part of its stylesheet); the exporter uses it when no style is chosen.
     */
    public static PartStyle htmlExportStyle() {
        return PartStyle.builtin(EXPORT_HTML, "HTML page", L.LABEL_STYLE_EXPORT_HTML).setForExport(true)
                .set(PartStyleOption.FONT_FAMILY, "Georgia, 'Times New Roman', serif")
                .set(PartStyleOption.LINE_HEIGHT, "1.6")
                .set(PartStyleOption.TEXT_COLOR, "#222")
                .set(PartStyleOption.TEXT_ALIGN, "justify")
                .set(PartStyleOption.HYPHENATION, "true")
                .set(PartStyleOption.PARAGRAPH_SPACING, "1em")
                .set(PartStyleOption.QUOTE_COLOR, "#444")
                .set(PartStyleOption.QUOTE_BORDER_COLOR, "#bbb")
                .set(PartStyleOption.CODE_FONT_FAMILY, "'Courier New', monospace")
                .set(PartStyleOption.SCENE_BREAK_TEXT, "* * *");
    }

    /**
     * The look the EPUB export had before styles existed; the exporter uses it when no style is chosen.
     */
    public static PartStyle epubExportStyle() {
        return PartStyle.builtin(EXPORT_EPUB, "E-book (EPUB)", L.LABEL_STYLE_EXPORT_EPUB).setForExport(true)
                .set(PartStyleOption.FONT_FAMILY, "serif")
                .set(PartStyleOption.LINE_HEIGHT, "1.5")
                .set(PartStyleOption.TEXT_ALIGN, "justify")
                .set(PartStyleOption.PARAGRAPH_SPACING, "1em")
                .set(PartStyleOption.QUOTE_BORDER_COLOR, "#999")
                .set(PartStyleOption.CODE_FONT_FAMILY, "monospace")
                .set(PartStyleOption.SCENE_BREAK_TEXT, "* * *");
    }

    /**
     * The look the PDF export had before styles existed (the fonts are the embedded Liberation ones); the exporter uses it when no style is chosen.
     */
    public static PartStyle pdfExportStyle() {
        return PartStyle.builtin(EXPORT_PDF, "PDF book", L.LABEL_STYLE_EXPORT_PDF).setForExport(true)
                .set(PartStyleOption.FONT_FAMILY, "serif")
                .set(PartStyleOption.FONT_SIZE, "12pt")
                .set(PartStyleOption.LINE_HEIGHT, "1.4")
                .set(PartStyleOption.TEXT_ALIGN, "justify")
                .set(PartStyleOption.PARAGRAPH_SPACING, "8pt")
                .set(PartStyleOption.HEADING_ALIGN, "left")
                .set(PartStyleOption.SCENE_BREAK_TEXT, "*   *   *");
    }

    /**
     * The look the Word export had before styles existed; the exporter uses it when no style is chosen. It does not set the line
     * height, Word keeps its single spacing.
     */
    public static PartStyle docxExportStyle() {
        return PartStyle.builtin(EXPORT_DOCX, "Word document", L.LABEL_STYLE_EXPORT_DOCX).setForExport(true)
                .set(PartStyleOption.FONT_FAMILY, "\"Times New Roman\", serif")
                .set(PartStyleOption.FONT_SIZE, "12pt")
                .set(PartStyleOption.TEXT_ALIGN, "justify")
                .set(PartStyleOption.PARAGRAPH_SPACING, "8pt")
                .set(PartStyleOption.HEADING_ALIGN, "left")
                .set(PartStyleOption.SCENE_BREAK_TEXT, "*   *   *");
    }

    /**
     * The book style: the reading mode of the manuscript (what the story showed when its styles were turned on).
     */
    static PartStyle createBookStyle() {
        PartStyle book = PartStyle.builtin(BOOK, "Book", L.LABEL_STYLE_BOOK);
        book.set(PartStyleOption.FONT_FAMILY, "\"Garamond\", \"Georgia\", \"Times New Roman\", serif")
                .set(PartStyleOption.FONT_SIZE, "1.04rem")
                .set(PartStyleOption.LINE_HEIGHT, "1.8")
                .set(PartStyleOption.LETTER_SPACING, "0.012em")
                .set(PartStyleOption.TEXT_COLOR, "var(--lumo-body-text-color, #e0e0e0)")
                .set(PartStyleOption.TEXT_ALIGN, "justify")
                .set(PartStyleOption.HYPHENATION, "true")
                .set(PartStyleOption.PARAGRAPH_SPACING, "0.85em")
                .set(PartStyleOption.EMPHASIS_COLOR, "light-dark(#5e35b1, #d1c4e9)")
                .set(PartStyleOption.STRONG_COLOR, "var(--lumo-header-text-color, light-dark(#000000, #ffffff))")
                .set(PartStyleOption.QUOTE_COLOR, "light-dark(#546e7a, #b0bec5)")
                .set(PartStyleOption.QUOTE_BORDER_COLOR, "var(--lumo-primary-color, #7e57c2)")
                .set(PartStyleOption.HEADING_FONT_FAMILY, "\"Garamond\", \"Georgia\", serif")
                .set(PartStyleOption.HEADING_COLOR, "var(--lumo-header-text-color, light-dark(#000000, #ffffff))")
                .set(PartStyleOption.HEADING_ALIGN, "center")
                .set(PartStyleOption.HEADING_SMALL_CAPS, "true")
                .set(PartStyleOption.SCENE_BREAK_TEXT, "•  •  •")
                .set(PartStyleOption.SCENE_BREAK_COLOR, "var(--lumo-contrast-40pct, #888888)");
        return book;
    }

    /**
     * Adds the style (one with the same id is replaced); it becomes a built in style.
     *
     * @param owner who registers it, {@code "marginalia"} for the application, the symbolic name of the bundle for an extension
     */
    public synchronized void register(String owner, PartStyle style) {
        Objects.requireNonNull(style.getId(), "id");
        PartStyle builtin = PartStyle.builtin(style.getId(), style.getName(), null).setForExport(style.isForExport());
        // keep the localized name of a style the application registered itself
        styles.put(style.getId(), style.isBuiltin() ? style : copyAsBuiltin(style, builtin));
        owners.put(style.getId(), owner);
    }

    private static PartStyle copyAsBuiltin(PartStyle source, PartStyle target) {
        source.getOptions().forEach((key, value) -> target.getOptions().put(key, value));
        target.setCustomCss(source.getCustomCss());
        return target;
    }

    public synchronized void unregister(String id) {
        styles.remove(id);
        owners.remove(id);
    }

    /**
     * Removes the styles an owner registered.
     */
    public synchronized void unregisterAll(String owner) {
        new ArrayList<>(owners.entrySet()).stream().filter(entry -> Objects.equals(owner, entry.getValue()))
                .map(Map.Entry::getKey).forEach(this::unregister);
    }

    /**
     * The built in style of the id, null when there is none.
     */
    public synchronized PartStyle find(String id) {
        return id == null ? null : styles.get(id);
    }

    public synchronized boolean isBuiltin(String id) {
        return id != null && styles.containsKey(id);
    }

    /**
     * The built in styles in the order of their registration.
     */
    public synchronized List<PartStyle> getAll() {
        return List.copyOf(styles.values());
    }
}
