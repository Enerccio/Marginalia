package com.github.enerccio.marginalia.domain.service.command;

import com.github.enerccio.marginalia.loc.L;

import java.util.*;
import java.util.function.Function;

/**
 * What to tell the user who types a command: which command it is, which forms (overloads) of it fit what is typed so
 * far, and which parameter comes next. Pure logic over the registered definitions, shown by the command bar.
 *
 * @param typingName      whether the name is still being typed ({@code commandName} is then the best match)
 * @param commandName     the command the text is about, null when there is none
 * @param overloads       the forms of the command that can take the argument being typed; all forms when none can
 * @param argumentIndex   the index of the argument being typed, -1 while the name is typed
 * @param tooManyArguments whether more arguments are typed than any form takes
 */
public record CommandHelp(boolean typingName, String commandName, List<CommandDefinition> overloads,
                          int argumentIndex, boolean tooManyArguments) {

    /**
     * The names of the commands that match the text, best first: the names that start with the typed name, then the
     * ones that contain it or have it in the human name or the description. All names when nothing is typed. The texts
     * are matched as the {@code localizer} gives them (the language of the user).
     */
    public static List<String> matchingNames(List<CommandDefinition> all, String text, Function<L, String> localizer) {
        String typed = CommandLine.parse(text).name().toLowerCase(Locale.ROOT);
        Map<String, List<CommandDefinition>> byName = byName(all);
        List<String> starts = new ArrayList<>();
        List<String> contains = new ArrayList<>();
        for (Map.Entry<String, List<CommandDefinition>> entry : byName.entrySet()) {
            String name = entry.getKey();
            CommandDefinition first = entry.getValue().getFirst();
            if (name.toLowerCase(Locale.ROOT).startsWith(typed)) {
                starts.add(name);
            } else if (name.toLowerCase(Locale.ROOT).contains(typed)
                    || first.humanName().get(localizer).toLowerCase(Locale.ROOT).contains(typed)
                    || entry.getValue().stream().anyMatch(d -> d.description().get(localizer).toLowerCase(Locale.ROOT).contains(typed))) {
                contains.add(name);
            }
        }
        starts.sort(String.CASE_INSENSITIVE_ORDER);
        contains.sort(String.CASE_INSENSITIVE_ORDER);
        starts.addAll(contains);
        return starts;
    }

    public static CommandHelp of(List<CommandDefinition> all, String text, Function<L, String> localizer) {
        CommandLine line = CommandLine.parse(text);
        if (!line.command()) {
            return new CommandHelp(false, null, List.of(), -1, false);
        }
        Map<String, List<CommandDefinition>> byName = byName(all);
        if (line.typingName()) {
            List<String> matches = matchingNames(all, line.name(), localizer);
            if (matches.isEmpty()) {
                return new CommandHelp(true, null, List.of(), -1, false);
            }
            String exact = matches.stream().filter(n -> n.equalsIgnoreCase(line.name())).findFirst().orElse(matches.getFirst());
            return new CommandHelp(true, exact, byName.get(exact), -1, false);
        }

        List<CommandDefinition> all1 = byName.get(line.name());
        if (all1 == null) {
            return new CommandHelp(false, null, List.of(), line.currentArgument(), false);
        }
        int index = line.currentArgument();
        List<CommandDefinition> compatible = all1.stream()
                .filter(d -> d.anyArguments() || d.parameters().size() > index).toList();
        if (compatible.isEmpty()) {
            return new CommandHelp(false, line.name(), all1, index, true);
        }
        return new CommandHelp(false, line.name(), compatible, index, false);
    }

    private static Map<String, List<CommandDefinition>> byName(List<CommandDefinition> all) {
        Map<String, List<CommandDefinition>> byName = new LinkedHashMap<>();
        for (CommandDefinition definition : all) {
            byName.computeIfAbsent(definition.commandName(), _ -> new ArrayList<>()).add(definition);
        }
        return byName;
    }

}
