package com.github.enerccio.marginalia.manual;

import com.github.enerccio.marginalia.domain.service.manual.ManualPage;
import com.github.enerccio.marginalia.domain.service.manual.ManualParser;
import com.github.enerccio.marginalia.domain.service.manual.ManualSection;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A page loses its front matter, pictures and admonition markers, links between pages become manual paths and the
 * page is split by its second and third level headings (never inside a code block).
 */
class ManualParserTest {

    private static final String PAGE = """
            ---
            label: Story editor
            order: 10
            ---

            # The story editor

            Intro with a [lorebook](../lorebooks/index.md#entries) link and the [dev docs](../../developer/x.md).

            ![A picture](../../images/x.png)

            !!!warning Careful
            Do not do this.
            !!!

            ## Writing

            Write things.

            ### Branches

            ```
            ## not a heading
            ```

            ## Summaries & more

            Last.
            """;

    @Test
    void splitsByHeadingsAndCleansTheText() {
        ManualPage page = ManualParser.parse("books/story-editor.md", PAGE);

        assertThat(page.title()).isEqualTo("The story editor");
        assertThat(page.description()).startsWith("Intro with a lorebook link and the dev docs.");
        assertThat(page.text()).doesNotContain("---", "order:", "![", "!!!", "x.png");
        assertThat(page.text()).contains("**Warning: Careful**", "(lorebooks/index.md#entries)", "## not a heading", "the dev docs.");
        assertThat(page.text()).doesNotContain("developer/");

        assertThat(page.sections()).extracting(ManualSection::heading)
                .containsExactly("The story editor", "Writing", "Writing > Branches", "Summaries & more");
        assertThat(page.sections()).extracting(ManualSection::anchor)
                .containsExactly("", "writing", "branches", "summaries--more");
        assertThat(page.sections().get(2).text()).contains("## not a heading");
        assertThat(page.sections().get(2).link()).isEqualTo("books/story-editor.md#branches");
        assertThat(page.sections().get(0).link()).isEqualTo("books/story-editor.md");
    }

    @Test
    void titleFallsBackToTheLabel() {
        ManualPage page = ManualParser.parse("x.md", "---\nlabel: Only label\n---\n\nText only.\n");

        assertThat(page.title()).isEqualTo("Only label");
        assertThat(page.sections()).hasSize(1);
    }
}
