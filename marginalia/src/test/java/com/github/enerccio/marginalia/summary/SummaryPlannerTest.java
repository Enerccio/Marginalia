package com.github.enerccio.marginalia.summary;

import com.github.enerccio.marginalia.domain.components.SummaryPlanner;
import com.github.enerccio.marginalia.domain.components.SummaryPlanner.Mode;
import com.github.enerccio.marginalia.domain.components.SummaryPlanner.Options;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Where the summaries of a stretch of the story go: {@link SummaryPlanner}. Pure, no application.
 */
class SummaryPlannerTest {

    private static List<ChatMessage> story(long tokens, String... texts) {
        List<ChatMessage> parts = new ArrayList<>();
        long id = 1;
        for (String text : texts) {
            ChatMessage part = new ChatMessage();
            part.setId(id++);
            part.setResponse(text);
            part.setTokenCount(tokens);
            parts.add(part);
        }
        return parts;
    }

    private static List<Long> ids(List<ChatMessage> parts) {
        return parts.stream().map(ChatMessage::getId).toList();
    }

    @Test
    void tokensEndTheBlockAtThePartThatReachedTheTarget() {
        // 5 tokens and 1 for the separator: a part is 6, so 2 parts are 12 and reach 10, the block ends after the second
        List<ChatMessage> parts = story(5, "a", "b", "c", "d", "e", "f", "g");
        assertThat(ids(SummaryPlanner.plan(parts, new Options(Mode.TOKENS, 10, 0, false), 0))).containsExactly(2L, 4L, 6L);
        // the rest after the last cut has a summary only when asked for
        assertThat(ids(SummaryPlanner.plan(parts, new Options(Mode.TOKENS, 10, 0, true), 0))).containsExactly(2L, 4L, 6L, 7L);
    }

    @Test
    void partsEndTheBlockAfterTheNumberOfParts() {
        List<ChatMessage> parts = story(5, "a", "b", "c", "d", "e");
        assertThat(ids(SummaryPlanner.plan(parts, new Options(Mode.PARTS, 2, 0, false), 0))).containsExactly(2L, 4L);
        assertThat(ids(SummaryPlanner.plan(parts, new Options(Mode.PARTS, 2, 0, true), 0))).containsExactly(2L, 4L, 5L);
    }

    @Test
    void aChapterEndsTheBlockBeforeItsFirstPart() {
        // the chapters start at the parts 1, 3 and 5; the first one has nothing before it
        List<ChatMessage> parts = story(5, "# One\n\ntext", "more", "# Two\n\ntext", "more", "# Three\n\ntext", "more");
        assertThat(ids(SummaryPlanner.plan(parts, new Options(Mode.CHAPTERS, 0, 0, true), 0))).containsExactly(2L, 4L, 6L);
        // without the rest the last chapter has no summary
        assertThat(ids(SummaryPlanner.plan(parts, new Options(Mode.CHAPTERS, 0, 0, false), 0))).containsExactly(2L, 4L);
    }

    @Test
    void aShortChapterJoinsTheNextBlockAndTheMinimumDecidesWhat() {
        // the chapter of one part of 6 tokens is below the minimum of 10: it has no summary of its own
        List<ChatMessage> parts = story(5, "# One\n\ntext", "# Two\n\ntext", "more", "# Three\n\ntext", "more");
        assertThat(ids(SummaryPlanner.plan(parts, new Options(Mode.CHAPTERS, 0, 10, true), 0))).containsExactly(3L, 5L);
        // with no minimum every chapter gets one
        assertThat(ids(SummaryPlanner.plan(parts, new Options(Mode.CHAPTERS, 0, 0, true), 0))).containsExactly(1L, 3L, 5L);
        // a last chapter below the minimum is left without a summary too
        List<ChatMessage> shortEnd = story(5, "# One\n\ntext", "more", "# Two\n\ntext");
        assertThat(ids(SummaryPlanner.plan(shortEnd, new Options(Mode.CHAPTERS, 0, 10, true), 0))).containsExactly(2L);
    }

    @Test
    void theChapterLevelIsTheShallowestHeadingThatStartsAPart() {
        // the chapters are ##, the part that starts with ### is a section of a chapter
        List<ChatMessage> parts = story(5, "## One\n\ntext", "### Section\n\ntext", "## Two\n\ntext");
        assertThat(SummaryPlanner.chapterStarts(parts)).containsExactly(true, false, true);
        assertThat(SummaryPlanner.chapterStarts(story(5, "no heading", "text"))).containsExactly(false, false);
    }

    @Test
    void aBlockNeverGetsLongerThanTheModelCanRead() {
        List<ChatMessage> parts = story(5, "a", "b", "c", "d", "e", "f");
        // 6 a part, the limit 20 allows three parts; the target of 1000 would have made one block
        assertThat(ids(SummaryPlanner.plan(parts, new Options(Mode.TOKENS, 1000, 0, true), 20))).containsExactly(3L, 6L);
        // a part over the limit is a block of its own
        assertThat(ids(SummaryPlanner.plan(story(50, "a", "b"), new Options(Mode.PARTS, 5, 0, true), 20))).containsExactly(1L, 2L);
    }

    @Test
    void nothingToSummarizeGivesNoCuts() {
        assertThat(SummaryPlanner.plan(List.of(), new Options(Mode.PARTS, 1, 0, true), 0)).isEmpty();
        assertThatThrownBy(() -> new Options(Mode.PARTS, 0, 0, true)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Options(null, 1, 0, true)).isInstanceOf(IllegalArgumentException.class);
    }
}
