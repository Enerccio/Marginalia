package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * {@code /get-template-variables scope [part [branch]]}: the template variables of the book ({@code global}) or of a part ({@code part}).
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class GetTemplateVariablesCommand extends ManuscriptReadSupport {

    static final String NAME = "get-template-variables";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_TEMPLATE_VARIABLES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_TEMPLATE_VARIABLES, llmDescription = L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scope", type = String.class, description = L.DESC_COMMAND_SCOPE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_RETURN))
    public void getTemplateVariables(CommandContext context) throws Exception {
        templateVariables(context, context.get("scope", String.class), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_TEMPLATE_VARIABLES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_TEMPLATE_VARIABLES, llmDescription = L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scope", type = String.class, description = L.DESC_COMMAND_SCOPE_PARAM),
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_RETURN))
    public void getTemplateVariablesPart(CommandContext context) throws Exception {
        templateVariables(context, context.get("scope", String.class), context.get("part", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_TEMPLATE_VARIABLES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_TEMPLATE_VARIABLES, llmDescription = L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scope", type = String.class, description = L.DESC_COMMAND_SCOPE_PARAM),
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_RETURN))
    public void getTemplateVariablesBranch(CommandContext context) throws Exception {
        templateVariables(context, context.get("scope", String.class), context.get("part", Integer.class), context.get("branch", Integer.class));
    }

    private void templateVariables(CommandContext context, String scope, Integer position, Integer branch) throws Exception {
        String name = scope == null ? "" : scope.strip().toLowerCase();
        Book book = new Book();
        TemplateVariables variables = new TemplateVariables();
        JsonObject result = new JsonObject();
        result.addProperty("scope", name);
        switch (name) {
            case "global" -> variables.loadFrom(TemplateVariables.Scope.GLOBAL, book.manuscript.getAttributes());
            case "part" -> {
                List<ChatMessage> path = book.path(branch);
                int at = position == null ? path.size() : position;
                variables.loadFrom(TemplateVariables.Scope.LOCAL, book.part(path, at).getAttributes());
                result.addProperty("position", at);
            }
            default -> throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_UNKNOWN_SCOPE), scope));
        }
        result.add("variables", variables.toJson("global".equals(name) ? TemplateVariables.Scope.GLOBAL
                : TemplateVariables.Scope.LOCAL));
        answer(context, result);
    }
}
