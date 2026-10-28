package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.components.TokenizerChoice;
import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.TokenizerResolver;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * The tokenizers of an OpenAI compatible server: the endpoint and the model say what is counted against (one server can
 * serve several models), the API key tells who asks. Unless the AI is set to one tokenizer, the strategies are tried in order
 * until one answers.
 */
public class OpenAICompatibleTokenizerResolver implements TokenizerResolver {

    private final List<TokenizerStrategy> strategies;
    private final TokenizerStrategy local;
    private final BiFunction<String, TokenizerKey, TokenizerStrategy> extensionTokenizers;

    /**
     * @param strategies          tried in order when the AI is set to automatic
     * @param local               the estimate made in this process, the choice that never calls a server
     * @param extensionTokenizers the tokenizer of a plugin by its id, for the key of the AI (an unavailable strategy when the
     *                            plugin is not loaded)
     */
    public OpenAICompatibleTokenizerResolver(List<TokenizerStrategy> strategies, TokenizerStrategy local,
                                             BiFunction<String, TokenizerKey, TokenizerStrategy> extensionTokenizers) {
        this.strategies = strategies;
        this.local = local;
        this.extensionTokenizers = extensionTokenizers;
    }

    /**
     * The names of the built-in choices.
     */
    public List<String> builtInChoices() {
        return Stream.concat(strategies.stream(), Stream.of(local)).map(TokenizerStrategy::getName).toList();
    }

    @Override
    public TokenizerKey keyFor(AI ai) {
        OpenAICompatible openAi = (OpenAICompatible) ai;
        String choice = StringUtils.trimToNull(openAi.getTokenizer());
        return TokenizerKey.of(endpointAndModel(openAi) + (choice == null ? "" : "|" + choice), openAi.getApiKey());
    }

    /**
     * The key of the endpoint and the model, as the tokenizers of plugins get it: without the choice of the AI and the credentials.
     */
    public TokenizerKey endpointKey(AI ai) {
        return TokenizerKey.of(endpointAndModel((OpenAICompatible) ai));
    }

    @Override
    public List<TokenizerStrategy> chain(TokenizerKey key, AI ai) {
        String choice = StringUtils.trimToNull(((OpenAICompatible) ai).getTokenizer());
        if (choice == null) {
            return strategies;
        }
        if (choice.startsWith(TokenizerChoice.EXTENSION_PREFIX)) {
            return List.of(extensionTokenizers.apply(choice.substring(TokenizerChoice.EXTENSION_PREFIX.length()), endpointKey(ai)));
        }
        return List.of(Stream.concat(strategies.stream(), Stream.of(local))
                .filter(strategy -> strategy.getName().equals(choice)).findFirst()
                .orElseGet(() -> new UnavailableTokenizerStrategy(choice)));
    }

    private static String endpointAndModel(OpenAICompatible ai) {
        return normalizeEndpoint(ai.getUri()) + "#" + StringUtils.trimToEmpty(ai.getModel());
    }

    /**
     * The endpoint without what does not tell the server apart: the credentials in the address, the query, the fragment, the
     * {@code /v1} and the slashes at the end; the scheme and the host in lower case.
     */
    static String normalizeEndpoint(String uri) {
        String trimmed = StringUtils.trimToEmpty(uri);
        try {
            URI parsed = URI.create(trimmed);
            if (parsed.getHost() != null) {
                StringBuilder endpoint = new StringBuilder();
                endpoint.append(StringUtils.defaultString(parsed.getScheme(), "http").toLowerCase(Locale.ROOT)).append("://")
                        .append(parsed.getHost().toLowerCase(Locale.ROOT));
                if (parsed.getPort() >= 0) {
                    endpoint.append(':').append(parsed.getPort());
                }
                endpoint.append(parsed.getPath() == null ? "" : parsed.getPath());
                return stripPath(endpoint.toString());
            }
        } catch (IllegalArgumentException ignored) {
            // not an address: the text itself tells the AIs apart
        }
        return stripPath(trimmed.toLowerCase(Locale.ROOT));
    }

    private static String stripPath(String endpoint) {
        return endpoint.replaceAll("/v1/?$", "").replaceAll("/+$", "");
    }
}
