package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.TokenizerResolver;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.TokenizerStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A read-write transaction holds the database write lock, so the tokenizer must not call the model server meanwhile.
 */
class TokenizerServiceImplTest {

    private static final String TEXT = "The quick brown fox jumps over the lazy dog.";

    private final AtomicInteger remoteCalls = new AtomicInteger();

    private final TokenizerServiceImpl service = new TokenizerServiceImpl() {
        @Override
        protected TokenizerResolver resolverFor(AI ai) {
            return new TokenizerResolver() {
                @Override
                public TokenizerKey keyFor(AI ai) {
                    return TokenizerKey.of("remote-" + ((OpenAICompatible) ai).getUri());
                }

                @Override
                public List<TokenizerStrategy> chain(TokenizerKey key, AI ai) {
                    return List.of(new TokenizerStrategy() {
                        @Override
                        public long countTokens(AI ai, String text) {
                            remoteCalls.incrementAndGet();
                            return 1000;
                        }

                        @Override
                        public String getName() {
                            return "REMOTE";
                        }
                    });
                }
            };
        }
    };

    @AfterEach
    void clearTransaction() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
    }

    private static AI ai() {
        return ai(1L, "a");
    }

    private static AI ai(long id, String uri) {
        OpenAICompatible ai = new OpenAICompatible();
        ai.setId(id);
        ai.setUri(uri);
        return ai;
    }

    @Test
    void withoutTransactionTheModelServerCounts() throws Exception {
        assertThat(service.countTokens(ai(), TEXT)).isEqualTo(1000);
        assertThat(remoteCalls).hasValue(1);
    }

    @Test
    void readOnlyTransactionTheModelServerCounts() throws Exception {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);

        assertThat(service.countTokens(ai(), TEXT)).isEqualTo(1000);
        assertThat(remoteCalls).hasValue(1);
    }

    @Test
    void writeTransactionCountsApproximately() throws Exception {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);

        long tokens = service.countTokens(ai(), TEXT);

        assertThat(tokens).isEqualTo(service.countTokensApprox(TEXT)).isBetween(5L, 20L);
        assertThat(remoteCalls).hasValue(0);
    }

    @Test
    void aisWithTheSameKeyShareTheProbedStrategy() throws Exception {
        service.countTokens(ai(1L, "a"), TEXT);
        service.countTokens(ai(2L, "a"), TEXT);

        // the strategy is validated by the first count, the second one runs it once, not probes again
        assertThat(remoteCalls).hasValue(2);
        assertThat(service.cachedStrategies()).isEqualTo(1);
    }

    @Test
    void aisWithAnotherKeyAreProbedSeparately() throws Exception {
        service.countTokens(ai(1L, "a"), TEXT);
        service.countTokens(ai(2L, "b"), TEXT);

        assertThat(service.cachedStrategies()).isEqualTo(2);
    }

    @Test
    void invalidatingAnAiForgetsTheStrategyOfItsKey() throws Exception {
        service.countTokens(ai(1L, "a"), TEXT);
        service.invalidateCache(1L);

        assertThat(service.cachedStrategies()).isZero();
    }
}
