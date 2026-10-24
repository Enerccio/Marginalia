package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAICompatibleTokenizerResolverTest {

    private static TokenizerStrategy strategy(String name) {
        return new TokenizerStrategy() {
            @Override
            public long countTokens(com.github.enerccio.marginalia.domain.model.impl.AI ai, String text) {
                return 1;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private final TokenizerStrategy first = strategy("FIRST");
    private final TokenizerStrategy second = strategy("SECOND");
    private final TokenizerStrategy local = strategy("LOCAL");

    private final OpenAICompatibleTokenizerResolver resolver = new OpenAICompatibleTokenizerResolver(List.of(first, second), local,
            (id, key) -> strategy("ext:" + id + "@" + key.id()));

    private static OpenAICompatible ai(String uri, String model, String apiKey) {
        OpenAICompatible ai = new OpenAICompatible();
        ai.setUri(uri);
        ai.setModel(model);
        ai.setApiKey(apiKey);
        return ai;
    }

    @Test
    void endpointIsNormalized() {
        assertThat(OpenAICompatibleTokenizerResolver.normalizeEndpoint(" HTTP://Example.COM:8080/v1/ ")).isEqualTo("http://example.com:8080");
        assertThat(OpenAICompatibleTokenizerResolver.normalizeEndpoint("https://user:pass@host/api/v1?x=1#f")).isEqualTo("https://host/api");
        assertThat(OpenAICompatibleTokenizerResolver.normalizeEndpoint("not an address")).isEqualTo("not an address");
        assertThat(OpenAICompatibleTokenizerResolver.normalizeEndpoint(null)).isEmpty();
    }

    @Test
    void sameServerAndModelShareTheKeyWhateverTheSpelling() {
        assertThat(resolver.keyFor(ai("http://llm:8080/v1", "m", "k"))).isEqualTo(resolver.keyFor(ai("HTTP://LLM:8080/v1/", " m ", "k")));
    }

    @Test
    void anotherModelOnTheSameServerIsAnotherKey() {
        assertThat(resolver.keyFor(ai("http://llm:8080/v1", "a", "k"))).isNotEqualTo(resolver.keyFor(ai("http://llm:8080/v1", "b", "k")));
    }

    @Test
    void anotherServerOrApiKeyIsAnotherKey() {
        TokenizerKey key = resolver.keyFor(ai("http://llm:8080/v1", "m", "k"));

        assertThat(key).isNotEqualTo(resolver.keyFor(ai("http://other:8080/v1", "m", "k")));
        assertThat(key).isNotEqualTo(resolver.keyFor(ai("http://llm:8080/v1", "m", "other")));
    }

    @Test
    void apiKeyIsNotInTheId() {
        TokenizerKey key = resolver.keyFor(ai("http://llm:8080/v1", "m", "sk-very-secret"));

        assertThat(key.id()).isEqualTo("http://llm:8080#m").doesNotContain("sk-very-secret");
        assertThat(key.toString()).doesNotContain("sk-very-secret");
    }

    @Test
    void automaticTriesTheStrategiesInOrder() {
        OpenAICompatible ai = ai("http://llm:8080/v1", "m", "k");

        assertThat(resolver.chain(resolver.keyFor(ai), ai)).containsExactly(first, second);
        assertThat(resolver.builtInChoices()).containsExactly("FIRST", "SECOND", "LOCAL");
    }

    @Test
    void chosenStrategyIsTheOnlyOneTried() {
        OpenAICompatible ai = ai("http://llm:8080/v1", "m", "k");
        ai.setTokenizer("SECOND");

        assertThat(resolver.chain(resolver.keyFor(ai), ai)).containsExactly(second);
        ai.setTokenizer("LOCAL");
        assertThat(resolver.chain(resolver.keyFor(ai), ai)).containsExactly(local);
    }

    @Test
    void chosenTokenizerOfAPluginGetsTheKeyWithoutTheChoice() {
        OpenAICompatible ai = ai("http://llm:8080/v1", "m", "k");
        ai.setTokenizer("ext:plugin");

        assertThat(resolver.chain(resolver.keyFor(ai), ai)).extracting(TokenizerStrategy::getName).containsExactly("ext:plugin@http://llm:8080#m");
    }

    @Test
    void unknownChoiceIsUnavailable() {
        OpenAICompatible ai = ai("http://llm:8080/v1", "m", "k");
        ai.setTokenizer("GONE");

        assertThat(resolver.chain(resolver.keyFor(ai), ai)).singleElement().isInstanceOf(UnavailableTokenizerStrategy.class);
    }

    @Test
    void choiceIsPartOfTheKey() {
        OpenAICompatible automatic = ai("http://llm:8080/v1", "m", "k");
        OpenAICompatible chosen = ai("http://llm:8080/v1", "m", "k");
        chosen.setTokenizer("SECOND");

        assertThat(resolver.keyFor(automatic)).isNotEqualTo(resolver.keyFor(chosen));
        assertThat(resolver.keyFor(chosen).id()).isEqualTo("http://llm:8080#m|SECOND");
        assertThat(resolver.endpointKey(chosen).id()).isEqualTo("http://llm:8080#m");
    }
}
