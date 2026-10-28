package com.github.enerccio.marginalia.domain.service.command;

import com.github.enerccio.marginalia.domain.traits.BatchPolicy;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;

import java.util.List;

/**
 * A registered command: the metadata of the {@link com.github.enerccio.marginalia.domain.traits.Command} annotation
 * of its method and the suite object the method is called on. The overloads of a command are separate definitions that
 * share the {@code commandName}.
 *
 * @param commandName    unique name the command is run by
 * @param humanName      name shown to the user, the command name when the command gives none
 * @param description    what the command does, for the user, may be empty
 * @param llmDescription what the command does, for an agent, may be empty
 * @param async          whether the command runs on a worker thread and answers when it likes
 * @param dataSeverity   whether the command changes or removes the data that exists, the same for all the overloads
 * @param availableForAgent whether an agent can be given the command, the same for all the overloads
 * @param readOnly       whether the command changes no data at all, the same for all the overloads
 * @param batch          how the command is used among the calls of one answer of an agent, the same for all the overloads
 * @param linkedCommands the names of the commands an agent gets only together with this one, see
 *                       {@link com.github.enerccio.marginalia.domain.traits.Command#linkedCommand()}, the same for all the overloads; empty for none
 * @param instance       the registered suite object, the one that has the method
 * @param anyArguments   whether the command takes any arguments (it declares no signature); then {@code parameters} is
 *                       empty
 * @param parameters     the parameters of the signature in order
 * @param returns        what the command returns
 */
public record CommandDefinition(String commandName, CommandText humanName, CommandText description, CommandText llmDescription,
                                boolean async, CommandDataSeverity dataSeverity, boolean availableForAgent, boolean readOnly, BatchPolicy batch, List<String> linkedCommands, Object instance, boolean anyArguments,
                                List<CommandParameterDefinition> parameters, CommandReturnDefinition returns) {

}
