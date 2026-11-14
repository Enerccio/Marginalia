package com.github.enerccio.marginalia.update;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.service.UpdateService;
import com.github.enerccio.marginalia.domain.service.UpdateService.UpdateStatus;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.utils.BuildInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The application's side of Admin, Update ({@link UpdateService}): it reads the status the watcher on the host writes into the
 * folder {@code update} of the data folder and asks for an update with a file there. The watcher is played by the test.
 */
class UpdateServiceTest extends MarginaliaTestBase {

    private static final String OTHER_COMMIT = "0123456789abcdef0123456789abcdef01234567";

    @Autowired
    private UpdateService updateService;

    @Autowired
    private Configuration configuration;

    private Path folder;

    @BeforeEach
    void setUp() throws Exception {
        folder = configuration.getFolder().toPath().resolve(UpdateService.FOLDER);
        Files.createDirectories(folder);
        Files.deleteIfExists(folder.resolve(UpdateService.STATUS_FILE));
        Files.deleteIfExists(folder.resolve(UpdateService.REQUEST_FILE));
        Files.deleteIfExists(folder.resolve(UpdateService.LOG_FILE));
        loginAdmin();
    }

    @Test
    void onlyAdministratorsUpdate() throws Exception {
        watcherWrites("idle", now(), OTHER_COMMIT, "");
        login();

        assertThatThrownBy(() -> updateService.getStatus()).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> updateService.requestUpdate()).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> updateService.readLog(100)).isInstanceOf(SecurityException.class);
        assertThat(folder.resolve(UpdateService.REQUEST_FILE)).doesNotExist();
    }

    @Test
    void withoutTheWatcherNothingCanBeAsked() throws Exception {
        UpdateStatus status = updateService.getStatus();

        assertThat(status.watching()).isFalse();
        assertThat(status.canRequest()).isFalse();
        assertThat(status.updateAvailable()).isFalse();
        assertThat(status.runningVersion()).isEqualTo(BuildInfo.current().getShortText());
        assertThatThrownBy(() -> updateService.requestUpdate()).isInstanceOf(IllegalStateException.class);
        assertThat(folder.resolve(UpdateService.REQUEST_FILE)).doesNotExist();
        assertThat(updateService.readLog(100)).isEmpty();
    }

    @Test
    void aWatcherThatStoppedWritingIsNotThere() throws Exception {
        watcherWrites("idle", now() - UpdateService.WATCHER_TIMEOUT_MS / 1000 - 60, OTHER_COMMIT, "");

        assertThat(updateService.getStatus().watching()).isFalse();
        assertThatThrownBy(() -> updateService.requestUpdate()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theStatusOfTheWatcherIsRead() throws Exception {
        watcherWrites("idle", now(), OTHER_COMMIT, "fetchFailed=0\nlastRun=" + (now() - 100) + "\nlastResult=failed\n");

        UpdateStatus status = updateService.getStatus();

        assertThat(status.watching()).isTrue();
        assertThat(status.running()).isFalse();
        assertThat(status.branch()).isEqualTo("dev");
        assertThat(status.upstream()).isEqualTo("origin/dev");
        assertThat(status.remoteCommit()).isEqualTo(OTHER_COMMIT);
        assertThat(status.fetchFailed()).isFalse();
        assertThat(status.checked()).isNotNull();
        assertThat(status.lastRun()).isNotNull();
        assertThat(status.lastRunOk()).isFalse();
        // the remote branch has another commit than the build that runs, when the build knows its own
        assertThat(status.updateAvailable()).isEqualTo(BuildInfo.current().getCommitFull() != null);
    }

    @Test
    void theCommitThatRunsIsNoUpdate() throws Exception {
        String running = BuildInfo.current().getCommitFull();
        watcherWrites("idle", now(), running == null ? "" : running, "lastRun=" + now() + "\nlastResult=ok\n");

        UpdateStatus status = updateService.getStatus();

        assertThat(status.updateAvailable()).isFalse();
        assertThat(status.lastRunOk()).isTrue();
        // a rebuild can still be asked for
        assertThat(status.canRequest()).isTrue();
    }

    @Test
    void anUpdateIsAskedForOnce() throws Exception {
        watcherWrites("idle", now(), OTHER_COMMIT, "");

        updateService.requestUpdate();

        Path request = folder.resolve(UpdateService.REQUEST_FILE);
        assertThat(request).exists();
        assertThat(Files.readString(request)).startsWith(currentUser.getLogin() + " ");
        UpdateStatus status = updateService.getStatus();
        assertThat(status.requested()).isTrue();
        assertThat(status.canRequest()).isFalse();
        assertThatThrownBy(() -> updateService.requestUpdate()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noSecondUpdateWhileOneRuns() throws Exception {
        // the build writes no status until it ends, so a running one is trusted for longer
        watcherWrites("running", now() - UpdateService.WATCHER_TIMEOUT_MS / 1000 - 60, OTHER_COMMIT, "");

        UpdateStatus status = updateService.getStatus();

        assertThat(status.watching()).isTrue();
        assertThat(status.running()).isTrue();
        assertThat(status.canRequest()).isFalse();
        assertThatThrownBy(() -> updateService.requestUpdate()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theEndOfTheLogIsRead() throws Exception {
        Files.writeString(folder.resolve(UpdateService.LOG_FILE), "first line\nsecond line\nthird line\n", StandardCharsets.UTF_8);

        assertThat(updateService.readLog(1000)).isEqualTo("first line\nsecond line\nthird line\n");
        // cut inside the second line: the text starts at the third
        assertThat(updateService.readLog(15)).isEqualTo("third line\n");
    }

    private static long now() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * What {@code tools/update-watcher/watch.sh} writes.
     */
    private void watcherWrites(String state, long checked, String remote, String more) throws Exception {
        Files.writeString(folder.resolve(UpdateService.STATUS_FILE), "state=" + state + "\nbranch=dev\nupstream=origin/dev\nlocal="
                + remote + "\nremote=" + remote + "\nchecked=" + checked + "\n" + more, StandardCharsets.UTF_8);
    }
}
