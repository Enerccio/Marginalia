package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.ImageAnchor;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.PartLayout;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportTemplate;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.ExportFrontMatterTemplateData;
import com.github.enerccio.marginalia.domain.templates.ExportHeaderTemplateData;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common part of the exporters. The text of a message is Markdown, the base class feeds the title page and messages to
 * the {@link ExportWriter} of the format one by one and offers the helpers to turn the Markdown into the target format.
 * <p>
 * Exporters are shared between users, so everything that changes during the export lives in the writer.
 */
@Configurable(preConstruction = true)
public abstract class ExporterBase implements Exporter {
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML = HtmlRenderer.builder().escapeHtml(true).sanitizeUrls(true).build();
    // the first heading of a message is its chapter title, the same rule the Chapter Marker plugin uses in the sidebar
    private static final Pattern HEADER_PATTERN = Pattern.compile("(?m)^\\s*#+\\s*(.+)$");

    protected static final String CONTENTS_TITLE = "Contents";

    /**
     * The title page, which every format writes the same way (the Markdown the template renders); the user can change the template in the
     * settings.
     */
    public static final ExportTemplate TITLE_PAGE_TEMPLATE = new ExportTemplate("titlePage", "Title page",
            Defaults.DEFAULT_EXPORT_HEADER_TEMPLATE, ExportHeaderTemplateData.class);

    /**
     * The YAML front matter of the formats that have one (Markdown for static site generators, the Obsidian vault); the user can change the
     * template in the settings.
     */
    public static final ExportTemplate FRONT_MATTER_TEMPLATE = new ExportTemplate("frontMatter", "Front matter",
            Defaults.DEFAULT_EXPORT_FRONT_MATTER_TEMPLATE, ExportFrontMatterTemplateData.class);

    private static final Logger log = LoggerFactory.getLogger(ExporterBase.class);

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ResourceService resourceService;

    /**
     * Image attached to a message, as stored (PNG, JPEG or GIF), with where it is in the message.
     *
     * @param caption text shown with the image, empty if none
     * @param anchor  where the image is (the row is the place in the text, the column the side)
     * @param wrap    true when the text flows around the image: the anchor is at the left or at the right
     * @param width   width of the image in percent of the width of the text, null for the size of the image (never wider than the text)
     */
    protected record ExportImage(byte[] data, String mimeType, String caption, ImageAnchor anchor, boolean wrap, Integer width) {

        /**
         * An image after the text, in the size it has.
         */
        public ExportImage(byte[] data, String mimeType, String caption) {
            this(data, mimeType, caption, ImageAttachment.DEFAULT_ANCHOR, false, null);
        }

        public String extension() {
            return mimeType.substring("image/".length());
        }
    }

    /**
     * A piece of a message: a run of whole blocks of its text, or the images that are between them (the same anchor and wrapping).
     */
    protected sealed interface Part permits TextPart, PicturePart, BandPart {
    }

    protected record TextPart(String markdown) implements Part {
    }

    protected record PicturePart(ImageAnchor anchor, boolean wrap, List<ExportImage> images) implements Part {
    }

    /**
     * Images in the middle of some text: the text in two columns, the images between them (one above the other). A format that has
     * no columns writes the left text, the images and the right text.
     *
     * @param left  Markdown that is read first, at the left of the images
     * @param right Markdown that follows, at the right of the images
     */
    protected record BandPart(List<ExportImage> images, String left, String right) implements Part {
    }

    /**
     * Message that starts with a heading is a chapter, the heading is its title.
     *
     * @param number 1 based position among the chapters of the export
     */
    protected record Chapter(int number, String title) {

        /**
         * @return name of the target the table of contents links to, valid as an HTML id, PDF destination and Word bookmark
         */
        public String anchor() {
            return "chapter_" + number;
        }
    }

    /**
     * Receives the exported content in the story order, {@link #finish()} produces the file.
     */
    protected interface ExportWriter {

        /**
         * @param markdown Markdown of the title page
         */
        void titlePage(String markdown) throws Exception;

        /**
         * Called once before the first message, only if the export has chapters.
         */
        default void contents(List<Chapter> chapters) throws Exception {
        }

        /**
         * @param chapter chapter that starts with this message, null if the message is not the start of a chapter
         */
        void message(String markdown, Chapter chapter) throws Exception;

        /**
         * Called after {@link #message} for the images that are put between the pieces of the text of a message (and after all of it, or
         * before all of it); the images have the same anchor and wrapping. The text of a message that has only images is empty.
         * A format that does not place images writes them where they come.
         */
        default void images(List<ExportImage> images) throws Exception {
        }

        /**
         * A message in its pieces, in the order they are shown. The default writes the first piece with {@link #message(String, Chapter)}
         * (an empty text when the message starts with images, so the chapter starts where the message does), the other text with
         * {@code message(text, null)} and the images with {@link #images} (the text at the left of images in the middle of it, the images,
     * the text at their right). A format that places the images overrides this.
         */
        default void message(List<Part> parts, Chapter chapter) throws Exception {
            boolean started = false;
            for (Part part : parts) {
                if (part instanceof TextPart text) {
                    message(text.markdown(), started ? null : chapter);
                    started = true;
                } else if (part instanceof PicturePart pictures) {
                    if (!started) {
                        message("", chapter);
                        started = true;
                    }
                    images(pictures.images());
                } else if (part instanceof BandPart band) {
                    message(band.left(), started ? null : chapter);
                    started = true;
                    images(band.images());
                    message(band.right(), null);
                }
            }
        }

        byte[] finish() throws Exception;
    }

    @Override
    public List<ExportTemplate> getTemplates() {
        return List.of(TITLE_PAGE_TEMPLATE);
    }

    protected abstract ExportWriter open(ExportOptions options) throws Exception;

    @Override
    public byte[] export(ExportOptions options, List<ChatMessage> chatMessages, ProgressBarDialog progress) throws Exception {
        ExportWriter writer = open(options);
        if (options.includeHeaders()) {
            writer.titlePage(buildHeader(options));
        }

        // the table of contents comes before the text, so the chapters have to be known first
        List<List<Part>> partsOf = new ArrayList<>();
        List<Chapter> chapterOf = new ArrayList<>();
        List<Chapter> chapters = new ArrayList<>();
        for (ChatMessage message : chatMessages) {
            String text = StringUtils.trimToNull(message.getResponse());
            List<ImageAttachment> attached = new ArrayList<>();
            List<ExportImage> images = loadImages(message, attached);
            if (text != null || !images.isEmpty()) {
                text = StringUtils.defaultString(text);
                String title = chapterTitle(text);
                Chapter chapter = title == null ? null : new Chapter(chapters.size() + 1, title);
                if (chapter != null) {
                    chapters.add(chapter);
                }
                partsOf.add(parts(text, attached, images));
                chapterOf.add(chapter);
            }
        }
        if (!chapters.isEmpty()) {
            writer.contents(chapters);
        }

        for (int i = 0; i < partsOf.size(); i++) {
            writer.message(partsOf.get(i), chapterOf.get(i));
            progress.updateProgress();
        }
        return writer.finish();
    }

    /**
     * @param attached the attachments of the images that were found, in the order of the result
     */
    private List<ExportImage> loadImages(ChatMessage message, List<ImageAttachment> attached) throws Exception {
        List<ExportImage> result = new ArrayList<>();
        for (ImageAttachment attachment : message.getImages()) {
            Resource resource = resourceService.findImage(attachment.resource());
            if (resource == null) {
                // removed or not the exporting user's, the book is exported without it
                log.warn("Image {} of message {} is not available, skipping it", attachment.resource(), message.getUuid());
                continue;
            }
            attached.add(attachment);
            result.add(new ExportImage(resourceService.getResourceData(resource), resource.getMimeType(),
                    StringUtils.defaultString(attachment.caption()).trim(), attachment.effectiveAnchor(), attachment.floats(),
                    attachment.effectiveWidth()));
        }
        return result;
    }

    /**
     * The message cut in the text and the images where their anchors put them, see {@link PartLayout}.
     *
     * @param attached the attachments of the images, the images are the same ones in the same order
     */
    private static List<Part> parts(String text, List<ImageAttachment> attached, List<ExportImage> images) {
        List<Part> parts = new ArrayList<>();
        for (PartLayout.Segment segment : PartLayout.layout(text, attached)) {
            switch (segment) {
                case PartLayout.Text piece -> parts.add(new TextPart(piece.markdown()));
                case PartLayout.Surrounded band -> parts.add(new BandPart(exportImages(band.images(), attached, images), band.left(), band.right()));
                case PartLayout.Pictures pictures ->
                        parts.add(new PicturePart(pictures.anchor(), pictures.wrap(), exportImages(pictures.images(), attached, images)));
            }
        }
        return parts;
    }

    /**
     * @return the images of the group: the objects of the list the layout was made of are the attachments, the same positions of the
     * list of the loaded images are theirs
     */
    private static List<ExportImage> exportImages(List<ImageAttachment> group, List<ImageAttachment> attached, List<ExportImage> images) {
        List<ExportImage> result = new ArrayList<>();
        for (ImageAttachment image : group) {
            for (int i = 0; i < attached.size(); i++) {
                if (attached.get(i) == image) {
                    result.add(images.get(i));
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @return title of the chapter the message starts, null if the message has no heading
     */
    protected static String chapterTitle(String markdown) {
        Matcher matcher = HEADER_PATTERN.matcher(markdown);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    /**
     * @param href link target of a chapter
     * @return XHTML compatible list of the links to the chapters
     */
    protected static String contentsList(List<Chapter> chapters, Function<Chapter, String> href) {
        StringBuilder list = new StringBuilder("<ol>\n");
        for (Chapter chapter : chapters) {
            list.append("<li><a href=\"").append(escapeXml(href.apply(chapter))).append("\">")
                    .append(escapeXml(chapter.title())).append("</a></li>\n");
        }
        return list.append("</ol>\n").toString();
    }

    protected String buildHeader(ExportOptions options) throws Exception {
        return templateService.processTemplate(options.templateText(TITLE_PAGE_TEMPLATE), "exportHeader",
                new ExportHeaderTemplateData(options.bookName(), StringUtils.trimToNull(options.author())));
    }

    /**
     * @return the front matter block (with the {@code ---} lines) the template of the export renders
     */
    protected String buildFrontMatter(ExportOptions options) throws Exception {
        return templateService.processTemplate(options.templateText(FRONT_MATTER_TEMPLATE), "exportFrontMatter",
                new ExportFrontMatterTemplateData(yamlQuoted(options.bookName()), StringUtils.trimToNull(yamlQuoted(options.author())),
                        StringUtils.defaultIfBlank(options.language(), "en"), LocalDate.now().toString()));
    }

    /**
     * @return the text escaped for use inside a double quoted YAML string, on one line
     */
    protected static String yamlQuoted(String text) {
        return StringUtils.defaultString(text).replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("\\s*[\\r\\n]+\\s*", " ").trim();
    }

    protected static Node parse(String markdown) {
        return PARSER.parse(markdown);
    }

    /**
     * @param source value of the {@code src} attribute of the image
     * @return XHTML compatible figure of the image
     */
    protected static String figure(String source, String caption) {
        String text = StringUtils.isBlank(caption) ? "" : "<figcaption>" + escapeXml(caption) + "</figcaption>\n";
        return "<figure><img src=\"" + escapeXml(source) + "\" alt=\"" + escapeXml(caption) + "\"/>\n" + text + "</figure>\n";
    }

    /**
     * The rules of the figures that {@link #toHtml(List, Function)} writes, for the HTML, the EPUB and the other formats built from HTML.
     */
    protected static final String PICTURES_CSS = """
            .pictures { display: flex; flex-wrap: wrap; align-items: flex-start; gap: 0.8em; clear: both; margin: 1em 0; }
            .pictures-left { justify-content: flex-start; }
            .pictures-center { justify-content: center; }
            .pictures-right { justify-content: flex-end; }
            .pictures figure, figure.float-left, figure.float-right { margin: 0; }
            figure.sized { flex: 0 0 auto; }
            figure.sized img { width: 100%; }
            figure.float-left { float: left; margin: 0 1em 0.6em 0; }
            figure.float-right { float: right; margin: 0 0 0.6em 1em; }
            .clear { clear: both; }
            .band { display: grid; gap: 1.2em; align-items: start; clear: both; margin: 1em 0; }
            .band-middle { align-self: center; }
            .band-middle figure { margin: 0 0 0.8em 0; }
            .band-text > :first-child { margin-top: 0; }
            .band-text > :last-child { margin-bottom: 0; }
            @media (max-width: 30em) {
              figure.float-left, figure.float-right { float: none; width: 100% !important; margin: 0.6em 0; }
              .band { grid-template-columns: 1fr !important; }
              .band-middle figure { width: 100% !important; margin: 0.6em 0; }
            }
            """;

    /**
     * @param source value of the {@code src} attribute of the image
     * @param width  width in percent of the text, null for the size of the image
     * @param css    class of the figure (the float it is), may be null
     * @return XHTML compatible figure of the image
     */
    protected static String figure(String source, String caption, Integer width, String css) {
        List<String> classes = new ArrayList<>();
        if (css != null) {
            classes.add(css);
        }
        if (width != null) {
            classes.add("sized");
        }
        String attributes = (classes.isEmpty() ? "" : " class=\"" + String.join(" ", classes) + "\"")
                + (width == null ? "" : " style=\"width: " + width + "%\"");
        String text = StringUtils.isBlank(caption) ? "" : "<figcaption>" + escapeXml(caption) + "</figcaption>\n";
        return "<figure" + attributes + "><img src=\"" + escapeXml(source) + "\" alt=\"" + escapeXml(caption) + "\"/>\n" + text + "</figure>\n";
    }

    /**
     * @param source gives the {@code src} of an image (the file in the archive, or the data of it)
     * @return XHTML compatible HTML of a message in its pieces: the text, and the images where they belong
     */
    protected static String toHtml(List<Part> parts, Function<ExportImage, String> source) {
        StringBuilder html = new StringBuilder();
        boolean floats = false;
        for (Part part : parts) {
            switch (part) {
                case TextPart text -> html.append(toHtml(text.markdown()));
                case BandPart band -> {
                    // the width of the column of the images is the widest of them, each is its own part of it
                    int widest = band.images().stream().mapToInt(image -> image.width() == null ? ImageAttachment.SURROUNDED_WIDTH : image.width())
                            .max().orElse(ImageAttachment.SURROUNDED_WIDTH);
                    html.append("<div class=\"band\" style=\"grid-template-columns: 1fr ").append(widest).append("% 1fr\">\n<div class=\"band-text\">\n")
                            .append(toHtml(band.left())).append("</div>\n<div class=\"band-middle\">\n");
                    for (ExportImage image : band.images()) {
                        int width = image.width() == null ? ImageAttachment.SURROUNDED_WIDTH : image.width();
                        html.append(figure(source.apply(image), image.caption(), width * 100 / widest, null));
                    }
                    html.append("</div>\n<div class=\"band-text\">\n").append(toHtml(band.right())).append("</div>\n</div>\n");
                }
                case PicturePart pictures -> {
                    if (pictures.wrap()) {
                        floats = true;
                        String css = pictures.anchor().horizontal() == ImageAnchor.Horizontal.LEFT ? "float-left" : "float-right";
                        for (ExportImage image : pictures.images()) {
                            html.append(figure(source.apply(image), image.caption(), image.width(), css));
                        }
                    } else {
                        html.append("<div class=\"pictures pictures-").append(pictures.anchor().horizontal().name().toLowerCase()).append("\">\n");
                        for (ExportImage image : pictures.images()) {
                            html.append(figure(source.apply(image), image.caption(), image.width(), null));
                        }
                        html.append("</div>\n");
                    }
                }
            }
        }
        if (floats) {
            // a picture the text flows around stays in its message
            html.append("<div class=\"clear\"></div>\n");
        }
        return html.toString();
    }

    /**
     * @return XHTML compatible HTML of the markdown, raw HTML in the markdown is escaped
     */
    protected static String toHtml(String markdown) {
        return HTML.render(parse(markdown));
    }

    protected static String escapeXml(String text) {
        return StringUtils.defaultString(text)
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ------------------------------------------------------------------------------------------------------------
    // Markdown as a sequence of blocks, for the formats that are built from paragraphs and runs of text
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Piece of text with the same style.
     */
    protected record Run(String text, boolean bold, boolean italic, boolean code, boolean lineBreak) {

        static final Run LINE_BREAK = new Run("\n", false, false, false, true);
    }

    /**
     * Where a block is, the first block of a list item carries the marker of the item.
     */
    protected record BlockContext(int quoteDepth, int listDepth, String marker) {

        static final BlockContext ROOT = new BlockContext(0, 0, null);

        public boolean isIndented() {
            return quoteDepth > 0 || listDepth > 0;
        }
    }

    protected interface BlockSink {

        void heading(int level, List<Run> runs) throws Exception;

        void paragraph(BlockContext context, List<Run> runs) throws Exception;

        void code(BlockContext context, String code) throws Exception;

        void rule() throws Exception;
    }

    protected static void walk(String markdown, BlockSink sink) throws Exception {
        walk(parse(markdown), BlockContext.ROOT, sink);
    }

    private static void walk(Node parent, BlockContext context, BlockSink sink) throws Exception {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNext()) {
            switch (node) {
                case Heading h -> sink.heading(h.getLevel(), runs(h));
                case Paragraph p -> sink.paragraph(context, runs(p));
                case BlockQuote q -> walk(q, new BlockContext(context.quoteDepth() + 1, context.listDepth(), context.marker()), sink);
                case BulletList l -> walkList(l, context, 0, sink);
                case OrderedList l -> walkList(l, context, l.getMarkerStartNumber() == null ? 1 : l.getMarkerStartNumber(), sink);
                case FencedCodeBlock c -> sink.code(context, c.getLiteral());
                case IndentedCodeBlock c -> sink.code(context, c.getLiteral());
                case ThematicBreak _ -> sink.rule();
                case HtmlBlock b -> sink.paragraph(context, List.of(new Run(b.getLiteral().trim(), false, false, false, false)));
                default -> walk(node, context, sink);
            }
            context = new BlockContext(context.quoteDepth(), context.listDepth(), null);
        }
    }

    private static void walkList(Node list, BlockContext context, int firstNumber, BlockSink sink) throws Exception {
        int number = firstNumber;
        for (Node item = list.getFirstChild(); item != null; item = item.getNext()) {
            String marker = firstNumber == 0 ? "•" : (number++) + ".";
            walk(item, new BlockContext(context.quoteDepth(), context.listDepth() + 1, marker), sink);
        }
    }

    private static List<Run> runs(Node block) {
        List<Run> result = new ArrayList<>();
        collectRuns(block, false, false, false, result);
        return result;
    }

    private static void collectRuns(Node parent, boolean bold, boolean italic, boolean code, List<Run> result) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNext()) {
            switch (node) {
                case Text t -> result.add(new Run(t.getLiteral(), bold, italic, code, false));
                case Code c -> result.add(new Run(c.getLiteral(), bold, italic, true, false));
                case HtmlInline h -> result.add(new Run(h.getLiteral(), bold, italic, code, false));
                case SoftLineBreak _ -> result.add(new Run(" ", bold, italic, code, false));
                case HardLineBreak _ -> result.add(Run.LINE_BREAK);
                case Emphasis e -> collectRuns(e, bold, true, code, result);
                case StrongEmphasis s -> collectRuns(s, true, italic, code, result);
                default -> collectRuns(node, bold, italic, code, result);
            }
        }
    }
}
