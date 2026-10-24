package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.AgentRunner.LiveAnswer;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;

/**
 * What the model of a run has written so far in the request that is going on, for the console to show while it is written. In
 * memory only: the transcript stays the record, the answer is stored whole when the round is over, and a request that is
 * cancelled or fails leaves nothing. The worker's thread writes, the poll of a console reads; both are short.
 */
public final class LiveAnswerBuffer {

    /** The most of each part that is kept; a longer one loses its beginning, the end is what is being written. */
    static final int MAX_CHARS = 100_000;

    private final StringBuilder reasoning = new StringBuilder();
    private final StringBuilder text = new StringBuilder();
    private boolean active;

    /** A request starts: what was left is forgotten. */
    public synchronized void begin() {
        reasoning.setLength(0);
        text.setLength(0);
        active = true;
    }

    public synchronized void append(ChunkType type, String chunk) {
        if (!active || chunk == null || chunk.isEmpty()) {
            return;
        }
        StringBuilder part = type == ChunkType.REASONING ? reasoning : text;
        part.append(chunk);
        if (part.length() > MAX_CHARS) {
            part.delete(0, part.length() - MAX_CHARS);
        }
    }

    /** The answer is stored or the request is over without one: nothing is shown any more. */
    public synchronized void end() {
        active = false;
        reasoning.setLength(0);
        text.setLength(0);
    }

    /**
     * @return what has arrived, null when no request is going on
     */
    public synchronized LiveAnswer snapshot() {
        return active ? new LiveAnswer(reasoning.toString(), text.toString()) : null;
    }
}
