package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.Proposal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The proposals that a command made with {@code ask true} and holds until {@code conlang-apply-changes} applies the numbers the caller chose (the
 * Knowledge Tracker's {@code runs.hold}). A proposal is held per place (the book of the run, or the user) and language, the newest replaces the older;
 * it lives in memory only, a restart of the application drops it.
 */
public final class HeldProposals {

    private static final Map<String, Proposal> HELD = new ConcurrentHashMap<>();

    private HeldProposals() {
    }

    private static String key(String place, String languageId) {
        return place + "|" + languageId;
    }

    public static void hold(String place, String languageId, Proposal proposal) {
        HELD.put(key(place, languageId), proposal);
    }

    /**
     * @return the held proposal, null when none is held
     */
    public static Proposal peek(String place, String languageId) {
        return HELD.get(key(place, languageId));
    }

    /**
     * @return the held proposal, taken away; null when none is held
     */
    public static Proposal take(String place, String languageId) {
        return HELD.remove(key(place, languageId));
    }
}
