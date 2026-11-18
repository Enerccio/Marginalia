package com.github.enerccio.marginalia.domain.service;

/**
 * Told of every request {@link UsageService#record} counts, after the usage of the AI is added. Marginalia counts the
 * tokens of a book in its writing statistics with it. Beans of this type are found by the usage service; one must not
 * throw (a failure is logged and the others still run).
 */
public interface UsageListener {

    /**
     * @param contextId       the id the request was made for ({@link UsageContext#current()}), null when none
     * @param prefillTokens   the tokens of the prompt
     * @param generatedTokens the tokens of the answer
     */
    void counted(Long contextId, long prefillTokens, long generatedTokens);
}
