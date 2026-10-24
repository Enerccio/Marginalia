package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HexFormat;
import java.util.List;

/**
 * Notices an agent that goes round in circles: rounds whose calls <em>and</em> results are the same as in one of the last few
 * rounds, so the model learned nothing from them. The runner feeds it the rounds that changed nothing ({@link #reset} for
 * every other round, the state of the world is then a different one) and acts on the verdict: a notice at
 * {@link Defaults#AGENT_REPEAT_NOTICE_AFTER}, the end of the turn at {@link Defaults#AGENT_REPEAT_STOP_AFTER}. Not thread safe, one
 * per conversation.
 */
public final class RepeatDetector {

    public enum Verdict {
        /** Nothing to say. */
        OK,
        /** The round is the same as earlier ones: the model is told. */
        WARN,
        /** Told and still at it: the model is asked once more, without tools, and the turn ends. */
        STOP
    }

    private final Deque<String> recent = new ArrayDeque<>();

    /**
     * Records a round and says what to do about it.
     *
     * @param calls   the calls of the round, in order
     * @param results what each of them returned (the same order)
     */
    public Verdict record(List<LLMToolCall> calls, List<String> results) {
        String fingerprint = fingerprint(calls, results);
        recent.addLast(fingerprint);
        while (recent.size() > Defaults.AGENT_REPEAT_WINDOW) {
            recent.removeFirst();
        }
        long same = recent.stream().filter(fingerprint::equals).count();
        if (same >= Defaults.AGENT_REPEAT_STOP_AFTER) {
            return Verdict.STOP;
        }
        return same == Defaults.AGENT_REPEAT_NOTICE_AFTER ? Verdict.WARN : Verdict.OK;
    }

    /**
     * Forgets the rounds: something changed, or the conversation starts anew.
     */
    public void reset() {
        recent.clear();
    }

    private static String fingerprint(List<LLMToolCall> calls, List<String> results) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (int i = 0; i < calls.size(); i++) {
                digest.update(calls.get(i).getName().getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                digest.update(String.valueOf(calls.get(i).getArguments()).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                digest.update(String.valueOf(i < results.size() ? results.get(i) : "").getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
