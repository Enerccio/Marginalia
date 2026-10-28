package com.github.enerccio.marginalia.domain.service.command;

import java.util.*;

/**
 * The links between commands that an agent gets only together, see
 * {@link com.github.enerccio.marginalia.domain.traits.Command#linkedCommand()}. The agent's allowlist is a plain list of
 * names, so it is completed when it is read: {@link #close} adds the linked commands (for the lists of the editor and
 * of the agents saved before a link existed), {@link #resolve} also takes out the commands that can't have all of theirs.
 */
public final class CommandLinks {

    private CommandLinks() {
    }

    /**
     * The names with the commands linked to them, and those linked to those, added. Only commands an agent can have are
     * added; names that no command has are kept as they are.
     *
     * @param definitions the registered commands
     * @return the names in their order, the added ones after them
     */
    public static Set<String> close(Collection<CommandDefinition> definitions, Collection<String> names) {
        Map<String, List<String>> links = linksOf(definitions);
        Set<String> closed = new LinkedHashSet<>(names);
        Deque<String> open = new ArrayDeque<>(closed);
        while (!open.isEmpty()) {
            for (String linked : links.getOrDefault(open.poll(), List.of())) {
                if (links.containsKey(linked) && closed.add(linked)) {
                    open.add(linked);
                }
            }
        }
        return closed;
    }

    /**
     * The commands an agent allowed these names can actually call: the names completed with the commands linked to them,
     * without the commands that are not registered or not available for an agent, and without those that have a linked
     * command of that kind.
     *
     * @param definitions the registered commands
     * @return the names in their order, the added ones after them
     */
    public static Set<String> resolve(Collection<CommandDefinition> definitions, Collection<String> names) {
        Map<String, List<String>> links = linksOf(definitions);
        Set<String> resolved = new LinkedHashSet<>(close(definitions, names));
        // a command whose linked command is missing is out, and so is everything linked to that one in turn
        resolved.removeIf(name -> !links.containsKey(name));
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Iterator<String> it = resolved.iterator(); it.hasNext(); ) {
                if (!resolved.containsAll(links.get(it.next()))) {
                    it.remove();
                    changed = true;
                }
            }
        }
        return resolved;
    }

    /**
     * The commands that need this one, directly or not: when it is taken away from an agent, so are they.
     */
    public static Set<String> dependents(Collection<CommandDefinition> definitions, String name) {
        Map<String, List<String>> links = linksOf(definitions);
        Set<String> found = new LinkedHashSet<>();
        Deque<String> open = new ArrayDeque<>(List.of(name));
        while (!open.isEmpty()) {
            String current = open.poll();
            links.forEach((other, linked) -> {
                if (linked.contains(current) && !other.equals(name) && found.add(other)) {
                    open.add(other);
                }
            });
        }
        return found;
    }

    // the commands an agent can have, with the names they are linked to
    private static Map<String, List<String>> linksOf(Collection<CommandDefinition> definitions) {
        Map<String, List<String>> links = new HashMap<>();
        for (CommandDefinition definition : definitions) {
            if (definition.availableForAgent()) {
                links.putIfAbsent(definition.commandName(), definition.linkedCommands());
            }
        }
        return links;
    }

}
