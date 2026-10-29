package com.github.enerccio.marginalia.prompt;

import com.github.enerccio.marginalia.domain.components.PromptComparison;
import com.github.enerccio.marginalia.domain.components.PromptComparison.Status;
import com.github.enerccio.marginalia.domain.components.PromptComparison.Verdict;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Finds where two stored prompts first differ, the way a prefix cache sees them.
 */
class PromptComparisonTest {

    private static final Gson GSON = new Gson();
    private static final String LONG_PART = "Rain fell on the old harbour and the lamps went out one by one along the quay.";

    private static Map<String, Object> message(String role, String content) {
        return Map.of("role", role, "content", content, "attachments", List.of());
    }

    /** The payload of a story: the system message, a pair per part and the user message of the turn. */
    private static String prompt(String system, List<String> parts, String turn) {
        List<Map<String, Object>> payload = new ArrayList<>();
        payload.add(message("SYSTEM", system));
        for (int i = 0; i < parts.size(); i++) {
            payload.add(message("USER", i == 0 ? "[ Generate story. ]" : "[ Generate more story. ]"));
            payload.add(message("ASSISTANT", parts.get(i)));
        }
        payload.add(message("USER", turn));
        return GSON.toJson(payload);
    }

    @Test
    void identicalPrompts() {
        String p = prompt("sys", List.of(LONG_PART), "go");
        PromptComparison c = PromptComparison.compare(p, p);
        assertThat(c.getVerdict()).isEqualTo(Verdict.IDENTICAL);
        assertThat(c.getFirstDifference()).isEqualTo(-1);
        assertThat(c.getSharedShare()).isEqualTo(1.0);
        assertThat(c.getRows()).allMatch(r -> r.status() == Status.SAME && r.cacheKept());
    }

    @Test
    void nextPartDiffersAtTheLastUserMessageOfTheEarlierPrompt() {
        String earlier = prompt("sys", List.of(LONG_PART), "write the storm");
        String next = prompt("sys", List.of(LONG_PART, "The storm broke."), "write the morning");
        PromptComparison c = PromptComparison.compare(earlier, next);
        assertThat(c.getVerdict()).isEqualTo(Verdict.LAST_MESSAGE);
        // system, user, assistant are kept, the user message of the earlier turn is where it ends
        assertThat(c.getFirstDifference()).isEqualTo(3);
        assertThat(c.getRows().get(2).cacheKept()).isTrue();
        assertThat(c.getRows().get(3).status()).isEqualTo(Status.CHANGED);
        assertThat(c.getRows().get(3).cacheKept()).isFalse();
        assertThat(c.getRows().get(4).status()).isEqualTo(Status.ADDED);
        assertThat(c.getSharedChars()).isEqualTo("sys".length() + "[ Generate story. ]".length() + LONG_PART.length()
                + commonPrefix("write the storm", "[ Generate more story. ]"));
    }

    private static int commonPrefix(String a, String b) {
        int i = 0;
        while (i < Math.min(a.length(), b.length()) && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    @Test
    void changedSystemMessageLosesTheWholeStory() {
        String earlier = prompt("lore A", List.of(LONG_PART), "go");
        String next = prompt("lore B", List.of(LONG_PART), "go");
        PromptComparison c = PromptComparison.compare(earlier, next);
        assertThat(c.getVerdict()).isEqualTo(Verdict.SYSTEM_CHANGED);
        assertThat(c.getFirstDifference()).isZero();
        assertThat(c.getFirstDifferenceChar()).isEqualTo("lore ".length());
        assertThat(c.getSharedChars()).isEqualTo("lore ".length());
        assertThat(c.getSharedShare()).isLessThan(0.1);
        assertThat(c.getRows()).extracting(PromptComparison.Row::cacheKept).containsOnly(false);
    }

    @Test
    void storyWindowThatSlidIsRecognised() {
        String second = "The second part of the story is long enough to be recognised again.";
        String third = "The third part of the story is long enough to be recognised again.";
        String earlier = prompt("sys", List.of(LONG_PART, second), "go");
        String slid = prompt("sys", List.of(second, third), "go");
        PromptComparison c = PromptComparison.compare(earlier, slid);
        assertThat(c.getVerdict()).isEqualTo(Verdict.HISTORY_SHIFTED);
        assertThat(c.getFirstDifference()).isEqualTo(2);
    }

    @Test
    void editedPartInTheMiddleOfTheStory() {
        String second = "The second part of the story is long enough to be recognised again.";
        String earlier = prompt("sys", List.of(LONG_PART, second), "go");
        String edited = prompt("sys", List.of(LONG_PART + " (edited)", second), "go");
        PromptComparison c = PromptComparison.compare(earlier, edited);
        assertThat(c.getVerdict()).isEqualTo(Verdict.HISTORY_CHANGED);
        assertThat(c.getFirstDifference()).isEqualTo(2);
        assertThat(c.getFirstDifferenceChar()).isEqualTo(LONG_PART.length());
    }

    @Test
    void promptThatOnlyAddsMessages() {
        String earlier = GSON.toJson(List.of(message("SYSTEM", "sys"), message("USER", "go")));
        String longer = GSON.toJson(List.of(message("SYSTEM", "sys"), message("USER", "go"), message("ASSISTANT", "text")));
        assertThat(PromptComparison.compare(earlier, longer).getVerdict()).isEqualTo(Verdict.EXTENDED);
        assertThat(PromptComparison.compare(longer, earlier).getVerdict()).isEqualTo(Verdict.SHORTENED);
        assertThat(PromptComparison.compare(earlier, longer).getSharedShare()).isLessThan(1.0);
    }

    @Test
    void differentRolesAtTheDifference() {
        String a = GSON.toJson(List.of(message("SYSTEM", "sys"), message("USER", "go")));
        String b = GSON.toJson(List.of(message("SYSTEM", "sys"), message("ASSISTANT", "go")));
        assertThat(PromptComparison.compare(a, b).getVerdict()).isEqualTo(Verdict.ROLE_CHANGED);
    }

    @Test
    void promptStoredAsJsonStringIsRead() {
        String p = prompt("sys", List.of(LONG_PART), "go");
        PromptComparison c = PromptComparison.compare(GSON.toJson(p), p);
        assertThat(c).isNotNull();
        assertThat(c.getVerdict()).isEqualTo(Verdict.IDENTICAL);
    }

    @Test
    void unreadablePromptsGiveNull() {
        String p = prompt("sys", List.of(LONG_PART), "go");
        assertThat(PromptComparison.compare(null, p)).isNull();
        assertThat(PromptComparison.compare(" ", p)).isNull();
        assertThat(PromptComparison.compare("not json {", p)).isNull();
        assertThat(PromptComparison.compare("{\"a\":1}", p)).isNull();
        assertThat(PromptComparison.compare("[1, 2]", p)).isNull();
    }

    @Test
    void toolCallsAndAttachmentsCount() {
        Map<String, Object> plain = message("USER", "go");
        Map<String, Object> withTool = Map.of("role", "USER", "content", "go", "toolCallId", "call-1");
        PromptComparison c = PromptComparison.compare(GSON.toJson(List.of(plain)), GSON.toJson(List.of(withTool)));
        assertThat(c.getRows().get(0).status()).isEqualTo(Status.CHANGED);
        assertThat(c.getFirstDifferenceChar()).isEqualTo("go".length());
    }
}
