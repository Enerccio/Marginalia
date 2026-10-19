package com.github.enerccio.marginalia.domain.service.command;

import com.github.enerccio.marginalia.domain.model.impl.*;

/**
 * What a command works on and who answers its questions: the book, the lorebooks of the book and the way to ask for a
 * confirmation. A command gets it from {@link CommandContext#getScope()}, so it needs neither the open window nor the
 * session, and the same command works for the user at the command bar (the scope of the open book) and for an agent (the
 * scope of its working copy), see {@link com.github.enerccio.marginalia.domain.service.CommandScopes}.
 * <p>
 * The scope is also the <b>cage</b>: a part, a lorebook or an entry is reached through it by the uuid, and only the ones
 * that belong to the book of the scope (its parts, its lorebook and the subbooks of it, however deep) are found. A uuid of
 * anything else, a part of another book or a lorebook the book does not use, is refused as if it did not exist, so a
 * command can't be aimed outside of the book by what it is given.
 */
public interface CommandScope {

    /**
     * The book the command works on, as it is stored now.
     *
     * @return null when there is none (nothing is open)
     */
    Manuscript getManuscript() throws Exception;

    /**
     * The root lorebook of the book; null when the book has none.
     */
    Lorebook getLorebook() throws Exception;

    /**
     * The run of the agent that calls the command; null when the user does.
     */
    AgentRun getRun();

    /**
     * Whether an agent calls the command.
     */
    default boolean isAgent() {
        return getRun() != null;
    }

    /**
     * Asks a yes/no question of whoever is behind the call and waits for the answer. The user at the command bar gets a
     * dialog; an agent is not asked again, what it may do without asking was decided when the command was called (the
     * destructive mode of the agent), so the answer is yes. Gives {@code false} when the answer is no, when the run is
     * terminated meanwhile and when nobody can be asked.
     *
     * @param context the context of the command that asks, to see whether it was stopped
     */
    boolean confirm(CommandContext context, String question) throws InterruptedException;

    /**
     * The part of the book with that uuid.
     *
     * @throws IllegalArgumentException when it is not a part of the book of the scope, or is deleted
     */
    ChatMessage resolveMessage(String uuid) throws Exception;

    /**
     * The lorebook with that uuid, which has to be the lorebook of the book or one of its subbooks, however deep.
     *
     * @throws IllegalArgumentException when it is not one of them, or is deleted
     */
    Lorebook resolveLorebook(String uuid) throws Exception;

    /**
     * The entry with that uuid, which has to be an entry of one of the lorebooks of the book.
     *
     * @throws IllegalArgumentException when it is not one, or is deleted
     */
    LorebookEntry resolveEntry(String uuid) throws Exception;

}
