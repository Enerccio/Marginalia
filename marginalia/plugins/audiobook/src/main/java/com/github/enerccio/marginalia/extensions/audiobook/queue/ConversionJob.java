package com.github.enerccio.marginalia.extensions.audiobook.queue;

import com.github.enerccio.marginalia.extensions.audiobook.script.PartScript;

import java.util.UUID;

/**
 * The conversion of one part: its state and how far it is. The fields are changed by the queue only; a reader (the window with the list of
 * jobs) takes a {@link #snapshot()}.
 */
public final class ConversionJob {

    public enum State {
        /** Waits for its turn. */
        WAITING,
        /** Being made (the script is made, the pieces are spoken, the part is joined). */
        RUNNING,
        /** The audio of the part is stored. */
        DONE,
        /** Stopped by an error, see the message; a retry keeps the pieces already made. */
        FAILED,
        /** Stopped by the user. */
        CANCELLED
    }

    /** What a job makes: the audio of the part, only its script (the annotation, nothing is spoken), or only the audio of some pieces (a hearing). */
    public enum Mode {
        PART, SCRIPT_ONLY, SEGMENTS_ONLY
    }

    /** What the part is made from: asked when the job runs, so it reads the text of the part as it is then (and may fail). */
    @FunctionalInterface
    public interface ScriptSource {
        PartScript get() throws Exception;
    }

    /** A copy of the state of a job. */
    public record Snapshot(String id, String partId, String title, State state, String message, int done, int total, Mode mode) {

        public boolean finished() {
            return state == State.DONE || state == State.FAILED || state == State.CANCELLED;
        }
    }

    private final String id = UUID.randomUUID().toString();
    private final String partId;
    private final String title;
    private final ScriptSource source;
    private final Mode mode;

    private final LabelTrace trace = new LabelTrace();

    private State state = State.WAITING;
    private String message = "";
    private int done;
    private int total;
    private boolean cancelRequested;

    ConversionJob(String partId, String title, ScriptSource source, Mode mode) {
        this.partId = partId;
        this.title = title;
        this.source = source;
        this.mode = mode;
    }

    /** What the labelling model said while this job made its script. */
    public LabelTrace trace() {
        return trace;
    }

    public Mode mode() {
        return mode;
    }

    public String id() {
        return id;
    }

    public String partId() {
        return partId;
    }

    ScriptSource source() {
        return source;
    }

    synchronized void set(State state, String message) {
        this.state = state;
        this.message = message == null ? "" : message;
    }

    synchronized void progress(int done, int total) {
        this.done = done;
        this.total = total;
    }

    synchronized void advance() {
        done++;
    }

    synchronized State state() {
        return state;
    }

    synchronized void requestCancel() {
        cancelRequested = true;
    }

    synchronized boolean cancelRequested() {
        return cancelRequested;
    }

    /** A new try: the state is waiting again. */
    synchronized void reset() {
        state = State.WAITING;
        message = "";
        done = 0;
        total = 0;
        cancelRequested = false;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(id, partId, title, state, message, done, total, mode);
    }
}
