package com.github.enerccio.marginalia.extensions.prompthistory;

import com.github.enerccio.marginalia.bound.SessionPoint;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Where the histories are: the one of a browser session is in the plugin data of its {@link SessionPoint}, the ones of
 * the runs of agents (which have sessions of their own that are made up again at every turn of the run) are kept here,
 * the latest few runs.
 */
final class PromptHistory {

    static final String KEY = "com.github.enerccio.marginalia.extensions.prompthistory.history";

    /** How many runs of agents keep their history. */
    private static final int AGENT_RUNS = 10;

    private final Map<String, HistoryStore> agentRuns = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, HistoryStore> eldest) {
            return size() > AGENT_RUNS;
        }
    };

    /**
     * The history of the session; call it where the session is reachable, the store can then be used from any thread.
     * A store left in a long-lived session by an earlier load of this plugin is of a class that is gone with it: it is
     * replaced.
     */
    static HistoryStore of(SessionPoint session) {
        try {
            return session.getPluginData(KEY, HistoryStore::new);
        } catch (ClassCastException e) {
            session.getPluginData().remove(KEY);
            return session.getPluginData(KEY, HistoryStore::new);
        }
    }

    /**
     * The history of a run of an agent, made when first asked for.
     *
     * @param scope the scope of the {@code agent} kind of work: the id of the run
     */
    HistoryStore ofAgentRun(String scope) {
        synchronized (agentRuns) {
            return agentRuns.computeIfAbsent(scope, s -> new HistoryStore());
        }
    }
}
