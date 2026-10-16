package com.github.enerccio.marginalia.manual;

import com.github.enerccio.marginalia.domain.service.impl.ManualServiceImpl;
import com.github.enerccio.marginalia.domain.service.manual.ManualHit;
import com.github.enerccio.marginalia.domain.service.manual.ManualPage;
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
}
