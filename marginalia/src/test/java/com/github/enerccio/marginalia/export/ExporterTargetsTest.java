package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.components.BookAuthorship;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportLoreEntry;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The export targets for other tools: HTML for Archive of Our Own, Markdown with front matter, the chat of SillyTavern, the Obsidian vault.
 */
class ExporterTargetsTest extends MarginaliaTestBase {

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private Exporter exporter(String id) {
        return exporterService.getExporters().stream().filter(e -> e.getId().equals(id)).findFirst().orElseThrow();
    }

    private static ChatMessage message(String instructions, String response) {
        ChatMessage message = new ChatMessage();
        message.setInstructions(instructions);
        message.setResponse(response);
        return message;
    }

    private static List<ChatMessage> story() {
        return List.of(
                message("Start with the dragon.", "# The Dragon\n\nElara met the dragon.\nIt was *red*.\n\n---\n\nShe left <b>quickly</b>."),
                message(null, "More text about Elara and the Red Keep, and \"quotes\"."),
                message("Next chapter.", "# The Keep\n\nThe Red Keep stood tall."));
    }

    private byte[] export(String id, ExportOptions options) throws Exception {
        return exporter(id).export(options, story(), new ProgressBarDialog(false));
    }

    private static Map<String, String> unzip(byte[] zip) throws Exception {
        Map<String, String> entries = new LinkedHashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                entries.put(entry.getName(), new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return entries;
    }

    @Test
    void theTargetsAreRegisteredWithTheirOwnIds() {
        assertThat(exporterService.getExporters()).extracting(Exporter::getId)
                .contains("ao3-html", "markdown-front-matter", "sillytavern", "obsidian").doesNotHaveDuplicates();
        assertThat(exporterService.getExporters().stream().filter(Exporter::usesLore).map(Exporter::getId)).containsExactly("obsidian");
    }

    @Test
    void frontMatterMarkdownStartsWithTheBlockInsteadOfATitlePage() throws Exception {
        String text = new String(export("markdown-front-matter", new ExportOptions("Tom \"Q\" & Jerry", "Some\nAuthor", true, "cs")), StandardCharsets.UTF_8);

        assertThat(text).startsWith("---\ntitle: \"Tom \\\"Q\\\" & Jerry\"\nauthor: \"Some Author\"\nlang: cs\ndate: ");
        assertThat(text).contains("\n---\n\n# The Dragon\n\nElara met the dragon.").doesNotContain("# Tom");
        assertThat(text.indexOf("\n---\n")).isLessThan(text.indexOf("# The Dragon"));
    }

    @Test
    void frontMatterTemplateIsChangeable() throws Exception {
        String template = "---\ntitle: \"{{title}}\"\nlayout: book\n---\n";
        String text = new String(export("markdown-front-matter",
                new ExportOptions("Book", "", true, "en", null, Map.of("frontMatter", template))), StandardCharsets.UTF_8);

        assertThat(text).startsWith("---\ntitle: \"Book\"\nlayout: book\n---\n\n# The Dragon");
    }

    @Test
    void ao3HasTheChapterTextBetweenCommentsWithoutTheTitleAndTheLineBreaks() throws Exception {
        String html = new String(export("ao3-html", new ExportOptions("Book", "Author", true, "en")), StandardCharsets.UTF_8);

        assertThat(html).contains("<h2>The Dragon</h2>\n<!-- BEGIN chapter 1 -->\n<p>Elara met the dragon. It was <em>red</em>.</p><hr />");
        assertThat(html).contains("&lt;b&gt;quickly&lt;/b&gt;").contains("&quot;quotes&quot;").contains("<!-- END chapter 2 -->");
        // the title is in the field of AO3, not in the pasted text, and the book has no title page
        String chapterOne = html.substring(html.indexOf("-->", html.indexOf("<!-- BEGIN chapter 1")) + 4, html.indexOf("<!-- END chapter 1") - 1);
        assertThat(chapterOne).doesNotContain("<h1>").doesNotContain("Book").doesNotContain("\n<p>").doesNotContain("</p>\n");
        assertThat(html).contains("<meta name=\"author\" content=\"Author\">").contains("<div id=\"workskin\">");
        assertThat(exporter("ao3-html").getTemplates()).containsExactly(ExporterBase.AUTHORSHIP_TEMPLATE);
    }

    @Test
    void theAuthorshipStatementEndsTheTextOfTheFormatsThatReadAndNothingElse() throws Exception {
        BookAuthorship authorship = new BookAuthorship(3, 2, 30, 70, List.of("model-a", "model-b"));

        String text = new String(export("markdown-front-matter", new ExportOptions("Book", "Author", true, "en").withAuthorship(authorship)),
                StandardCharsets.UTF_8);
        assertThat(text).endsWith("The Red Keep stood tall.\n\n## Authorship\n\nOf the 100 words of this text, 70 (70 %) were written by the author, "
                + "Author, and 30 (30 %) were generated by an AI model (model-a, model-b).\n");

        String plain = new String(export("markdown-front-matter", new ExportOptions("Book", "Author", true, "en")), StandardCharsets.UTF_8);
        assertThat(plain).doesNotContain("Authorship");

        String html = new String(export("ao3-html", new ExportOptions("Book", "Author", true, "en").withAuthorship(authorship)), StandardCharsets.UTF_8);
        assertThat(html).contains("<h2>Authorship</h2>").contains("70 (70 %) were written");

        assertThat(exporterService.getExporters().stream().filter(Exporter::usesAuthorship).map(Exporter::getId))
                .contains("markdown-front-matter", "ao3-html", "obsidian").doesNotContain("sillytavern");
    }

    @Test
    void theAuthorshipStatementIsATemplateTheUserCanChange() throws Exception {
        BookAuthorship authorship = new BookAuthorship(1, 1, 25, 75, List.of());

        String text = new String(export("markdown-front-matter", new ExportOptions("Book", "", true, "en", null,
                Map.of("authorship", "AI share: {{generatedPercent}} % of {{totalWords}}{{#models}} by {{.}}{{/models}}")).withAuthorship(authorship)),
                StandardCharsets.UTF_8);

        assertThat(text).endsWith("AI share: 25 % of 100\n");
    }

    @Test
    void sillyTavernChatHasOneJsonObjectPerLine() throws Exception {
        String text = new String(export("sillytavern", new ExportOptions("The Book", "Author", true, "en")), StandardCharsets.UTF_8);
        List<JsonObject> lines = text.lines().map(l -> JsonParser.parseString(l).getAsJsonObject()).toList();

        assertThat(lines).hasSize(1 + 2 + 1 + 2);
        assertThat(lines.getFirst().get("user_name").getAsString()).isEqualTo("Author");
        assertThat(lines.getFirst().get("character_name").getAsString()).isEqualTo("The Book");
        assertThat(lines.get(1).get("is_user").getAsBoolean()).isTrue();
        assertThat(lines.get(1).get("mes").getAsString()).isEqualTo("Start with the dragon.");
        assertThat(lines.get(2).get("is_user").getAsBoolean()).isFalse();
        assertThat(lines.get(2).get("name").getAsString()).isEqualTo("The Book");
        assertThat(lines.get(2).get("mes").getAsString()).startsWith("# The Dragon\n\nElara met the dragon.");
        assertThat(lines.get(3).get("mes").getAsString()).contains("\"quotes\"");
        assertThat(text).contains("\"quotes\"".replace("\"", "\\\"")).doesNotContain("\\u0027");
    }

    @Test
    void sillyTavernNamesFallBackWhenTheBookHasNoAuthor() throws Exception {
        String text = new String(export("sillytavern", new ExportOptions("The Book", " ", true, "en")), StandardCharsets.UTF_8);

        assertThat(JsonParser.parseString(text.lines().findFirst().orElseThrow()).getAsJsonObject().get("user_name").getAsString()).isEqualTo("User");
    }

    @Test
    void obsidianVaultHasNotesForTheChaptersAndTheLoreLinkedToEachOther() throws Exception {
        List<ExportLoreEntry> lore = List.of(
                new ExportLoreEntry("Characters", "Elara", "A knight who met the dragon at the Red Keep.", List.of("hero", "main character"), "Check her age"),
                new ExportLoreEntry("Places", "Red Keep", "The home of Elara.", List.of(), null),
                new ExportLoreEntry("Places", "Elara", "A river, not the knight.", List.of(), null));
        Map<String, String> vault = unzip(export("obsidian", new ExportOptions("Tom: Jerry", "Author", true, "en", null, null, lore)));

        assertThat(vault.keySet()).containsExactly(
                "Tom_ Jerry/Chapters/001 The Dragon.md", "Tom_ Jerry/Chapters/002 The Keep.md",
                "Tom_ Jerry/Lore/Characters/Elara.md", "Tom_ Jerry/Lore/Places/Red Keep.md", "Tom_ Jerry/Lore/Places/Elara.md",
                "Tom_ Jerry/Tom_ Jerry.md");

        String first = vault.get("Tom_ Jerry/Chapters/001 The Dragon.md");
        assertThat(first).startsWith("---\nbook: \"[[Tom_ Jerry]]\"\nchapter: 1\n");
        // only the first mention is a link, the first entry of the name wins
        assertThat(first).contains("[[Lore/Characters/Elara|Elara]] met the dragon.").doesNotContain("[[Lore/Places/Elara");
        assertThat(first.split("\\[\\[Lore/Characters/Elara", -1)).hasSize(2);
        assertThat(first).contains("[[Lore/Places/Red Keep|Red Keep]]").endsWith("[[Chapters/002 The Keep|Next]] →\n");
        assertThat(vault.get("Tom_ Jerry/Chapters/002 The Keep.md")).contains("← [[Chapters/001 The Dragon|Previous]] | [[Tom_ Jerry|Tom_ Jerry]]");

        String elara = vault.get("Tom_ Jerry/Lore/Characters/Elara.md");
        assertThat(elara).startsWith("---\nlorebook: \"Characters\"\ntags:\n  - hero\n  - main-character\n---\n");
        assertThat(elara).contains("[[Lore/Places/Red Keep|Red Keep]]").doesNotContain("[[Lore/Characters/Elara").endsWith("> [!note]\n> Check her age\n");

        String index = vault.get("Tom_ Jerry/Tom_ Jerry.md");
        assertThat(index).startsWith("---\ntitle: \"Tom: Jerry\"\nauthor: \"Author\"\nlang: en\n").contains("# Tom: Jerry\n\n*Author*");
        assertThat(index).contains("- [[Chapters/001 The Dragon|The Dragon]]").contains("### Characters\n\n- [[Lore/Characters/Elara|Elara]]")
                .contains("- [[Lore/Places/Elara|Elara]]");
    }

    @Test
    void obsidianVaultWithoutChaptersOrLoreIsOneNote() throws Exception {
        List<ChatMessage> plain = List.of(message(null, "Just text."));
        Map<String, String> vault = unzip(exporter("obsidian").export(new ExportOptions("Book", "", false, "en"), plain, new ProgressBarDialog(false)));

        assertThat(vault.keySet()).containsExactly("Book/Chapters/001 Story.md", "Book/Book.md");
        assertThat(vault.get("Book/Book.md")).contains("\n# Book\n").contains("- [[Chapters/001 Story|Story]]").doesNotContain("## Lore");
    }

    @Test
    void theLoreOfTheBookIsReadFromItsEnabledLorebooksAndEntries() throws Exception {
        login();
        Lorebook root = new Lorebook();
        root.setName("Root");
        root.setSubbooks(new ArrayList<>());
        root = lorebookService.save(root);
        Lorebook places = lorebookService.createSubbook(root, "Places");
        entry(root, "Elara", "A knight", true);
        entry(root, "Hidden", "A draft", false);
        entry(places, "Red Keep", "A castle", true);
        Manuscript book = new Manuscript();
        book.setName(uniqueName("book"));
        book.setLorebook(root);
        book = manuscriptService.save(book);

        List<ExportLoreEntry> lore = exporterService.collectLore(book);

        assertThat(lore).extracting(ExportLoreEntry::lorebook, ExportLoreEntry::name, ExportLoreEntry::content)
                .containsExactlyInAnyOrder(org.assertj.core.groups.Tuple.tuple("Root", "Elara", "A knight"),
                        org.assertj.core.groups.Tuple.tuple("Places", "Red Keep", "A castle"));

        Manuscript without = new Manuscript();
        without.setName(uniqueName("book"));
        assertThat(exporterService.collectLore(manuscriptService.save(without))).isEmpty();
    }

    private void entry(Lorebook lorebook, String name, String payload, boolean enabled) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload(payload);
        entry.setEnabled(enabled);
        lorebookEntryService.save(entry);
    }
}
