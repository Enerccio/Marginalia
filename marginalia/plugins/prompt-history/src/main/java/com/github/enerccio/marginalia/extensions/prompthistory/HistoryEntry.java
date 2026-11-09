package com.github.enerccio.marginalia.extensions.prompthistory;

import com.github.enerccio.marginalia.domain.service.InferenceListener.*;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.google.gson.reflect.TypeToken;

import java.time.Instant;
import java.util.List;

/**
 * One request that was sent to a model and how it is going or how it ended. The messages are kept packed from the start
 * and the answer once the request ends (see {@link Packing}); while it runs, the answer written so far is kept as plain
 * text, it is small and still growing.
 */
final class HistoryEntry {

    /** What the dialog shows of the answer. */
    record Answer(String text, String reasoning, List<FunctionCall> calls, String error) {
    }

    private record PackedAnswer(String text, String reasoning, List<FunctionCall> calls, String error) {
    }

    private final Instant startedAt;
    private final String kind;
    private final String aiName;
    private final String model;
    private final byte[] packedMessages;
    private final HistoryStore store;

    private final StringBuilder liveText = new StringBuilder();
    private final StringBuilder liveReasoning = new StringBuilder();

    private volatile byte[] packedAnswer;
    private volatile Outcome outcome;
    private volatile long millis;

    HistoryEntry(Request request, HistoryStore store) {
        this.startedAt = request.startedAt();
        this.kind = request.kind();
        this.aiName = request.aiName();
        this.model = request.model();
        this.packedMessages = Packing.pack(request.messages());
        this.store = store;
    }

    Instant startedAt() {
        return startedAt;
    }

    /** The type of the request, null when it was started outside of any scope. */
    String kind() {
        return kind;
    }

    String aiName() {
        return aiName;
    }

    String model() {
        return model;
    }

    /** How the request ended, null while it runs. */
    Outcome outcome() {
        return outcome;
    }

    long millis() {
        return millis;
    }

    boolean running() {
        return outcome == null;
    }

    /** Unpacks the messages that were sent. */
    List<Message> messages() {
        return Packing.unpack(packedMessages, new TypeToken<List<Message>>() {
        }.getType());
    }

    /** The answer so far, or the whole of it (unpacked) when the request has ended. */
    Answer answer() {
        byte[] packed = packedAnswer;
        if (packed != null) {
            PackedAnswer answer = Packing.unpack(packed, PackedAnswer.class);
            return new Answer(answer.text(), answer.reasoning(), answer.calls() == null ? List.of() : answer.calls(), answer.error());
        }
        synchronized (this) {
            return new Answer(liveText.toString(), liveReasoning.isEmpty() ? null : liveReasoning.toString(), List.of(), null);
        }
    }

    void chunk(ChunkType type, String text) {
        if (text == null) {
            return;
        }
        synchronized (this) {
            (type == ChunkType.REASONING ? liveReasoning : liveText).append(text);
        }
        store.changedSoon();
    }

    void finish(Response response) {
        packedAnswer = Packing.pack(new PackedAnswer(response.text(), response.reasoning(), response.calls(), response.error()));
        millis = response.millis();
        outcome = response.outcome();
        synchronized (this) {
            liveText.setLength(0);
            liveText.trimToSize();
            liveReasoning.setLength(0);
            liveReasoning.trimToSize();
        }
        store.changed();
    }
}
