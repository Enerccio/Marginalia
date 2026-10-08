package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.TokenLimits;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenLimitsTest {

    private static OpenAICompatible ai(Integer context, Integer response) {
        OpenAICompatible ai = new OpenAICompatible();
        ai.setMaxContext(context);
        ai.setMaxCompletionTokens(response);
        return ai;
    }

    private static Protocol protocol(Integer maxTokens, Integer replyTokens) {
        Protocol protocol = new ChatCompletionProtocol();
        protocol.setMaxTokens(maxTokens);
        protocol.setReplyTokens(replyTokens);
        return protocol;
    }

    @Test
    void protocolLimitsWin() {
        assertThat(TokenLimits.contextTokens(ai(32000, 1000), protocol(8000, 500))).isEqualTo(8000);
        assertThat(TokenLimits.responseTokens(ai(32000, 1000), protocol(8000, 500))).isEqualTo(500);
        assertThat(TokenLimits.promptTokens(ai(32000, 1000), protocol(8000, 500))).isEqualTo(7500);
    }

    @Test
    void missingProtocolLimitsFallBackToAi() {
        for (Protocol protocol : new Protocol[]{null, protocol(null, null), protocol(0, 0), protocol(-1, -1)}) {
            assertThat(TokenLimits.contextTokens(ai(32000, 1000), protocol)).isEqualTo(32000);
            assertThat(TokenLimits.responseTokens(ai(32000, 1000), protocol)).isEqualTo(1000);
            assertThat(TokenLimits.promptTokens(ai(32000, 1000), protocol)).isEqualTo(31000);
        }
    }

    @Test
    void limitsMixProtocolAndAi() {
        assertThat(TokenLimits.promptTokens(ai(32000, 1000), protocol(8000, null))).isEqualTo(7000);
        assertThat(TokenLimits.promptTokens(ai(32000, 1000), protocol(null, 4000))).isEqualTo(28000);
    }

    @Test
    void missingAiLimitsAreZero() {
        assertThat(TokenLimits.contextTokens(ai(null, null), null)).isZero();
        assertThat(TokenLimits.responseTokens(ai(null, null), null)).isZero();
        assertThat(TokenLimits.promptTokens(ai(100, 500), null)).as("never negative").isZero();
        assertThat(TokenLimits.promptTokens(null, null)).isZero();
    }
}
