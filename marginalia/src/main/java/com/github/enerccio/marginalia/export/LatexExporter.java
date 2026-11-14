package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A LaTeX project in a ZIP file: a folder named after the book with {@code main.tex} (a {@code book} that compiles with {@code pdflatex},
 * {@code xelatex} or {@code lualatex}; the chapters are the headings of the story, the footnotes are footnotes) and the images of the book in
 * {@code images/}. A single {@code .tex} could not carry the images.
 * <p>
 * The shallowest heading of the story is a chapter, the next level a section and so on, the headings are not numbered (a story names its
 * chapters itself) but are listed in the table of contents. The title page is the one of the export settings (its first heading is the title).
 */
public class LatexExporter extends ExporterBase {
    // kana, ideographs, hangul, the CJK punctuation and the full width forms
    private static final Pattern CJK = Pattern.compile("[\\u2E80-\\u9FFF\\uAC00-\\uD7AF\\uF900-\\uFAFF\\uFF00-\\uFFEF]");
    private static final String[] LEVELS = {"chapter", "section", "subsection", "subsubsection", "paragraph", "subparagraph"};

    @Override
    public String getName() {
        return "LaTeX";
    }

    @Override
    public String getId() {
        return "latex";
    }

    @Override
    public String getFileExtension() {
        return "zip";
    }

    @Override
    public String getMimeType() {
        return "application/zip";
    }

    @Override
    protected ExportWriter open(ExportOptions options, OutputStream out) {
        return new LatexWriter(options, out);
    }

    /**
     * @return the text safe to put into LaTeX, the characters that mean something to it written as text
     */
    static String escape(String text) {
        StringBuilder result = new StringBuilder(text.length() + 8);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\' -> result.append("\\textbackslash{}");
                case '{' -> result.append("\\{");
                case '}' -> result.append("\\}");
                case '$' -> result.append("\\$");
                case '&' -> result.append("\\&");
                case '#' -> result.append("\\#");
                case '%' -> result.append("\\%");
                case '_' -> result.append("\\_");
                case '^' -> result.append("\\textasciicircum{}");
                case '~' -> result.append("\\textasciitilde{}");
                // the typewriter fonts of TeX turn these into ligatures or other characters
                case '<' -> result.append("\\textless{}");
                case '>' -> result.append("\\textgreater{}");
                case '|' -> result.append("\\textbar{}");
                // an active character of babel (German, Czech, ...)
                case '"' -> result.append("\\textquotedbl{}");
                case '\u00A0' -> result.append('~');
                case '\r' -> {
                }
                default -> result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * @return the language name of {@code babel} for the ISO 639-1 code; null when none is known and the text is set in English
     */
    static String babel(String language) {
        if (StringUtils.isBlank(language)) {
            return null;
        }
        return switch (language.strip().toLowerCase(Locale.ROOT).replace('_', '-').split("-")[0]) {
            case "cs" -> "czech";
            case "sk" -> "slovak";
            case "de" -> "ngerman";
            case "fr" -> "french";
            case "es" -> "spanish";
            case "it" -> "italian";
            case "pt" -> "portuguese";
            case "nl" -> "dutch";
            case "pl" -> "polish";
            case "ru" -> "russian";
            case "uk" -> "ukrainian";
            case "sv" -> "swedish";
            case "da" -> "danish";
            case "nb", "no" -> "norsk";
            case "fi" -> "finnish";
            case "hu" -> "magyar";
            case "ro" -> "romanian";
            case "hr" -> "croatian";
            case "sl" -> "slovene";
            case "tr" -> "turkish";
            case "el" -> "greek";
            case "en" -> "english";
            default -> null;
        };
    }

    /**
     * @return the text of the runs set in LaTeX; the notes, by their label, are footnotes of the place they are referred to
     */
    private static String runs(List<Run> runs, Map<String, List<List<Run>>> notes) {
        StringBuilder result = new StringBuilder();
        for (Run run : runs) {
            if (run.lineBreak()) {
                result.append("\\\\\n");
            } else if (run.note() != null) {
                result.append(footnote(run.note(), notes));
            } else if (!run.text().isEmpty()) {
                result.append(run(run));
            }
        }
        return result.toString();
    }

    private static String run(Run run) {
        String text = escape(run.text());
        if (run.code()) {
            text = "\\texttt{" + text + "}";
        }
        if (run.italic()) {
            text = "\\textit{" + text + "}";
        }
        if (run.bold()) {
            text = "\\textbf{" + text + "}";
        }
        return text;
    }

    private static String footnote(String label, Map<String, List<List<Run>>> notes) {
        List<List<Run>> paragraphs = notes.get(label);
        if (paragraphs == null) {
            // a reference without a note stays as the text it was
            return escape("[" + label + "]");
        }
        List<String> texts = new ArrayList<>();
        for (List<Run> paragraph : paragraphs) {
            texts.add(runs(paragraph, Map.of()));
        }
        return "\\footnote{" + String.join("\n\n", texts) + "}";
    }

    /**
     * A part of the book that is written once the levels of the headings are known.
     */
    private sealed interface Piece permits Source, Heading {
    }

    private record Source(String latex) implements Piece {
    }

    private record Heading(int level, String title) implements Piece {
    }

    /**
     * The blocks of Markdown as LaTeX: the quotes and the lists are environments that are opened and closed as the blocks go in and out of
     * them.
     */
    private static class BlockWriter implements BlockSink {
        private enum Environment {QUOTE, LIST}

        private final List<Piece> pieces;
        private final List<Environment> open = new ArrayList<>();
        private final boolean titlePage;
        private Map<String, List<List<Run>>> notes = Map.of();
        private final StringBuilder text = new StringBuilder();

        /**
         * @param titlePage true when the blocks are the title page: its headings are big lines of the page, not the headings of the book
         */
        private BlockWriter(List<Piece> pieces, boolean titlePage) {
            this.pieces = pieces;
            this.titlePage = titlePage;
        }

        @Override
        public boolean notes(Map<String, List<List<Run>>> notes) {
            this.notes = notes;
            return true;
        }

        @Override
        public void heading(int level, List<Run> runs) {
            enter(0, 0);
            if (titlePage) {
                text.append("\\marginaliatitle{").append(LatexExporter.runs(runs, Map.of())).append("}\n\n");
                return;
            }
            flush();
            // the title is a plain text: a note, a break or the emphasis do not belong into a table of contents
            StringBuilder title = new StringBuilder();
            runs.forEach(run -> title.append(run.lineBreak() ? " " : run.note() != null ? "" : run.text()));
            pieces.add(new Heading(level, title.toString().strip()));
        }

        @Override
        public void paragraph(BlockContext context, List<Run> runs) {
            enter(context.quoteDepth(), context.listDepth());
            if (context.listDepth() > 0 && context.marker() != null) {
                text.append("\\item[").append(marker(context.marker())).append("] ");
            }
            text.append(LatexExporter.runs(runs, notes)).append("\n\n");
        }

        @Override
        public void code(BlockContext context, String code) {
            enter(context.quoteDepth(), context.listDepth());
            if (context.listDepth() > 0 && context.marker() != null) {
                text.append("\\item[").append(marker(context.marker())).append("] ");
            }
            text.append("\\begin{verbatim}\n").append(code.stripTrailing().replace("\\end{verbatim}", "\\end {verbatim}"))
                    .append("\n\\end{verbatim}\n\n");
        }

        @Override
        public void rule() {
            enter(0, 0);
            text.append("\\begin{center}\n\\textasteriskcentered\\quad\\textasteriskcentered\\quad\\textasteriskcentered\n\\end{center}\n\n");
        }

        /**
         * Closes what is still open and hands the text over.
         */
        void end() {
            enter(0, 0);
            flush();
        }

        private void flush() {
            if (!text.isEmpty()) {
                pieces.add(new Source(text.toString()));
                text.setLength(0);
            }
        }

        private static String marker(String marker) {
            return "•".equals(marker) ? "\\textbullet" : escape(marker);
        }

        /**
         * Opens and closes the environments until there are the quotes and the lists the block is in; the quotes come first.
         */
        private void enter(int quotes, int lists) {
            List<Environment> target = new ArrayList<>();
            for (int i = 0; i < quotes; i++) {
                target.add(Environment.QUOTE);
            }
            for (int i = 0; i < lists; i++) {
                target.add(Environment.LIST);
            }
            int common = 0;
            while (common < open.size() && common < target.size() && open.get(common) == target.get(common)) {
                common++;
            }
            while (open.size() > common) {
                Environment closed = open.removeLast();
                text.append(closed == Environment.QUOTE ? "\\end{quote}\n\n" : "\\end{list}\n\n");
            }
            for (int i = common; i < target.size(); i++) {
                open.add(target.get(i));
                text.append(target.get(i) == Environment.QUOTE ? "\\begin{quote}\n"
                        : "\\begin{list}{}{\\setlength{\\itemsep}{0pt}\\setlength{\\parsep}{0.4\\baselineskip}"
                        + "\\setlength{\\labelwidth}{1.5em}\\setlength{\\leftmargin}{2em}\\setlength{\\labelsep}{0.5em}}\n");
            }
        }
    }

    private static class LatexWriter implements ExportWriter {
        private final ExportOptions options;
        private final List<Piece> pieces = new ArrayList<>();
        // the images go into the zip as they come, the text at the end
        private final ZipOutputStream zip;
        private final String book;
        private int images;
        private String titlePage;
        private boolean contents;

        private LatexWriter(ExportOptions options, OutputStream out) {
            this.options = options;
            this.zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
            this.book = folderName(options.bookName());
        }

        @Override
        public void titlePage(String markdown) throws Exception {
            List<Piece> title = new ArrayList<>();
            BlockWriter blocks = new BlockWriter(title, true);
            walk(markdown, blocks);
            blocks.end();
            StringBuilder page = new StringBuilder("\\begin{titlepage}\n\\centering\n\\vspace*{\\fill}\n");
            for (Piece piece : title) {
                if (piece instanceof Source source) {
                    page.append(source.latex());
                }
            }
            titlePage = page.append("\\vspace*{\\fill}\n\\end{titlepage}\n").toString();
        }

        @Override
        public void contents(List<Chapter> chapters) {
            contents = true;
        }

        @Override
        public void message(String markdown, Chapter chapter) throws Exception {
            BlockWriter blocks = new BlockWriter(pieces, false);
            walk(markdown, blocks);
            blocks.end();
        }

        @Override
        public void images(List<ExportImage> images) throws IOException {
            for (ExportImage image : images) {
                this.images++;
                String file = "image-" + this.images + "." + image.extension();
                zip.putNextEntry(new ZipEntry(book + "/images/" + file));
                zip.write(image.data());
                zip.closeEntry();
                StringBuilder figure = new StringBuilder("\\begin{figure}[htbp]\n\\centering\n\\includegraphics[");
                // the width is a part of the width of the text, the images are never wider than it
                figure.append("width=").append(width(image)).append("\\textwidth,keepaspectratio]{images/").append(file).append("}\n");
                if (!image.caption().isEmpty()) {
                    figure.append("\\caption*{").append(escape(image.caption().replaceAll("\\s+", " "))).append("}\n");
                }
                pieces.add(new Source(figure.append("\\end{figure}\n\n").toString()));
            }
        }

        private static String width(ExportImage image) {
            return image.width() == null ? "1" : String.format(Locale.ROOT, "%.2f", Math.min(100, Math.max(1, image.width())) / 100.0);
        }

        @Override
        public void finish() throws Exception {
            zip.putNextEntry(new ZipEntry(book + "/main.tex"));
            zip.write(main().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            // the stream itself is closed by the exporter
            zip.finish();
        }

        private static boolean hasCjk(List<Piece> pieces, String titlePage) {
            if (titlePage != null && CJK.matcher(titlePage).find()) {
                return true;
            }
            for (Piece piece : pieces) {
                String text = piece instanceof Source source ? source.latex() : ((Heading) piece).title();
                if (CJK.matcher(text).find()) {
                    return true;
                }
            }
            return false;
        }

        private static String folderName(String name) {
            String folder = StringUtils.defaultString(name).replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").replaceAll("\\s+", " ").trim();
            folder = StringUtils.stripStart(folder, ".").trim();
            return folder.isEmpty() ? "book" : StringUtils.abbreviate(folder, "", 100).trim();
        }

        private String main() {
            // the shallowest heading is the chapter
            int shallowest = pieces.stream().filter(p -> p instanceof Heading).mapToInt(p -> ((Heading) p).level()).min().orElse(1);
            String babel = babel(options.language());

            StringBuilder tex = new StringBuilder();
            tex.append("% Exported from Marginalia. Compile with pdflatex, xelatex or lualatex (xelatex or lualatex for scripts other than Latin).\n");
            tex.append("\\documentclass[11pt,a4paper,openany]{book}\n");
            // pdfLaTeX needs the encodings, XeLaTeX and LuaLaTeX read Unicode and take the fonts from fontspec (Latin Modern by default)
            tex.append("\\usepackage{iftex}\n\\ifPDFTeX\n  \\usepackage[T1]{fontenc}\n  \\usepackage[utf8]{inputenc}\n  \\usepackage{lmodern}\n\\else\n  \\usepackage{fontspec}\n\\fi\n");
            // the last language is the one the text is in
            tex.append("\\usepackage[").append(babel != null && !"english".equals(babel) ? "english," + babel : "english").append("]{babel}\n");
            tex.append("\\usepackage{microtype}\n\\usepackage{graphicx}\n\\usepackage{caption}\n");
            if (hasCjk(pieces, titlePage)) {
                // Japanese, Chinese or Korean text: pdfLaTeX has no way to set it, the others break the lines and take the font of the author
                tex.append("% This book has Japanese, Chinese or Korean text: compile it with XeLaTeX or LuaLaTeX and, with XeLaTeX, set a font that has the characters,\n"
                        + "% for example \\setCJKmainfont{Noto Serif CJK JP} after \\usepackage{xeCJK}.\n"
                        + "\\ifPDFTeX\n  \\PackageError{marginalia}{This book has CJK text}{Compile it with XeLaTeX or LuaLaTeX.}\n"
                        + "\\else\\ifXeTeX\n  \\usepackage{xeCJK}\n\\else\n  \\usepackage{luatexja}\n\\fi\\fi\n");
            }
            tex.append("\\usepackage[hidelinks]{hyperref}\n");
            tex.append("\\setcounter{tocdepth}{2}\n");
            tex.append("\\title{").append(escape(options.bookName())).append("}\n");
            tex.append("\\author{").append(escape(StringUtils.defaultString(StringUtils.trimToNull(options.author())))).append("}\n");
            tex.append("\\date{}\n");
            tex.append("\\newcommand{\\marginaliatitle}[1]{{\\Huge\\bfseries #1\\par}\\vspace{1.5em}}\n");
            tex.append("\n\\begin{document}\n\n");
            if (titlePage != null) {
                tex.append(titlePage).append('\n');
            }
            if (contents) {
                tex.append("\\tableofcontents\n\\clearpage\n\n");
            }
            for (Piece piece : pieces) {
                if (piece instanceof Source source) {
                    tex.append(source.latex());
                } else if (piece instanceof Heading heading) {
                    String command = LEVELS[Math.min(LEVELS.length - 1, Math.max(0, heading.level() - shallowest))];
                    String title = escape(heading.title());
                    // not numbered: the story names its chapters, the table of contents lists them all the same
                    tex.append('\\').append(command).append("*{").append(title).append("}\n");
                    tex.append("\\addcontentsline{toc}{").append(command).append("}{").append(title).append("}\n\n");
                }
            }
            tex.append("\\end{document}\n");
            return tex.toString();
        }
    }
}
