package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AdminHelpTools} over an {@link AdminDataSource}.
 */
public final class AdminHelpToolsImpl implements AdminHelpTools {
    private static final Logger log = LoggerFactory.getLogger(AdminHelpToolsImpl.class);

    static final int DEFAULT_LOG_LINES = 40;
    static final int MAX_LOG_LINES = 200;

    private final AdminDataSource source;

    public AdminHelpToolsImpl(AdminDataSource source) {
        this.source = source;
    }

    @Override
    public void getAdminData(FunctionContinuation<String> done, String topic) {
        String wanted = topic == null ? "" : topic.strip().toLowerCase();
        if (!AdminDataSource.TOPICS.contains(wanted)) {
            done.fail("There is no topic \"" + topic + "\". The topics are: " + String.join(", ", AdminDataSource.TOPICS) + ".");
            return;
        }
        try {
            done.complete(source.read(wanted));
        } catch (SecurityException e) {
            done.fail("Only an administrator can read this.");
        } catch (Exception e) {
            log.warn("The help assistant could not read the admin data {}", wanted, e);
            done.fail("The data could not be read.");
        }
    }

    @Override
    public void readExtensionLog(FunctionContinuation<String> done, String extension, int lines) {
        if (extension == null || extension.isBlank()) {
            done.fail("Name the extension. get_admin_data with topic extensions lists them.");
            return;
        }
        int wanted = lines < 1 ? DEFAULT_LOG_LINES : Math.min(lines, MAX_LOG_LINES);
        try {
            done.complete(source.readExtensionLog(extension.strip(), wanted));
        } catch (IllegalArgumentException e) {
            done.fail(e.getMessage());
        } catch (SecurityException e) {
            done.fail("Only an administrator can read this.");
        } catch (Exception e) {
            log.warn("The help assistant could not read the log of {}", extension, e);
            done.fail("The log could not be read.");
        }
    }
}
