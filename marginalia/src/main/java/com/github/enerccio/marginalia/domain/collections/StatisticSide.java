package com.github.enerccio.marginalia.domain.collections;

/**
 * Whose statistics are read: what the author did (with the models they asked), what the agents did on the book, or both.
 */
public enum StatisticSide {

    ALL(null),
    AUTHOR(false),
    AGENTS(true);

    private final Boolean agent;

    StatisticSide(Boolean agent) {
        this.agent = agent;
    }

    /**
     * @return the value of the {@code agent} flag of the rows of this side, null for both sides
     */
    public Boolean agent() {
        return agent;
    }

    public static StatisticSide of(boolean agent) {
        return agent ? AGENTS : AUTHOR;
    }

}
