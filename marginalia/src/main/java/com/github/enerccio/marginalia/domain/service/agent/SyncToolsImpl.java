package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

/**
 * {@link SyncTools} of one run.
 */
public final class SyncToolsImpl implements SyncTools {

    public static final String NO_REPORT = "The user did not change the book while the run was paused (or the run was never paused).";

    private final Supplier<String> report;

    /**
     * @param report the report of the last sync of the run; null or empty for none
     */
    public SyncToolsImpl(Supplier<String> report) {
        this.report = report;
    }

    @Override
    public void getSyncReport(FunctionContinuation<String> done) {
        String text;
        try {
            text = report.get();
        } catch (RuntimeException e) {
            done.fail("The report can't be read: " + e.getMessage());
            return;
        }
        done.complete(StringUtils.isBlank(text) ? NO_REPORT : text);
    }
}
