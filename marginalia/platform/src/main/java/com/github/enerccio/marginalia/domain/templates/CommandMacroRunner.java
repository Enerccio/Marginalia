package com.github.enerccio.marginalia.domain.templates;

import java.util.List;

/**
 * Runs the command of a {@code {{command::name::argument}}} macro. A render that has none (the summary prompts, the
 * export) leaves the macro empty; the generation brings one that runs the command for the book it generates in and keeps
 * the answer, see {@code CommandMacros}.
 */
@FunctionalInterface
public interface CommandMacroRunner {

    /**
     * @param commandName the name of the command
     * @param arguments   the arguments of the macro after the name
     * @return what the command answered as text, empty when it failed
     */
    String run(String commandName, List<String> arguments);
}
