package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.TokenizerChoice;
import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.components.TokenizerProbe;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The tokenizers of plugins for OpenAI compatible AIs: registered by id, offered as a choice, used only by the AIs set to them,
 * and given the endpoint and the model but never the API key.
 */
class ExtensionTokenizerTest extends MarginaliaTestBase {

    @Autowired
    private TokenizerService tokenizerService;

    private String id;
    private RecordingTokenizer tokenizer;

    @BeforeEach
    void register() {
        id = uniqueName("tok");
        tokenizer = new RecordingTokenizer(id);
        tokenizerService.registerTokenizer(tokenizer);
    }

    @AfterEach
    void unregister() {
        tokenizerService.unregisterTokenizer(tokenizer);
    }

    private static OpenAICompatible ai(long id, String tokenizer) {
        OpenAICompatible ai = new OpenAICompatible();
        ai.setId(id);
        ai.setUri("http://llm.example:8080/v1");
        ai.setModel("model-a");
        ai.setApiKey("sk-very-secret");
        ai.setTokenizer(tokenizer);
        return ai;
    }

    @Test
    void choicesListTheBuiltInTokenizersThenThoseOfPlugins() {
        List<TokenizerChoice> choices = tokenizerService.getChoices();

        assertThat(choices).extracting(TokenizerChoice::id).contains("OPENAI_SDK", "LLAMA_CPP_NATIVE", "LITELLM_NATIVE", "LOCAL_TOKKIT");
        assertThat(choices.get(choices.size() - 1)).isEqualTo(new TokenizerChoice("ext:" + id, "Recording"));
        assertThat(choices.get(choices.size() - 1).isExtension()).isTrue();
    }

    @Test
    void sameIdCannotBeRegisteredTwice() {
        assertThatThrownBy(() -> tokenizerService.registerTokenizer(new RecordingTokenizer(id))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> tokenizerService.registerTokenizer(new RecordingTokenizer(" "))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aiSetToTheTokenizerCountsWithItAndOnlyGetsTheEndpointAndTheModel() throws Exception {
        assertThat(tokenizerService.countTokens(ai(9101L, "ext:" + id), "some text")).isEqualTo(777);

        assertThat(tokenizer.keys).containsExactly("http://llm.example:8080#model-a");
        assertThat(tokenizer.keys.toString()).doesNotContain("sk-very-secret");
    }

    @Test
    void aiNotSetToTheTokenizerNeverReachesIt() throws Exception {
        tokenizerService.countTokens(ai(9102L, "LOCAL_TOKKIT"), "some text");
        tokenizerService.probe(ai(9103L, "LOCAL_TOKKIT"), "some text");

        assertThat(tokenizer.keys).isEmpty();
    }

    @Test
    void aiSetToAnUnloadedTokenizerCountsLocally() throws Exception {
        long tokens = tokenizerService.countTokens(ai(9104L, "ext:gone"), "The quick brown fox jumps over the lazy dog.");

        assertThat(tokens).isEqualTo(tokenizerService.countTokensApprox("The quick brown fox jumps over the lazy dog."));
    }

    @Test
    void unregisteredTokenizerIsGoneFromTheChoicesAndTheCache() throws Exception {
        tokenizerService.countTokens(ai(9105L, "ext:" + id), "some text");
        tokenizerService.unregisterTokenizer(tokenizer);

        assertThat(tokenizerService.getChoices()).extracting(TokenizerChoice::id).doesNotContain("ext:" + id);
        long tokens = tokenizerService.countTokens(ai(9105L, "ext:" + id), "The quick brown fox jumps over the lazy dog.");
        assertThat(tokens).isEqualTo(tokenizerService.countTokensApprox("The quick brown fox jumps over the lazy dog."));
        assertThat(tokenizer.keys).hasSize(1);
    }

    @Test
    void suggestionIsTheTokenizerThatAcceptsTheEndpoint() {
        tokenizer.accepted = "llm.example";

        assertThat(tokenizerService.suggestChoice(ai(9106L, null))).isEqualTo("ext:" + id);

        OpenAICompatible other = ai(9107L, null);
        other.setUri("http://elsewhere:8080/v1");
        assertThat(tokenizerService.suggestChoice(other)).isNull();
    }

    @Test
    void tokenizerThatThrowsInAcceptsSuggestsNothing() {
        tokenizer.failAccepting = true;

        assertThat(tokenizerService.suggestChoice(ai(9108L, null))).isNull();
    }

    @Test
    void probeTellsWhatTheChosenTokenizerAnswered() {
        List<TokenizerProbe> probes = tokenizerService.probe(ai(9109L, "ext:" + id), "some text");

        assertThat(probes).containsExactly(new TokenizerProbe("ext:" + id, 777L, null));
    }

    @Test
    void probeTellsWhyATokenizerFailed() {
        tokenizer.fail = true;

        List<TokenizerProbe> probes = tokenizerService.probe(ai(9110L, "ext:" + id), "some text");

        assertThat(probes).singleElement().satisfies(probe -> {
            assertThat(probe.worked()).isFalse();
            assertThat(probe.error()).isEqualTo("boom");
        });
    }

    private static class RecordingTokenizer implements ExtensionTokenizer {
        private final String id;
        private final List<String> keys = new ArrayList<>();
        private String accepted;
        private boolean failAccepting;
        private boolean fail;

        private RecordingTokenizer(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return "Recording";
        }

        @Override
        public boolean accepts(TokenizerKey key) {
            if (failAccepting) {
                throw new IllegalStateException("boom");
            }
            return accepted != null && key.id().contains(accepted);
        }

        @Override
        public long countTokens(TokenizerKey key, String text) {
            if (fail) {
                throw new IllegalStateException("boom");
            }
            keys.add(key.id());
            return 777;
        }
    }
}
