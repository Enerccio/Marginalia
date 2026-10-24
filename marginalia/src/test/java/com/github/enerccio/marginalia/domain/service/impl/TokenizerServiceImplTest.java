package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
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
        public List<TokenizerStrategy> getCandidates(AI ai) {
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

    @AfterEach
    void clearTransaction() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
    }

    private static AI ai() {
        OpenAICompatible ai = new OpenAICompatible();
        ai.setId(1L);
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
}
