package com.github.enerccio.marginalia.domain.service.agent;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Where the files of a run are on the disk: the folder of the run ({@code agent-runs/<run uuid>}) with the base
 * snapshot of the manuscript and the checkpoints, and the workspace of its agent ({@code agent-workspaces/<agent uuid>}),
 * which outlives the run. Only the workspace and the checkpoints are given to the model, as mounts of the
 * {@link VirtualFileSystem}; the base is not reachable through any of them.
 * <p>
 * The folders are named by uuids and the uuids are checked, so no value of the database can make a path leave the
 * folders of the agents. This class only knows the layout; it creates nothing.
 */
public final class RunFolder {

    public static final String RUNS = "agent-runs";
    public static final String WORKSPACES = "agent-workspaces";
    public static final String BASE = "base";
    public static final String CHECKPOINTS = "checkpoints";
    /**
     * The file of the base folder with the snapshot of the original book (the backup with its lorebooks).
     */
    public static final String BASE_MANUSCRIPT = "manuscript.json";
    /**
     * The files of the base folder with the report of the last sync of the run, and the mark that the model was not told of it yet.
     */
    public static final String SYNC_REPORT = "sync-report.txt";
    public static final String SYNC_PENDING = "sync-pending";
    /** The note of the author with a resume, until the model has read it. */
    public static final String RESUME_NOTE = "resume-note.txt";

    private final Path run;
    private final Path workspace;

    private RunFolder(Path run, Path workspace) {
        this.run = run;
        this.workspace = workspace;
    }

    /**
     * @param home the application folder, the parent of {@code agent-runs} and {@code agent-workspaces}
     * @throws IllegalArgumentException when a uuid is not one
     */
    public static RunFolder of(Path home, String runUuid, String agentUuid) {
        return new RunFolder(home.resolve(RUNS).resolve(checkUuid(runUuid)), workspaceOf(home, agentUuid));
    }

    /**
     * The workspace of an agent.
     */
    public static Path workspaceOf(Path home, String agentUuid) {
        return home.resolve(WORKSPACES).resolve(checkUuid(agentUuid));
    }

    /**
     * The folder of a run.
     */
    public static Path runOf(Path home, String runUuid) {
        return home.resolve(RUNS).resolve(checkUuid(runUuid));
    }

    private static String checkUuid(String uuid) {
        try {
            if (uuid != null && UUID.fromString(uuid).toString().equalsIgnoreCase(uuid)) {
                return uuid.toLowerCase();
            }
        } catch (IllegalArgumentException ignored) {
            // reported below
        }
        throw new IllegalArgumentException("Not a uuid: " + uuid);
    }

    public Path run() {
        return run;
    }

    public Path workspace() {
        return workspace;
    }

    public Path base() {
        return run.resolve(BASE);
    }

    public Path baseManuscript() {
        return base().resolve(BASE_MANUSCRIPT);
    }

    public Path checkpoints() {
        return run.resolve(CHECKPOINTS);
    }

}
