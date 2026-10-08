package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class SummaryCrudTest extends ExtendableCrudContract<Summary> {

    @Autowired
    private SummaryService summaryService;

    @Override
    protected OwnedService<Summary, ?> service() {
        return summaryService;
    }

    @Override
    protected Summary newEntity() {
        Summary summary = new Summary();
        summary.setSummary("The story so far.");
        summary.setSummaryTokens(5L);
        summary.setReasoning("Condensing chapters");
        summary.setReasoningTokens(4L);
        summary.setSummaryMessageHash("abc123");
        return summary;
    }

    @Override
    protected void assertCreated(Summary loaded) {
        assertThat(loaded.getSummary()).isEqualTo("The story so far.");
        assertThat(loaded.getSummaryTokens()).isEqualTo(5L);
        assertThat(loaded.getReasoning()).isEqualTo("Condensing chapters");
        assertThat(loaded.getReasoningTokens()).isEqualTo(4L);
        assertThat(loaded.getSummaryMessageHash()).isEqualTo("abc123");
    }

    @Override
    protected void modify(Summary entity) {
        entity.setSummary("Updated summary.");
        entity.setReasoning(null);
    }

    @Override
    protected void assertModified(Summary loaded) {
        assertThat(loaded.getSummary()).isEqualTo("Updated summary.");
        assertThat(loaded.getReasoning()).isNull();
        assertThat(loaded.getSummaryMessageHash()).isEqualTo("abc123");
    }

    @Test
    void copySummaryCreatesIndependentCopy() throws Exception {
        Summary original = create();

        Summary copy = summaryService.copySummary(reload(original));

        assertThat(copy.getId()).isNotEqualTo(original.getId());
        assertThat(copy.getUuid()).isNotEqualTo(original.getUuid());
        assertCreated(reload(copy));

        Summary changed = reload(copy);
        changed.setSummary("only the copy");
        summaryService.save(changed);
        assertThat(reload(original).getSummary()).isEqualTo("The story so far.");
    }

    @Test
    void copyOfNullIsNull() throws Exception {
        assertThat(summaryService.copySummary(null)).isNull();
    }
}
