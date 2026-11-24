package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.github.enerccio.marginalia.domain.service.InferenceService.RoundStats;
import com.github.enerccio.marginalia.domain.service.impl.inference.OpenAICompatibleInferenceService.RoundStatsCollector;
import com.openai.core.ObjectMappers;
import com.openai.models.chat.completions.ChatCompletionChunk;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a streamed answer says about how it went: the finish reason, the cache and the reasoning the usage reports, and the
 * {@code timings} llama.cpp adds to its last chunk.
 */
class RoundStatsCollectorTest {

    private static ChatCompletionChunk chunk(String json) throws Exception {
        return ObjectMappers.jsonMapper().readValue(json, ChatCompletionChunk.class);
    }

    @Test
    void theFinishReasonTheUsageAndTheTimingsOfLlamaCppAreRead() throws Exception {
        RoundStatsCollector collector = new RoundStatsCollector();
        collector.chunk(chunk("""
                {"id":"x","object":"chat.completion.chunk","created":1,"model":"m",
                 "choices":[{"index":0,"delta":{"content":"Hi"},"finish_reason":null}]}"""));
        collector.chunk(chunk("""
                {"id":"x","object":"chat.completion.chunk","created":1,"model":"m",
                 "choices":[{"index":0,"delta":{},"finish_reason":"length"}],
                 "usage":{"prompt_tokens":1000,"completion_tokens":200,"total_tokens":1200,
                          "prompt_tokens_details":{"cached_tokens":900},"completion_tokens_details":{"reasoning_tokens":120}},
                 "timings":{"cache_n":950,"prompt_n":50,"prompt_ms":123.4,"predicted_n":200,"predicted_ms":13400.7}}"""));

        RoundStats stats = collector.stats();

        assertThat(stats.finishReason()).isEqualTo("length");
        assertThat(stats.cachedTokens()).as("the larger of the usage and the cache of llama.cpp").isEqualTo(950);
        assertThat(stats.reasoningTokens()).isEqualTo(120);
        assertThat(stats.serverPromptMillis()).isEqualTo(123);
        assertThat(stats.serverPredictedMillis()).isEqualTo(13400);
        assertThat(stats.firstTokenMillis()).isNotNegative();
        assertThat(stats.generationMillis()).isNotNegative();
    }

    @Test
    void whatTheProviderDoesNotTellIsNotKnown() throws Exception {
        RoundStatsCollector collector = new RoundStatsCollector();
        collector.chunk(chunk("""
                {"id":"x","object":"chat.completion.chunk","created":1,"model":"m",
                 "choices":[{"index":0,"delta":{"content":"Hi"},"finish_reason":"stop"}]}"""));

        RoundStats stats = collector.stats();

        assertThat(stats.finishReason()).isEqualTo("stop");
        assertThat(stats.cachedTokens()).isEqualTo(-1);
        assertThat(stats.reasoningTokens()).isEqualTo(-1);
        assertThat(stats.serverPromptMillis()).isEqualTo(-1);
    }

    @Test
    void anAnswerThatNeverCameHasNoTimes() {
        RoundStats stats = new RoundStatsCollector().stats();

        assertThat(stats.firstTokenMillis()).isEqualTo(-1);
        assertThat(stats.generationMillis()).isEqualTo(-1);
        assertThat(stats.finishReason()).isNull();
    }
}
