package com.github.enerccio.marginalia.utils.docimport;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The cutting of a document into parts: chapters are kept apart, a chapter is cut between paragraphs near the target and
 * what would be left over at its end (less than half of the target) stays with the part before it.
 */
class PartSplitterTest {

    private static TextBlock paragraph(int words) {
        return new TextBlock(0, "w ".repeat(words).trim());
    }

    private static TextBlock heading(int level, String text) {
        return new TextBlock(level, text);
    }

    private static TextDocument document(TextBlock... blocks) {
        return new TextDocument(null, List.of(blocks));
    }

    private static List<Integer> sizes(PartSplitter.Result result) {
        return result.parts().stream().map(TextBlock::words).toList();
    }

    @Test
    void chaptersAreNeverJoinedAndStartWithTheirHeading() {
        PartSplitter.Result result = PartSplitter.split(document(heading(1, "One"), paragraph(150), heading(1, "Two"), paragraph(120)), 1, 1000);

        assertThat(result.chapters()).isEqualTo(2);
        assertThat(result.parts()).hasSize(2);
        assertThat(result.parts().get(0)).startsWith("# One\n\n");
        assertThat(result.parts().get(1)).startsWith("# Two\n\n");
    }

    @Test
    void aChapterIsCutBetweenParagraphsAtTheTarget() {
        List<TextBlock> blocks = new ArrayList<>(List.of(heading(1, "Long")));
        for (int i = 0; i < 40; i++) {
            blocks.add(paragraph(100));
        }
        PartSplitter.Result result = PartSplitter.split(new TextDocument(null, blocks), 1, 1000);

        // 4001 words in 100 word paragraphs: parts of about 1000 words, the heading counts too
        assertThat(result.parts()).hasSize(4);
        assertThat(result.parts().get(0)).startsWith("# Long\n\n");
        assertThat(result.parts().subList(1, 4)).noneMatch(part -> part.startsWith("#"));
        assertThat(sizes(result)).allSatisfy(size -> assertThat(size).isBetween(900, 1100));
        assertThat(result.words()).isEqualTo(4001);
    }

    @Test
    void aShortEndOfAChapterStaysWithThePartBeforeIt() {
        // 1000 + 400: the 400 left are less than half of the target, so the chapter stays one part
        PartSplitter.Result result = PartSplitter.split(document(paragraph(500), paragraph(500), paragraph(400)), 1, 1000);
        assertThat(sizes(result)).containsExactly(1400);

        // 1000 + 600: half or more is left, it is a part of its own
        result = PartSplitter.split(document(paragraph(500), paragraph(500), paragraph(300), paragraph(300)), 1, 1000);
        assertThat(sizes(result)).containsExactly(1000, 600);
    }

    @Test
    void aChapterUpToOneAndAHalfTimesTheTargetIsOnePart() {
        assertThat(sizes(PartSplitter.split(document(paragraph(700), paragraph(700)), 1, 1000))).containsExactly(1400);
        assertThat(sizes(PartSplitter.split(document(paragraph(800), paragraph(800)), 1, 1000))).containsExactly(800, 800);
    }

    @Test
    void aShortChapterIsNotJoinedWithTheNextOne() {
        PartSplitter.Result result = PartSplitter.split(document(heading(1, "A"), paragraph(20), heading(1, "B"), paragraph(20),
                heading(1, "C"), paragraph(20)), 1, 1000);

        assertThat(result.parts()).hasSize(3);
    }

    @Test
    void deeperHeadingsStayInTheText() {
        PartSplitter.Result result = PartSplitter.split(document(heading(1, "Part"), heading(2, "Chapter"), paragraph(10),
                heading(3, "Scene"), paragraph(10)), 2, 1000);

        // the headings above the chapter level start a chapter too (an empty one), the deeper ones are headings below the chapter's
        assertThat(result.chapters()).isEqualTo(2);
        assertThat(result.parts().get(0)).isEqualTo("# Part");
        assertThat(result.parts().get(1)).startsWith("# Chapter\n\n").contains("\n\n## Scene\n\n");
    }

    @Test
    void textBeforeTheFirstHeadingIsAChapterWithoutATitle() {
        PartSplitter.Result result = PartSplitter.split(document(paragraph(30), heading(1, "One"), paragraph(30)), 1, 1000);

        assertThat(result.chapters()).isEqualTo(2);
        assertThat(result.parts().get(0)).doesNotStartWith("#");
    }

    @Test
    void aParagraphLongerThanTheTargetIsNotCut() {
        // the 100 words before it are nearer the target than the 3100 with it; the 100 after it are less than half the target, they stay
        PartSplitter.Result result = PartSplitter.split(document(paragraph(100), paragraph(3000), paragraph(100)), 1, 1000);

        assertThat(sizes(result)).containsExactly(100, 3100);
    }

    @Test
    void theTargetIsChecked() {
        assertThatThrownBy(() -> PartSplitter.split(document(paragraph(10)), 1, 50)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PartSplitter.split(document(paragraph(10)), 1, 100_000)).isInstanceOf(IllegalArgumentException.class);
    }
}
