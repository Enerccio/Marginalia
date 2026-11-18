package com.github.enerccio.marginalia.domain.service.command;

/**
 * What the command framework itself needs of the scope a command runs in: whether an agent calls it and how to ask the caller
 * a question. What a command works on (in the application the book, its lorebooks and the run of an agent) is in the
 * application's subtype, {@code CommandScope}, which a command gets with {@code CommandScope.of(context)}.
 */
public interface BaseCommandScope {

    /**
     * Whether an agent calls the command.
     */
    boolean isAgent();

    /**
     * Asks a yes/no question of whoever is behind the call and waits for the answer. Gives {@code false} when the answer is
     * no, when the run is terminated meanwhile and when nobody can be asked.
     *
     * @param context the context of the command that asks, to see whether it was stopped
     */
    boolean confirm(CommandContext context, String question) throws InterruptedException;
}
