package com.github.enerccio.marginalia.manual;

import com.github.enerccio.marginalia.domain.service.impl.ManualServiceImpl;
import com.github.enerccio.marginalia.domain.service.manual.ManualHit;
import com.github.enerccio.marginalia.domain.service.manual.ManualPage;
import com.github.enerccio.marginalia.domain.service.manual.ManualToolsImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The manual packed from {@code manual/user} ({@code classpath:/manual/}) loads, its links between pages are
 * consistent and the search finds the page a question is about.
 */
class ManualServiceTest {

    private static ManualServiceImpl service;

    @BeforeAll
    static void load() {
        service = new ManualServiceImpl();
        service.load();
    }

    @Test
    void loadsThePagesWithoutPictures() {
        List<ManualPage> pages = service.listPages();

        assertThat(pages).hasSizeGreaterThan(30);
        assertThat(pages).allSatisfy(page -> {
            assertThat(page.title()).isNotBlank();
            assertThat(page.sections()).isNotEmpty();
            assertThat(page.text()).doesNotContain("![").doesNotContain("---\nlabel:");
        });
    }

    @Test
    void findsAPageByPathWithTolerance() {
        assertThat(service.getPage("books/story-editor.md")).isPresent();
        assertThat(service.getPage("/books/story-editor#images")).isPresent();
        assertThat(service.getPage("nope.md")).isEmpty();
        assertThat(service.getPage(null)).isEmpty();
    }

    @Test
    void linksBetweenPagesPointToExistingPages() {
        Set<String> paths = service.listPages().stream().map(ManualPage::path).collect(Collectors.toSet());
        var link = java.util.regex.Pattern.compile("]\\(([^)#:\\s]+\\.md)[)#]");

        for (ManualPage page : service.listPages()) {
            var m = link.matcher(page.text());
            while (m.find()) {
                assertThat(paths).as("link %s in %s", m.group(1), page.path()).contains(m.group(1));
            }
        }
    }

    @Test
    void searchRanksTheRelevantPageFirst() {
        List<ManualHit> hits = service.search("how do I install an extension", 5);

        assertThat(hits).isNotEmpty();
        assertThat(hits.stream().map(h -> h.section().pagePath()).limit(3))
                .anyMatch(path -> path.contains("extension"));
        assertThat(hits).isSortedAccordingTo((a, b) -> Double.compare(b.score(), a.score()));
    }

    @Test
    void searchWithoutUsableWordsFindsNothing() {
        assertThat(service.search("", 5)).isEmpty();
        assertThat(service.search("the of a", 5)).isEmpty();
        assertThat(service.search("zzzqqqxxx", 5)).isEmpty();
        assertThat(service.search(null, 5)).isEmpty();
        assertThat(service.search("lorebook", 0)).isEmpty();
    }

    @Test
    void addressesAreThoseOfThePublishedManual() {
        String base = "https://enerccio.github.io/Marginalia/user/";

        assertThat(ManualPage.url("books/story-editor.md", "")).isEqualTo(base + "books/story-editor/");
        assertThat(ManualPage.url("books/story-editor.md", "images")).isEqualTo(base + "books/story-editor/#images");
        assertThat(ManualPage.url("index.md", "")).isEqualTo(base);
        assertThat(ManualPage.url("administration/index.md", "")).isEqualTo(base + "administration/");
        assertThat(service.getPage("books/story-editor.md").orElseThrow().url()).isEqualTo(base + "books/story-editor/");
    }

    /** The generated site ({@code docs/}) has a page at the address of every page of the manual. */
    @Test
    void everyPageHasAPageInTheGeneratedSite() {
        java.nio.file.Path site = java.nio.file.Path.of("..", "docs", "user");
        org.junit.jupiter.api.Assumptions.assumeTrue(java.nio.file.Files.isDirectory(site), "docs/ is not there");

        for (ManualPage page : service.listPages()) {
            String relative = page.url().substring(ManualPage.WEB_BASE.length());
            assertThat(site.resolve(relative).resolve("index.html")).as("the site page of %s", page.path()).exists();
        }
    }

    @Test
    void theToolsGiveTheAddressOfWhatTheyFound() {
        ManualToolsImpl tools = new ManualToolsImpl(service);
        List<String> answers = new java.util.ArrayList<>();

        tools.readManualPage(recording(answers), "books/story-editor.md");
        tools.searchManual(recording(answers), "story editor images");

        assertThat(answers.get(0)).contains("Page: books/story-editor.md")
                .contains("URL: https://enerccio.github.io/Marginalia/user/books/story-editor/");
        assertThat(answers.get(1)).contains("URL: https://enerccio.github.io/Marginalia/user/");
    }

    private static com.github.enerccio.marginalia.domain.service.function.FunctionContinuation<String> recording(List<String> answers) {
        return new com.github.enerccio.marginalia.domain.service.function.FunctionContinuation<>() {
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
