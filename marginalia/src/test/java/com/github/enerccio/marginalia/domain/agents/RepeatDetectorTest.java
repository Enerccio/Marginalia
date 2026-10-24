package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.RepeatDetector;
import com.github.enerccio.marginalia.domain.service.agent.RepeatDetector.Verdict;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The detector says WARN at the third round that is the same as earlier ones (same calls, same results) and STOP at the fifth, among
 * the last fifteen rounds, so a cycle of two or three different rounds is caught too.
 */
class RepeatDetectorTest {

    private static Verdict round(RepeatDetector detector, String tool, String arguments, String result) {
        return detector.record(List.of(LLMToolCall.of("id", tool, arguments)), List.of(result));
    }

    @Test
    void theSameRoundIsToldAtTheThirdTimeAndStoppedAtTheFifth() {
        RepeatDetector detector = new RepeatDetector();

        assertThat(round(detector, "list_files", "{}", "a")).isEqualTo(Verdict.OK);
        assertThat(round(detector, "list_files", "{}", "a")).isEqualTo(Verdict.OK);
        assertThat(round(detector, "list_files", "{}", "a")).isEqualTo(Verdict.WARN);
        assertThat(round(detector, "list_files", "{}", "a")).isEqualTo(Verdict.OK);
        assertThat(round(detector, "list_files", "{}", "a")).isEqualTo(Verdict.STOP);
    }

    @Test
    void aDifferentResultIsProgress() {
        RepeatDetector detector = new RepeatDetector();

        for (int i = 0; i < 20; i++) {
            assertThat(round(detector, "list_files", "{}", "result " + i)).isEqualTo(Verdict.OK);
        }
    }

    @Test
    void differentArgumentsAreDifferentRounds() {
        RepeatDetector detector = new RepeatDetector();

        for (int i = 0; i < 20; i++) {
            assertThat(round(detector, "read_file", "{\"path\":\"/" + i + "\"}", "same text")).isEqualTo(Verdict.OK);
        }
    }

    @Test
    void aCycleOfTwoRoundsIsCaught() {
        RepeatDetector detector = new RepeatDetector();

        Verdict last = Verdict.OK;
        for (int i = 0; i < 10 && last != Verdict.STOP; i++) {
            last = round(detector, i % 2 == 0 ? "list_files" : "list-lorebooks", "{}", "same");
        }

        assertThat(last).isEqualTo(Verdict.STOP);
    }

    @Test
    void aCycleOfThreeRoundsIsCaught() {
        RepeatDetector detector = new RepeatDetector();
        String[] tools = {"list_files", "list-lorebooks", "list-lore-templates"};

        Verdict last = Verdict.OK;
        for (int i = 0; i < 15 && last != Verdict.STOP; i++) {
            last = round(detector, tools[i % 3], "{}", "same");
        }

        assertThat(last).isEqualTo(Verdict.STOP);
    }

    @Test
    void theResultsAreWhatCountsNotOnlyTheCalls() {
        RepeatDetector detector = new RepeatDetector();

        assertThat(round(detector, "list_files", "{}", "empty")).isEqualTo(Verdict.OK);
        assertThat(round(detector, "list_files", "{}", "empty")).isEqualTo(Verdict.OK);
        // somebody wrote a file: it is a new situation
        assertThat(round(detector, "list_files", "{}", "one file")).isEqualTo(Verdict.OK);
        assertThat(round(detector, "list_files", "{}", "one file")).isEqualTo(Verdict.OK);
    }

    @Test
    void resetForgetsTheRounds() {
        RepeatDetector detector = new RepeatDetector();
        round(detector, "list_files", "{}", "a");
        round(detector, "list_files", "{}", "a");

        detector.reset();

        assertThat(round(detector, "list_files", "{}", "a")).isEqualTo(Verdict.OK);
    }
}
