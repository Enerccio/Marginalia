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
    /** The id of the call of the agent that began to change something, until its result is stored. */
    public static final String CALL_MARKER = "call-began";
    /** The same for a call of a sub-agent: this + "-" + the handle of the sub-agent (only one call of it is in the air). */
    public static final String SUB_CALL_MARKER = CALL_MARKER + "-";
    /** What the author wrote to a conversation while it worked and the agent did not read yet. */
    public static final String QUEUE = "queue.json";
    /** The last results of the commands of the agent, whole, with their numbers (see {@link CommandResults}). */
    public static final String RESULTS = "results.json";
    /** What the runner took from the queue to deliver and has not finished: the messages and commands of the author in the air. */
    public static final String DELIVERY = "delivery.json";
    /** The to-do list of the agent (the last {@code update_plan}), kept at every change so a restart and a compaction do not lose it. */
    public static final String PLAN = "plan.json";

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
