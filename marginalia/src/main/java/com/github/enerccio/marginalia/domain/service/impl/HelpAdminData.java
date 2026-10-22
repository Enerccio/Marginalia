package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.bound.UiProblem;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.CleanupService;
import com.github.enerccio.marginalia.domain.service.CleanupService.CleanupPlan;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityStats;
import com.github.enerccio.marginalia.domain.service.DatabaseBackupService;
import com.github.enerccio.marginalia.domain.service.DatabaseBackupService.BackupSchedule;
import com.github.enerccio.marginalia.domain.service.DatabaseBackupService.DatabaseBackup;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.OsgiService.ExtensionReport;
import com.github.enerccio.marginalia.domain.service.manual.AdminDataSource;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.osgi.framework.Bundle;
import org.springframework.web.context.request.RequestAttributes;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * What the help assistant reads of the Admin page for an administrator (see {@link AdminDataSource}): the same data the
 * screens show, shortened for a model, with no names of users, no keys and no paths. The functions run on threads of
 * their own, so every read runs in the request of the administrator that asked, and checks that they still are one.
 */
final class HelpAdminData implements AdminDataSource {

    /** The most text the model gets of a log: the end of it, where the news is. */
    static final int MAX_LOG_CHARS = 6000;

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final AdminGuard adminGuard;
    private final OsgiService osgiService;
    private final UserService userService;
    private final DatabaseBackupService backupService;
    private final CleanupService cleanupService;
    private final RequestAttributes request;

    /**
     * @param osgiService null where there are no extensions (the application without its OSGi service)
     * @param request     the request of the administrator, to run the reads in
     */
    HelpAdminData(AdminGuard adminGuard, OsgiService osgiService, UserService userService, DatabaseBackupService backupService,
                  CleanupService cleanupService, RequestAttributes request) {
        this.adminGuard = adminGuard;
        this.osgiService = osgiService;
        this.userService = userService;
        this.backupService = backupService;
        this.cleanupService = cleanupService;
        this.request = request;
    }

    @Override
    public String read(String topic) throws Exception {
        try (InRequestScope _ = new InRequestScope(request)) {
            adminGuard.requireAdmin();
            return switch (topic) {
                case "extensions" -> extensions();
                case "users" -> users();
                case "backups" -> backups();
                case "cleanup" -> cleanup();
                case "application" -> application();
                default -> throw new IllegalArgumentException("There is no topic \"" + topic + "\". The topics are: "
                        + String.join(", ", TOPICS) + ".");
            };
        }
    }

    @Override
    public String readExtensionLog(String extension, int lines) throws Exception {
        try (InRequestScope _ = new InRequestScope(request)) {
            adminGuard.requireAdmin();
            Bundle bundle = findExtension(extension);
            String log = osgiService.readLog(bundle, 64 * 1024);
            return tail(name(bundle), log, lines);
        }
    }

    /** The last lines of the log, each one cleaned of secrets and cut, the whole cut at {@link #MAX_LOG_CHARS} from the front. */
    static String tail(String extension, String log, int lines) {
        List<String> all = log == null ? List.of() : log.lines().filter(line -> !line.isBlank()).toList();
        if (all.isEmpty()) {
            return "The log of " + extension + " is empty: it wrote nothing yet, or the log was cleared.";
        }
        List<String> last = new ArrayList<>(all.subList(Math.max(0, all.size() - lines), all.size()));
        last.replaceAll(UiProblem::clean);
        String text = String.join("\n", last);
        if (text.length() > MAX_LOG_CHARS) {
            text = "[...]\n" + text.substring(text.indexOf('\n', text.length() - MAX_LOG_CHARS) + 1);
        }
        return "Last " + last.size() + " of " + all.size() + " lines of the log of " + extension + ", newest last:\n\n" + text;
    }

    private String extensions() {
        if (osgiService == null) {
            return "The extension system is not running in this installation.";
        }
        List<Bundle> bundles = osgiService.getBundles();
        if (bundles.isEmpty()) {
            return "No extensions are installed.";
        }
        List<Bundle> loaded = osgiService.getLoadedExtensions().stream().map(OsgiService.LoadedExtension::bundle).toList();
        StringBuilder sb = new StringBuilder(bundles.size() + " extension(s) installed:\n");
        for (Bundle bundle : bundles) {
            sb.append("\n- **").append(name(bundle)).append("** ").append(bundle.getVersion()).append(": ")
                    .append(state(bundle, loaded.contains(bundle)));
            ExtensionReport report = osgiService.getVerificationReport(bundle);
            if (report != null) {
                sb.append("; verification ").append(report.valid() ? "passed" : "FAILED");
                if (!report.valid()) {
                    sb.append(" (").append(UiProblem.clean(report.text())).append(')');
                }
            }
        }
        return sb.toString();
    }

    private String state(Bundle bundle, boolean loaded) {
        if (osgiService.isStopped(bundle)) {
            return "stopped by the administrator";
        }
        String state = switch (bundle.getState()) {
            case Bundle.ACTIVE -> "active";
            case Bundle.RESOLVED -> "resolved, not started";
            case Bundle.INSTALLED -> "installed, not resolved (a dependency is missing?)";
            case Bundle.STARTING -> "starting";
            case Bundle.STOPPING -> "stopping";
            default -> "not installed";
        };
        return bundle.getState() == Bundle.ACTIVE && !loaded ? state + ", but has not finished loading" : state;
    }

    private Bundle findExtension(String wanted) {
        if (osgiService == null) {
            throw new IllegalArgumentException("The extension system is not running in this installation.");
        }
        for (Bundle bundle : osgiService.getBundles()) {
            if (wanted.equalsIgnoreCase(name(bundle)) || wanted.equalsIgnoreCase(bundle.getSymbolicName())) {
                return bundle;
            }
        }
        Bundle byJar = osgiService.findBundleByJar(wanted);
        if (byJar != null) {
            return byJar;
        }
        throw new IllegalArgumentException("There is no extension \"" + wanted + "\". The installed ones: "
                + osgiService.getBundles().stream().map(HelpAdminData::name).collect(Collectors.joining(", ")) + ".");
    }

    private static String name(Bundle bundle) {
        String name = bundle.getHeaders().get("Bundle-Name");
        return name != null && !name.isBlank() ? name : String.valueOf(bundle.getSymbolicName());
    }

    private String users() throws Exception {
        long now = System.currentTimeMillis();
        List<User> users = userService.findAll().stream().filter(user -> !user.isDeleted()).toList();
        long administrators = users.stream().filter(User::isAdmin).count();
        long locked = users.stream().filter(user -> user.getLockedUntil() > now).count();
        return users.size() + " users, " + administrators + " of them administrators, " + locked
                + " locked out at the moment (too many wrong passwords; an administrator can unlock them in Admin > Users).";
    }

    private String backups() throws Exception {
        List<DatabaseBackup> backups = backupService.getBackups();
        StringBuilder sb = new StringBuilder("Database size: ").append(megabytes(backupService.getDatabaseSize())).append(".\n");
        sb.append(backups.size()).append(" database backup(s)");
        backups.stream().map(DatabaseBackup::getCreation).max(Date::compareTo)
                .ifPresentOrElse(newest -> sb.append(", the newest from ").append(TIME.format(newest.toInstant())),
                        () -> sb.append(", none was made yet"));
        sb.append(".\n");
        BackupSchedule schedule = backupService.getSchedule();
        if (schedule.enabled()) {
            sb.append("Scheduled backups are on (cron ").append(schedule.cron()).append(", the last ").append(schedule.keep())
                    .append(" are kept); last run: ").append(schedule.lastRun() == null ? "never" : TIME.format(schedule.lastRun().toInstant()));
            Date next = backupService.getNextScheduledBackup();
            sb.append("; next: ").append(next == null ? "not planned" : TIME.format(next.toInstant())).append(".\n");
        } else {
            sb.append("Scheduled backups are off.\n");
        }
        sb.append(backupService.isRestorePending()
                ? "A restore of a backup is waiting for the next start of the application."
                : "No restore is pending.");
        return sb.toString();
    }

    private String cleanup() throws Exception {
        CleanupPlan plan = cleanupService.analyze();
        StringBuilder sb = new StringBuilder("What a cleanup would do now (nothing was changed): ")
                .append(plan.getPurge().size()).append(" deleted item(s) could be purged, ")
                .append(plan.getBlocked().size()).append(" are blocked by something that still refers to them.\n");
        for (EntityStats stats : plan.getStats().values()) {
            if (stats.getSoftDeleted() > 0) {
                sb.append("\n- ").append(stats.getType().getSimpleName()).append(": ").append(stats.getSoftDeleted())
                        .append(" deleted, ").append(stats.getPurgeable()).append(" purgeable, ").append(stats.getBlocked())
                        .append(" blocked");
            }
        }
        return sb.toString().strip();
    }

    private String application() {
        Runtime runtime = Runtime.getRuntime();
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        return "Java " + Runtime.version() + ", " + runtime.availableProcessors() + " processors. Memory in use "
                + megabytes(runtime.totalMemory() - runtime.freeMemory()) + " of at most " + megabytes(runtime.maxMemory())
                + ". Running for " + uptime / 3600 + " h " + uptime / 60 % 60 + " min since "
                + TIME.format(Instant.now().minusSeconds(uptime)) + ". Extensions: "
                + (osgiService == null ? "the extension system is not running" : osgiService.getBundles().size() + " installed")
                + ".";
    }

    private static String megabytes(long bytes) {
        return bytes < 1024 * 1024 ? bytes / 1024 + " kB" : bytes / (1024 * 1024) + " MB";
    }
}
