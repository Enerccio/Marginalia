package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The LaTeX project: the text of the story as LaTeX, the escaping, the headings, the notes.
 */
class LatexExporterTest extends MarginaliaTestBase {

    @Autowired
    private ExporterService exporterService;

    private static ChatMessage message(String response) {
        ChatMessage message = new ChatMessage();
        message.setResponse(response);
        return message;
    }

    private Exporter latex() {
        return exporterService.getExporters().stream().filter(e -> e.getId().equals("latex")).findFirst().orElseThrow();
    }

    private static Map<String, String> unzip(byte[] zip) throws Exception {
        Map<String, String> files = new LinkedHashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip), StandardCharsets.UTF_8)) {
            for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                files.put(entry.getName(), new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return files;
    }

    private String export(boolean headers, String language, String... texts) throws Exception {
        List<ChatMessage> messages = java.util.Arrays.stream(texts).map(LatexExporterTest::message).toList();
        Map<String, String> files = unzip(latex().export(new ExportOptions("Tom & Jerry_1", "Some Author", headers, language), messages,
                new ProgressBarDialog(false)));
        assertThat(files).containsOnlyKeys("Tom & Jerry_1/main.tex");
        return files.get("Tom & Jerry_1/main.tex");
    }

    @Test
    void theExporterIsAZipOfAProject() {
        assertThat(latex().getName()).isEqualTo("LaTeX");
        assertThat(latex().getFileExtension()).isEqualTo("zip");
    }

    @Test
    void documentHasThePreambleTheMetadataAndTheText() throws Exception {
        String tex = export(true, "cs", "The *first* paragraph with **bold** and `code`.");

        assertThat(tex).contains("\\documentclass").contains("\\usepackage[english,czech]{babel}")
                .contains("\\title{Tom \\& Jerry\\_1}").contains("\\author{Some Author}")
                .contains("\\begin{titlepage}").contains("\\marginaliatitle{Tom \\& Jerry\\_1}").contains("\\textit{Some Author}")
                .contains("The \\textit{first} paragraph with \\textbf{bold} and \\texttt{code}.")
                .contains("\\begin{document}").endsWith("\\end{document}\n");
    }

    @Test
    void withoutHeadersThereIsNoTitlePage() throws Exception {
        String tex = export(false, "en", "Text.");

        assertThat(tex).doesNotContain("\\begin{titlepage}").contains("Text.").contains("\\usepackage[english]{babel}");
    }

    @Test
    void specialCharactersAreWrittenAsText() throws Exception {
        String tex = export(false, "en", "50% of $5 & #1 in a_b {x} ^ ~ \\ < > | \"quoted\" a\u00A0b");

        assertThat(tex).contains("50\\% of \\$5 \\& \\#1 in a\\_b \\{x\\} \\textasciicircum{} \\textasciitilde{} \\textbackslash{} "
                + "\\textless{} \\textgreater{} \\textbar{} \\textquotedbl{}quoted\\textquotedbl{} a~b");
    }

    @Test
    void theShallowestHeadingIsTheChapterAndTheChaptersAreListed() throws Exception {
        String tex = export(false, "en", "## One\n\nText one.\n\n### Part\n\nMore.", "## Two\n\nText two.");

        assertThat(tex).contains("\\tableofcontents")
                .contains("\\chapter*{One}\n\\addcontentsline{toc}{chapter}{One}")
                .contains("\\section*{Part}\n\\addcontentsline{toc}{section}{Part}")
                .contains("\\chapter*{Two}");
        assertThat(tex.indexOf("\\tableofcontents")).isLessThan(tex.indexOf("\\chapter*{One}"));
    }

    @Test
    void cjkTextLoadsAPackageThatCanSetIt() throws Exception {
        assertThat(export(false, "en", "\u5F1F\u306F\u79C1\u306E\u540D\u524D\u3092\u77E5\u3063\u3066\u308B\u308F\u3002"))
                .contains("\\usepackage{xeCJK}").contains("\\usepackage{luatexja}").contains("\\PackageError{marginalia}");
        assertThat(export(false, "en", "Only Latin text, Příliš žluťoučký kůň.")).doesNotContain("CJK");
    }

    @Test
    void noHeadingNoTableOfContents() throws Exception {
        assertThat(export(false, "en", "Just text.")).doesNotContain("\\tableofcontents").doesNotContain("\\chapter");
    }

    @Test
    void quotesListsCodeAndSceneBreaks() throws Exception {
        String tex = export(false, "en", "> A quote\n\n- one\n- two\n\n1. first\n2. second\n\n---\n\n```\nlet x = 1;\n```");

        assertThat(tex).contains("\\begin{quote}\nA quote").contains("\\end{quote}")
                .contains("\\item[\\textbullet] one").contains("\\item[\\textbullet] two")
                .contains("\\item[1.] first").contains("\\item[2.] second")
                .contains("\\textasteriskcentered").contains("\\begin{verbatim}\nlet x = 1;\n\\end{verbatim}");
        // the lists follow each other, their items carry their own labels: one environment
        assertThat(count(tex, "\\begin{list}")).isEqualTo(count(tex, "\\end{list}")).isEqualTo(1);
        assertThat(count(tex, "\\begin{quote}")).isEqualTo(count(tex, "\\end{quote}"));
    }

    @Test
    void footnotesAreFootnotes() throws Exception {
        String tex = export(false, "en", "Mara said, \"Da ja-se vek.\"[^7] and left.\n\n[^7]: Mara: I see the *enemy*.");

        assertThat(tex).contains("\\textquotedbl{}\\footnote{Mara: I see the \\textit{enemy}.} and left.").doesNotContain("[7]");
    }

    private static int count(String text, String part) {
        int count = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            count++;
        }
        return count;
    }
}
