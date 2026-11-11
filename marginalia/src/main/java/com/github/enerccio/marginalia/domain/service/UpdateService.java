package com.github.enerccio.marginalia.domain.service;

import java.util.Date;

/**
 * Updating a Docker installation from the application (Admin, Update tab). For administrators only.
 * <p>
 * The application can neither reach the Git checkout nor Docker (it runs in the container, they are on the host), so
 * it only asks: {@link #requestUpdate()} writes the file {@value #REQUEST_FILE} into the folder {@value #FOLDER} of
 * the data folder. A watcher on the host ({@code tools/update-watcher/watch.sh}) sees it and runs {@code rebuild.sh},
 * which builds the new version and restarts the container. The same watcher looks at the remote of the branch the
 * checkout is on and writes what it found into {@value #STATUS_FILE}, which {@link #getStatus()} reads. Without the
 * watcher nothing can be asked.
 */
public interface UpdateService {

    String FOLDER = "update";
    String REQUEST_FILE = "request";
    String STATUS_FILE = "status.properties";
    String LOG_FILE = "update.log";

    /**
     * A status older than this means the watcher does not run any more (it writes one every 15 minutes).
     */
    long WATCHER_TIMEOUT_MS = 60 * 60 * 1000L;

    /**
     * The same while it runs an update, which writes no status until the build ends.
     */
    long RUNNING_TIMEOUT_MS = 3 * 60 * 60 * 1000L;

    /**
     * What the watcher on the host last wrote, and what runs here.
     *
     * @param watching       the watcher is installed and alive; everything from the watcher is only then worth showing
     * @param running        it runs an update now
     * @param requested      an update was asked for and the watcher has not taken it yet
     * @param runningVersion version and commit of the running application, {@code 1.8.0 (93ec118)}
     * @param runningCommit  full commit id of the running application, {@code null} when the build had none
     * @param branch         the branch the checkout is on
     * @param upstream       the remote branch it follows ({@code origin/dev}), {@code null} when it follows none
     * @param remoteCommit   the newest commit of the remote branch, {@code null} when not known
     * @param fetchFailed    the remote could not be reached at the last look, {@code remoteCommit} may be old
     * @param checked        when the watcher last looked
     * @param lastRun        when the last update ended, {@code null} when there was none
     * @param lastRunOk      how it ended
     */
    record UpdateStatus(boolean watching, boolean running, boolean requested, String runningVersion, String runningCommit,
                        String branch, String upstream, String remoteCommit, boolean fetchFailed, Date checked,
                        Date lastRun, boolean lastRunOk) {

        /**
         * The remote branch has a commit other than the one that runs.
         */
        public boolean updateAvailable() {
            return watching && remoteCommit != null && runningCommit != null && !remoteCommit.equals(runningCommit);
        }

        /**
         * An update can be asked for now.
         */
        public boolean canRequest() {
            return watching && !running && !requested;
        }
    }

    UpdateStatus getStatus() throws Exception;

    /**
     * Asks the watcher to update and restart the installation, also when there is nothing new (a rebuild).
     *
     * @throws IllegalStateException when the watcher does not run, or an update runs or is asked for already
     */
    void requestUpdate() throws Exception;

    /**
     * The end of the output of the last update, empty when there was none.
     */
    String readLog(int maxBytes) throws Exception;
}
