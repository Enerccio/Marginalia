package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenizerServiceImpl implements TokenizerService {
    private static final Logger log = LoggerFactory.getLogger(TokenizerServiceImpl.class);

    // Global in-memory cache: AI Cache Key -> Validated Working Strategy
    private final Map<String, TokenizerStrategy> strategyCache = new ConcurrentHashMap<>();

    // Reusable strategy singletons
    private final TokenizerStrategy openAiSdk = new OpenAiSdkTokenizerStrategy();
    private final TokenizerStrategy llamaCpp = new LlamaCppTokenizerStrategy();
    private final LiteLlmTokenizerStrategy liteLlmTokenizerStrategy = new LiteLlmTokenizerStrategy();
    private final LiteLlmAnthropicTokenizerStrategy liteLlmAnthropicTokenizerStrategy = new LiteLlmAnthropicTokenizerStrategy();
    private final TokenizerStrategy heuristic = new HeuristicTokenizerStrategy();

    /**
     * Map each AIType (or AI class) to its ordered list of candidate strategies.
     */
    public List<TokenizerStrategy> getCandidates(AI ai) {
        if (ai == null || ai.getAiType() == null) {
            return List.of(heuristic);
        }

        return switch (ai.getAiType()) {
            case OPEN_AI_COMPATIBLE -> List.of(openAiSdk, llamaCpp, liteLlmTokenizerStrategy, liteLlmAnthropicTokenizerStrategy, heuristic);
            default -> List.of(heuristic);
        };
    }

    @Override
    public long countTokens(AI ai, String text) throws Exception {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String cacheKey = getCacheKey(ai);
        TokenizerStrategy cachedStrategy = strategyCache.get(cacheKey);

        // 1. Fast Path: Executing previously validated strategy
        if (cachedStrategy != null) {
            try {
                return cachedStrategy.countTokens(ai, text);
            } catch (Exception e) {
                log.warn("Cached strategy '{}' failed for AI ID {}, invalidating...", cachedStrategy.getName(), ai.getId());
                strategyCache.remove(cacheKey);
            }
        }

        // 2. Discovery Path: Probe candidates in order
        List<TokenizerStrategy> candidates = getCandidates(ai);
        for (TokenizerStrategy strategy : candidates) {
            try {
                long count = strategy.countTokens(ai, text);
                log.info("Validated tokenizer strategy '{}' for AI ID {}", strategy.getName(), ai.getId());
                
                strategyCache.put(cacheKey, strategy);
                return count;
            } catch (Exception e) {
                log.debug("Strategy '{}' unviable for AI ID {}: {}", strategy.getName(), ai.getId(), e.getMessage());
            }
        }

        return heuristic.countTokens(ai, text);
    }

    @Override
    public void invalidateCache(Long aiId) {
        if (aiId != null) {
            strategyCache.remove("ai_id_" + aiId);
        }
    }

    private String getCacheKey(AI ai) {
        return Long.toString(ai.getId());
    }
}