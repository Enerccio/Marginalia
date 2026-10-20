package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.domain.service.InferenceService.RoundUsage;
import com.github.enerccio.marginalia.extensions.googlegenai.GoogleGenAiInferenceService.Answer;
import com.github.enerccio.marginalia.extensions.googlegenai.GoogleGenAiInferenceService.TokenUsage;
import com.google.genai.types.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What an answer of Google becomes as one round of an agent: the text without the thinking, the calls (not made here), the usage
 * with the cache and the thinking, and why it ended.
 */
class GoogleGenAiRoundTest {

    private static GenerateContentResponse response(String finish, GenerateContentResponseUsageMetadata usage, Part... parts) {
        GenerateContentResponse.Builder builder = GenerateContentResponse.builder()
                .candidates(List.of(Candidate.builder().content(Content.builder().role("model").parts(List.of(parts)).build())
                        .finishReason(finish).build()));
        if (usage != null) {
            builder.usageMetadata(usage);
        }
        return builder.build();
    }

    private static GenerateContentResponseUsageMetadata usage() {
        return GenerateContentResponseUsageMetadata.builder().promptTokenCount(1000).candidatesTokenCount(60).thoughtsTokenCount(40)
                .cachedContentTokenCount(800).build();
    }

    @Test
    void theCallsAndTheTextAreTakenAndTheThinkingIsKeptApart() {
        Answer answer = new Answer();
        var pieces = answer.absorb(response("STOP", usage(),
                Part.builder().text("Let me look.").thought(true).build(),
                Part.fromText("I will read the notes."),
                Part.builder().functionCall(FunctionCall.builder().name("read_file").id("c1").args(Map.of("path", "/notes.md")).build()).build()),
                true);

        assertEquals("I will read the notes.", answer.text.toString());
        assertEquals(List.of("Let me look."), pieces.stream().filter(piece -> piece.reasoning()).map(piece -> piece.text()).toList());
        assertEquals(1, answer.calls.size());
        assertEquals("c1", answer.calls.getFirst().getId());
        assertEquals("read_file", answer.calls.getFirst().getName());
        assertTrue(answer.calls.getFirst().getArguments().contains("/notes.md"));
    }

    @Test
    void theUsageHasTheCacheTheThinkingAndWhyTheAnswerEnded() {
        Answer answer = new Answer();
        answer.absorb(response("STOP", usage(), Part.builder().functionCall(FunctionCall.builder().name("list_files").args(Map.of()).build()).build()), true);

        RoundUsage round = GoogleGenAiInferenceService.roundUsage(answer.reported, answer, 4200);

        assertEquals(1000, round.promptTokens());
        assertEquals(100, round.completionTokens(), "the thinking is generated");
        assertEquals(800, round.stats().cachedTokens());
        assertEquals(40, round.stats().reasoningTokens());
        assertEquals("tool_calls", round.stats().finishReason());
        assertEquals(4200, round.stats().generationMillis());
        assertEquals(-1, round.stats().firstTokenMillis(), "the answer is not streamed");
    }

    @Test
    void anAnswerCutByTheLimitOfAnAnswerSaysLength() {
        Answer answer = new Answer();
        answer.absorb(response("MAX_TOKENS", usage(), Part.fromText("A long text that")), true);

        assertEquals("length", GoogleGenAiInferenceService.roundUsage(answer.reported, answer, 10).stats().finishReason());
    }

    @Test
    void whatGoogleDoesNotReportIsNotKnown() {
        Answer answer = new Answer();
        answer.absorb(response("STOP", null, Part.fromText("Done.")), true);

        RoundUsage round = GoogleGenAiInferenceService.roundUsage((TokenUsage) null, answer, 10);

        assertEquals(-1, round.stats().cachedTokens());
        assertEquals(-1, round.stats().reasoningTokens());
        assertEquals("stop", round.stats().finishReason());
    }
}
