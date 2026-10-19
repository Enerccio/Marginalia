package com.github.enerccio.marginalia.domain.service.agent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The last results the commands of a run returned, whole: the model gets a result cut at
 * {@link CommandFunctions#MAX_RESULT_LENGTH}, the file tools that list and save them
 * ({@link AgentFileTools#listCommandResults}, {@link AgentFileTools#saveCommandResult}) get all of it, so a long result can
 * be kept without the model writing it out again, and looked at later. Each result has a number, counted from 1 in the
 * run; only the last {@value #KEEP} are kept. One per run, shared by the functions of the commands (which add to it) and
 * the file tools (which read it). Only a call that returned counts, a failed one is not kept.
 */
public final class CommandResults {

    /** How many results are kept. */
    public static final int KEEP = 10;
    /** The longest text of the arguments of a call that is shown in the list, in characters. */
    public static final int MAX_ARGUMENTS = 200;

    /**
     * @param id        the number of the result, the first one of the run is 1
     * @param function  the name of the function of the command that returned it
     * @param arguments the arguments of the call, shortened, for the list
     * @param text      the result as the model was given it, before it was cut
     */
    public record Entry(int id, String function, String arguments, String text) {}

    private final LinkedList<Entry> kept = new LinkedList<>();
    private int counter;

    /**
     * Keeps a result.
     *
     * @return its number
     */
    public synchronized int add(String function, String arguments, String text) {
        Entry entry = new Entry(++counter, function, arguments, text);
        kept.add(entry);
        while (kept.size() > KEEP) {
            kept.removeFirst();
        }
        return entry.id();
    }

    /**
     * @return the results that are kept, the oldest first
     */
    public synchronized List<Entry> list() {
        return new ArrayList<>(kept);
    }

    /**
     * @return the result with that number, null when it is not kept (it never was, or it is older than the last
     * {@value #KEEP})
     */
    public synchronized Entry get(int id) {
        return kept.stream().filter(entry -> entry.id() == id).findFirst().orElse(null);
    }

    /**
     * @return the newest result, null when no command has returned yet
     */
    public synchronized Entry last() {
        return kept.peekLast();
    }

}
