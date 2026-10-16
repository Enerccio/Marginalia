package com.github.enerccio.marginalia.domain.service.command;

import java.util.List;

/**
 * A registered command: the metadata of the {@link com.github.enerccio.marginalia.domain.traits.Command} annotation
 * of its method and the suite object the method is called on. The overloads of a command are separate definitions that
 * share the {@code commandName}.
 *
 * @param commandName    unique name the command is run by
 * @param humanName      name shown to the user
 * @param description    what the command does, for the user, may be empty
 * @param llmDescription what the command does, for an agent, may be empty
 * @param async          whether the command runs on a worker thread and answers when it likes
 * @param instance       the registered suite object, the one that has the method
 * @param anyArguments   whether the command takes any arguments (it declares no signature); then {@code parameters} is
 *                       empty
 * @param parameters     the parameters of the signature in order
 * @param returns        what the command returns
 */
public record CommandDefinition(String commandName, String humanName, String description, String llmDescription,
                                boolean async, Object instance, boolean anyArguments,
                                List<CommandParameterDefinition> parameters, CommandReturnDefinition returns) {

}
