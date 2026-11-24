package com.github.enerccio.marginalia.test;

import com.github.enerccio.marginalia.domain.service.ActivityTracker;
import com.github.enerccio.marginalia.domain.service.ManuscriptSizeService;
import com.github.enerccio.marginalia.domain.service.impl.ActivityTrackerImpl;
import com.github.enerccio.marginalia.domain.service.impl.ManuscriptSizeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

/**
 * Base for the tests of the application: the {@link PlatformTestBase} with what the application's background writers need (the
 * sizes of the books, the activity of generations and agent runs). Helpers for books and lorebooks are in the bases on top
 * ({@link GenerationTestBase}, {@link AgentTestBase}).
 */
public abstract class MarginaliaTestBase extends PlatformTestBase {

    @Autowired
    private ManuscriptSizeService sizeService;

    @Autowired
    private ActivityTracker activityTracker;

    private boolean quietSizes;

    @AfterEach
    void resumeBackgroundWriters() {
        if (quietSizes) {
            quietSizes = false;
            sizeImpl().setManual(false);
            trackerImpl().setManual(false);
        }
    }

    private ActivityTrackerImpl trackerImpl() {
        return (ActivityTrackerImpl) AopTestUtils.getUltimateTargetObject(activityTracker);
    }

    private ManuscriptSizeServiceImpl sizeImpl() {
        return (ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService);
    }

    /**
     * For a test that rewrites whole tables in one transaction (a migration): what the background writers have pending from
     * earlier tests (the sizes of the books they changed, the activity of their generations and agent runs) is written now, and
     * the writers do not write by themselves until the test ends. Their delayed writes would otherwise land in the middle of the
     * test's transaction, which then fails with SQLITE_BUSY (only after tests that changed many books).
     */
    protected void quietBackgroundWriters() {
        quietSizes = true;
        ManuscriptSizeServiceImpl sizes = sizeImpl();
        sizes.setManual(true);
        sizes.flush();
        ActivityTrackerImpl tracker = trackerImpl();
        tracker.setManual(true);
        tracker.flush();
    }
}
