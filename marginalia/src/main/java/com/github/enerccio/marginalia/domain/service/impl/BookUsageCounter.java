package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.service.UsageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * The same request counts for the book it was made for (the id of the {@link com.github.enerccio.marginalia.domain.service.UsageContext}),
 * in the writing statistics of the owner of the book (which may not be the user of a shared AI, and a thread without a
 * user can count it too). Never throws.
 */
public class BookUsageCounter implements UsageListener {

    private static final Logger log = LoggerFactory.getLogger(BookUsageCounter.class);

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private ManuscriptStatisticService statisticService;

    @Override
    public void counted(Long manuscriptId, long prefillTokens, long generatedTokens) {
        if (manuscriptId == null) {
            return;
        }
        try {
            Manuscript book = manuscriptService.find(manuscriptId);
            if (book != null) {
                statisticService.count(book, StatisticCounts.tokens(prefillTokens, generatedTokens));
            }
        } catch (Exception e) {
            log.warn("Failed to count the tokens of book {}", manuscriptId, e);
        }
    }
}
