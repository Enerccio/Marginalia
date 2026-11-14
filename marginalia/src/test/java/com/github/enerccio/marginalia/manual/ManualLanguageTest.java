package com.github.enerccio.marginalia.manual;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.impl.ManualServiceImpl;
import com.github.enerccio.marginalia.domain.service.manual.ManualHit;
import com.github.enerccio.marginalia.domain.service.manual.ManualPage;
import com.github.enerccio.marginalia.domain.service.manual.ManualParser;
import com.github.enerccio.marginalia.domain.service.manual.ManualToolsImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The manual is read in the language of the user: the translations packed from {@code translations/<language>/user}
 * ({@code classpath:/manual-translations/}), English for what is not translated, and the address of a page is that of the site of the
 * language of its text.
 */
class ManualLanguageTest {

    private static final String SITE = "https://enerccio.github.io/Marginalia/";

    private static ManualServiceImpl service;

    @BeforeAll
    static void load() {
        service = new ManualServiceImpl();
        service.load();
    }

    @Test
    void aTranslatedPageIsInItsLanguageAndHasTheAddressOfItsSite() {
        ManualPage english = service.getPage("books/story-editor.md").orElseThrow();
        ManualPage spanish = service.getPage("es", "books/story-editor.md").orElseThrow();

        assumeTrue(!spanish.language().equals("en"), "the manual is not translated into Spanish");
        assertThat(spanish.language()).isEqualTo("es");
        assertThat(spanish.path()).isEqualTo(english.path());
        assertThat(spanish.text()).isNotEqualTo(english.text());
        assertThat(spanish.url()).isEqualTo(SITE + "es/user/books/story-editor/");
        assertThat(english.url()).isEqualTo(SITE + "user/books/story-editor/");
        assertThat(spanish.sections()).allSatisfy(section -> assertThat(section.language()).isEqualTo("es"));
    }

    @Test
    void aLanguageWithoutTranslationsGetsEnglishPagesAndTheEnglishSite() {
        ManualPage page = service.getPage("xx", "books/story-editor.md").orElseThrow();

        assertThat(page).isEqualTo(service.getPage("books/story-editor.md").orElseThrow());
        assertThat(page.language()).isEqualTo("en");
        assertThat(page.url()).isEqualTo(SITE + "user/books/story-editor/");
        assertThat(service.listPages("xx")).isEqualTo(service.listPages());
        assertThat(service.getPage("xx", "nope.md")).isEmpty();
    }

    @Test
    void aLanguageCodeIsNotATraversalOfTheClasspath() {
        for (String code : new String[]{"../manual", "es/../en", "", "  ", "ES/user", "*", null}) {
            assertThat(service.getPage(code, "books/story-editor.md").orElseThrow().language()).as("code %s", code).isEqualTo("en");
        }
        assertThat(service.getPage(" ES ", "books/story-editor.md")).isPresent();
    }

    @Test
    void theListHasEveryPageOnceAndTheTranslatedOnesInTheLanguage() {
        List<ManualPage> english = service.listPages();
        List<ManualPage> spanish = service.listPages("es");

        assertThat(spanish).extracting(ManualPage::path).containsExactlyElementsOf(english.stream().map(ManualPage::path).toList());
        assertThat(spanish).extracting(ManualPage::language).contains("es");
    }

    @Test
    void theSearchFindsInEnglishAndGivesTheSectionsOfTheTranslation() {
        List<ManualHit> english = service.search("how do I install an extension", 5);
        List<ManualHit> spanish = service.search("es", "how do I install an extension", 5);

        assertThat(spanish).extracting(hit -> hit.section().pagePath()).containsExactlyElementsOf(
                english.stream().map(hit -> hit.section().pagePath()).toList());
        assertThat(spanish).extracting(ManualHit::score).containsExactlyElementsOf(english.stream().map(ManualHit::score).toList());
        assertThat(spanish).anySatisfy(hit -> {
            assertThat(hit.section().language()).isEqualTo("es");
            assertThat(hit.section().url()).startsWith(SITE + "es/user/");
        });
    }

    @Test
    void theToolsReadAndLinkInTheLanguageOfTheUser() {
        List<String> answers = new ArrayList<>();
        ManualToolsImpl tools = new ManualToolsImpl(service, "es");

        tools.readManualPage(recording(answers), "books/story-editor.md");
        tools.searchManual(recording(answers), "story editor images");

        assumeTrue(service.getPage("es", "books/story-editor.md").orElseThrow().language().equals("es"));
        assertThat(answers.get(0)).contains("Page: books/story-editor.md").contains("URL: " + SITE + "es/user/books/story-editor/");
        assertThat(answers.get(1)).contains("URL: " + SITE + "es/user/");
        // the tools made without a language are English, as the agents' always were
        List<String> plain = new ArrayList<>();
        new ManualToolsImpl(service).readManualPage(recording(plain), "books/story-editor.md");
        assertThat(plain.get(0)).contains("URL: " + SITE + "user/books/story-editor/");
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "fr", "de", "it", "ja", "ko", "zh"})
    void everyPageIsTranslatedAndKeepsTheStructureOfTheEnglishPage(String language) {
        List<ManualPage> english = service.listPages();
        List<ManualPage> translated = service.listPages(language);

        assertThat(translated).extracting(ManualPage::path).containsExactlyElementsOf(english.stream().map(ManualPage::path).toList());
        for (int i = 0; i < english.size(); i++) {
            ManualPage page = translated.get(i);
            assertThat(page.language()).as("%s of %s", page.path(), language).isEqualTo(language);
            assertThat(page.url()).startsWith(SITE + language + "/user/");
            // the search gives the section of the translation at the place of the English one: the headings are the same
            assertThat(page.sections()).as("the sections of %s in %s", page.path(), language).hasSameSizeAs(english.get(i).sections());
        }
    }

    /**
     * What a translation must not change (tools/manual_i18n.py takes it out of the text before the request and puts it back, and
     * {@code verify} checks it): the code blocks and the inline code of the English page are in its translation as they are.
     */
    @ParameterizedTest
    @ValueSource(strings = {"es", "fr", "de", "it", "ja", "ko", "zh"})
    void aTranslationKeepsTheCodeOfTheEnglishPage(String language) {
        for (ManualPage english : service.listPages()) {
            ManualPage translated = service.getPage(language, english.path()).orElseThrow();

            assertThat(codeBlocks(translated.text())).as("the code blocks of %s in %s", english.path(), language)
                    .containsExactlyElementsOf(codeBlocks(english.text()));
            java.util.Map<String, Long> wanted = inlineCode(english.text());
            java.util.Map<String, Long> had = inlineCode(translated.text());
            wanted.forEach((code, count) -> assertThat(had.getOrDefault(code, 0L))
                    .as("`%s` in %s (%s)", code, english.path(), language).isGreaterThanOrEqualTo(count));
        }
    }

    /** The fenced blocks of a text, whole, in order. */
    private static List<String> codeBlocks(String text) {
        List<String> blocks = new ArrayList<>();
        StringBuilder current = null;
        String fence = null;
        for (String line : text.split("\n", -1)) {
            String trimmed = line.stripLeading();
            String mark = trimmed.startsWith("```") ? "```" : trimmed.startsWith("~~~") ? "~~~" : null;
            if (fence == null) {
                if (mark != null) {
                    fence = mark;
                    current = new StringBuilder(line);
                }
            } else {
                current.append('\n').append(line);
                if (mark != null && mark.equals(fence)) {
                    blocks.add(current.toString());
                    fence = null;
                }
            }
        }
        return blocks;
    }

    private static final java.util.regex.Pattern INLINE_CODE =
            java.util.regex.Pattern.compile("(?<!`)(`+)(?!`)(.+?)(?<!`)\\1(?!`)", java.util.regex.Pattern.DOTALL);

    /** Whether a backtick is left over when the spans of a piece of text are taken out: a span that is not closed. */
    private static boolean dangling(String piece) {
        return INLINE_CODE.matcher(piece).replaceAll("").contains("`");
    }

    /**
     * The inline code spans outside the fenced blocks, with how many times each is there, found as tools/manual_i18n.py does: line by line,
     * and a span that runs over the end of a line (CommonMark allows it) takes the next lines of its paragraph.
     */
    private static java.util.Map<String, Long> inlineCode(String text) {
        String outside = text;
        for (String block : codeBlocks(text)) {
            outside = outside.replace(block, "");
        }
        String[] lines = outside.split("\\n", -1);
        java.util.Map<String, Long> spans = new java.util.HashMap<>();
        int i = 0;
        while (i < lines.length) {
            String piece = lines[i];
            int used = 1;
            if (dangling(piece)) {
                for (int k = 1; k < 5 && i + k < lines.length && !lines[i + k].isBlank(); k++) {
                    String candidate = String.join("\n", java.util.Arrays.copyOfRange(lines, i, i + k + 1));
                    if (!dangling(candidate)) {
                        piece = candidate;
                        used = k + 1;
                        break;
                    }
                }
            }
            var span = INLINE_CODE.matcher(piece);
            while (span.find()) {
                spans.merge(span.group(), 1L, Long::sum);
            }
            i += used;
        }
        return spans;
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "fr", "de", "it", "ja", "ko", "zh"})
    void theLinksOfTheTranslatedPagesPointToExistingPages(String language) {
        Set<String> paths = service.listPages().stream().map(ManualPage::path).collect(Collectors.toSet());
        var link = java.util.regex.Pattern.compile("]\\(([^)#:\\s]+\\.md)[)#]");

        for (ManualPage page : service.listPages(language)) {
            var m = link.matcher(page.text());
            while (m.find()) {
                assertThat(paths).as("link %s in %s (%s)", m.group(1), page.path(), language).contains(m.group(1));
            }
        }
    }

    @Test
    void aHeadingKeepsTheEnglishIdThatTheTranslationToolWritesBeforeIt() {
        ManualPage page = ManualParser.parse("es", "books/x.md", """
                ---
                label: Equis
                ---

                # Equis

                Introducción.

                <a id="images"></a>
                ## Imágenes

                Texto de las imágenes.

                ## Otra cosa

                Más texto.
                """);

        assertThat(page.sections()).extracting(section -> section.anchor()).containsExactly("", "images", "otra-cosa");
        assertThat(page.sections().get(1).url()).isEqualTo(SITE + "es/user/books/x/#images");
        assertThat(page.text()).doesNotContain("<a id=");
    }

    /** The generated sites ({@code docs/<language>/}) have a page at the address of every translated page. */
    @ParameterizedTest
    @ValueSource(strings = {"es", "fr", "de", "it", "ja", "ko", "zh"})
    void everyTranslatedPageHasAPageInTheGeneratedSite(String language) {
        Path site = Path.of("..", "docs", language, "user");
        assumeTrue(Files.isDirectory(site), "docs/" + language + " is not there");

        for (ManualPage page : service.listPages(language)) {
            String relative = page.url().substring((SITE + language + "/user/").length());
            assertThat(site.resolve(relative).resolve("index.html")).as("the site page of %s", page.path()).exists();
        }
    }

    private static FunctionContinuation<String> recording(List<String> answers) {
        return new FunctionContinuation<>() {
            @Override
            public void complete(String result) {
                answers.add(result);
            }

            @Override
            public void fail(String message) {
                throw new AssertionError(message);
            }

            @Override
            public void fail(Throwable error) {
                throw new AssertionError(error);
            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        };
    }
}
