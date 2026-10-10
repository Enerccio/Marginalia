package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.ExportHeaderTemplateData;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private TemplateService templateService;

    /**
     * Receives the exported content in the story order, {@link #finish()} produces the file.
     */
    protected interface ExportWriter {

        /**
         * @param markdown Markdown of the title page
         */
        void titlePage(String markdown) throws Exception;

        void message(String markdown) throws Exception;

        byte[] finish() throws Exception;
    }

    protected abstract ExportWriter open(ExportOptions options) throws Exception;

    @Override
    public byte[] export(ExportOptions options, List<ChatMessage> chatMessages, ProgressBarDialog progress) throws Exception {
        ExportWriter writer = open(options);
        if (options.includeHeaders()) {
            writer.titlePage(buildHeader(options));
        }
        for (ChatMessage message : chatMessages) {
            String text = StringUtils.trimToNull(message.getResponse());
            if (text != null) {
                writer.message(text);
            }
            progress.updateProgress();
        }
        return writer.finish();
    }

    protected String buildHeader(ExportOptions options) throws Exception {
        return templateService.processTemplate(Defaults.DEFAULT_EXPORT_HEADER_TEMPLATE, "exportHeader",
                new ExportHeaderTemplateData(options.bookName(), StringUtils.trimToNull(options.author())));
    }

    protected static Node parse(String markdown) {
        return PARSER.parse(markdown);
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
