package com.github.enerccio.marginalia.extensions.betareaders.service;

import java.util.List;
import java.util.function.Predicate;

/**
 * Picks the AI or the protocol of a chat: the first of the candidates that still exists. A candidate that is empty, is not found or
 * is deleted counts as not set, so a chat whose tab names an AI that was deleted uses the one of the settings chain, and that one
 * falls back to the AI of the book.
 */
public final class ModelChoice {

    private ModelChoice() {
    }

    /**
     * Finds an entity by its uuid; null when there is none.
     */
    @FunctionalInterface
    public interface Lookup<T> {
        T find(String uuid) throws Exception;
    }

    /**
     * @param uuids  the candidates in the order of priority; null and blank ones are skipped
     * @param lookup finds the entity of a uuid
     * @param usable whether the entity found can be used (not deleted)
     * @return the first usable entity; null when there is none
     */
    public static <T> T firstUsable(List<String> uuids, Lookup<T> lookup, Predicate<T> usable) throws Exception {
        for (String uuid : uuids) {
            if (uuid == null || uuid.isBlank()) {
                continue;
            }
            T found = lookup.find(uuid);
            if (found != null && usable.test(found)) {
                return found;
            }
        }
        return null;
    }
}
