package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.TokenizerChoice;
import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.components.TokenizerProbe;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.ExtensionTokenizer;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.TokenizerResolver;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.*;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenizerServiceImpl implements TokenizerService {
    private static final Logger log = LoggerFactory.getLogger(TokenizerServiceImpl.class);

    /**
     * The key of the cache: whose resolver made the key (the id of the extension, empty for the application) so that two
     * providers can't take over the strategies of each other.
     */
    private record Owned(String owner, TokenizerKey key) {
    }

    /**
     * The resolver an extension provides, with the factory it came from (to tell when the extension was reloaded).
     */
    private record Provided(ExtensionAIFactory factory, TokenizerResolver resolver) {
    }

    // Global in-memory cache: what the AI counts against (TokenizerKey) -> Validated Working Strategy
    private final Map<Owned, TokenizerStrategy> strategyCache = new ConcurrentHashMap<>();

    // The key an AI was counted with last, to forget its strategy when the AI changes
    private final Map<Long, Owned> keysOfAIs = new ConcurrentHashMap<>();

    // The resolvers of the loaded extensions by their id
    private final Map<String, Provided> extensionResolvers = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private InferenceServices inferenceServices;

    // Reusable strategy singletons
    private final TokenizerStrategy tokkit = new JavaTokkitStrategy();

    // The tokenizers of the loaded extensions by their id
    private final Map<String, ExtensionTokenizer> extensionTokenizers = new ConcurrentHashMap<>();

    private final OpenAICompatibleTokenizerResolver openAiCompatible = new OpenAICompatibleTokenizerResolver(List.of(
            new OpenAiSdkTokenizerStrategy(),
            new LlamaCppTokenizerStrategy(),
            new LiteLlmTokenizerStrategy(),
            new LiteLlmAnthropicTokenizerStrategy()), tokkit, this::extensionStrategy);

    private final TokenizerResolver local = new LocalTokenizerResolver(tokkit);

    private TokenizerStrategy extensionStrategy(String id, TokenizerKey key) {
        ExtensionTokenizer tokenizer = extensionTokenizers.get(id);
        return tokenizer == null
                ? new UnavailableTokenizerStrategy(TokenizerChoice.EXTENSION_PREFIX + id)
                : new ExtensionTokenizerStrategy(tokenizer, key);
    }

    /**
     * The resolver of the kind of the AI, which says by what the AI is counted and what to try.
     */
    protected TokenizerResolver resolverFor(AI ai) {
        if (ai instanceof ExtensionAI extensionAI) {
            TokenizerResolver provided = extensionResolver(extensionAI.getExtensionId());
            return provided == null ? local : provided;
        }
        return ai instanceof OpenAICompatible ? openAiCompatible : local;
    }

    /**
     * The resolver of the extension, null when it is not loaded or counts by itself. A reloaded extension has another factory:
     * what was found for the old one is forgotten.
     */
    private TokenizerResolver extensionResolver(String extensionId) {
        ExtensionAIFactory factory = inferenceServices.getExtensionFactory(extensionId);
        Provided known = extensionResolvers.get(extensionId);
        if (known != null && known.factory() == factory) {
            return known.resolver();
        }
        if (known != null) {
            extensionResolvers.remove(extensionId, known);
            strategyCache.keySet().removeIf(owned -> owned.owner().equals(extensionId));
            keysOfAIs.values().removeIf(owned -> owned.owner().equals(extensionId));
        }
        TokenizerResolver resolver = factory == null ? null : factory.createTokenizerResolver();
        if (resolver != null) {
            extensionResolvers.put(extensionId, new Provided(factory, resolver));
        }
        return resolver;
    }

    private static String ownerOf(AI ai) {
        return ai instanceof ExtensionAI extensionAI && extensionAI.getExtensionId() != null ? extensionAI.getExtensionId() : "";
    }

    @Override
    public long countTokens(AI ai, String text) throws Exception {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // a read-write transaction holds the database write lock, and the strategies may call the model server, which can be
        // busy or loading the model first: everybody else who writes would fail with SQLITE_BUSY meanwhile
        if (TransactionSynchronizationManager.isActualTransactionActive() && !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            log.debug("Counting the tokens of AI ID {} approximately, a write transaction is active", ai == null ? null : ai.getId());
            return countTokensApprox(text);
        }

        TokenizerResolver resolver = resolverFor(ai);
        TokenizerKey tokenizerKey = resolver.keyFor(ai);
        Owned key = new Owned(ownerOf(ai), tokenizerKey);
        if (ai != null && ai.getId() != null) {
            keysOfAIs.put(ai.getId(), key);
        }
        TokenizerStrategy cachedStrategy = strategyCache.get(key);

        // 1. Fast Path: Executing previously validated strategy
        if (cachedStrategy != null) {
            try {
                return cachedStrategy.countTokens(ai, text);
            } catch (Exception e) {
                log.warn("Cached strategy '{}' failed for {}, invalidating...", cachedStrategy.getName(), tokenizerKey);
                strategyCache.remove(key);
            }
        }

        // 2. Discovery Path: Probe candidates in order
        for (TokenizerStrategy strategy : resolver.chain(tokenizerKey, ai)) {
            try {
                long count = strategy.countTokens(ai, text);
                log.info("Validated tokenizer strategy '{}' for {}", strategy.getName(), tokenizerKey);

                strategyCache.put(key, strategy);
                return count;
            } catch (Exception e) {
                log.debug("Strategy '{}' unviable for {}: {}", strategy.getName(), tokenizerKey, e.getMessage());
            }
        }

        return tokkit.countTokens(ai, text);
    }

    @Override
    public long countTokensApprox(String text) throws Exception {
        return tokkit.countTokens(null, text);
    }

    @Override
    public void invalidateCache(Long aiId) {
        if (aiId != null) {
            Owned key = keysOfAIs.remove(aiId);
            if (key != null) {
                strategyCache.remove(key);
            }
        }
    }

    @Override
    public void registerTokenizer(ExtensionTokenizer tokenizer) {
        String id = tokenizer.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("The id of the tokenizer " + tokenizer.getClass().getName() + " is blank");
        }
        if (extensionTokenizers.putIfAbsent(id, tokenizer) != null) {
            throw new IllegalStateException("There is a tokenizer of an extension with the id " + id + " already");
        }
        log.info("Registered the tokenizer {} of an extension", id);
    }

    @Override
    public void unregisterTokenizer(ExtensionTokenizer tokenizer) {
        if (extensionTokenizers.remove(tokenizer.getId(), tokenizer)) {
            // what was found for it must not outlive the extension
            strategyCache.values().removeIf(strategy -> strategy instanceof ExtensionTokenizerStrategy extension && extension.tokenizer() == tokenizer);
            log.info("Unregistered the tokenizer {} of an extension", tokenizer.getId());
        }
    }

    @Override
    public List<TokenizerChoice> getChoices() {
        List<TokenizerChoice> choices = new ArrayList<>();
        for (String name : openAiCompatible.builtInChoices()) {
            choices.add(new TokenizerChoice(name, name));
        }
        extensionTokenizers.values().stream()
                .sorted(Comparator.comparing(ExtensionTokenizer::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(tokenizer -> choices.add(new TokenizerChoice(TokenizerChoice.EXTENSION_PREFIX + tokenizer.getId(), tokenizer.getName())));
        return choices;
    }

    @Override
    public String suggestChoice(AI ai) {
        if (!(ai instanceof OpenAICompatible)) {
            return null;
        }
        TokenizerKey key = openAiCompatible.endpointKey(ai);
        return extensionTokenizers.values().stream()
                .sorted(Comparator.comparing(ExtensionTokenizer::getName, String.CASE_INSENSITIVE_ORDER))
                .filter(tokenizer -> {
                    try {
                        return tokenizer.accepts(key);
                    } catch (Exception e) {
                        log.warn("The tokenizer {} failed to tell whether it accepts {}", tokenizer.getId(), key, e);
                        return false;
                    }
                })
                .map(tokenizer -> TokenizerChoice.EXTENSION_PREFIX + tokenizer.getId())
                .findFirst().orElse(null);
    }

    @Override
    public List<TokenizerProbe> probe(AI ai, String text) {
        TokenizerResolver resolver = resolverFor(ai);
        TokenizerKey key = resolver.keyFor(ai);
        List<TokenizerProbe> results = new ArrayList<>();
        for (TokenizerStrategy strategy : resolver.chain(key, ai)) {
            try {
                results.add(new TokenizerProbe(strategy.getName(), strategy.countTokens(ai, text), null));
            } catch (Exception e) {
                results.add(new TokenizerProbe(strategy.getName(), null, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
            }
        }
        return results;
    }

    /**
     * How many ways of counting were validated, for the tests.
     */
    int cachedStrategies() {
        return strategyCache.size();
    }

}
